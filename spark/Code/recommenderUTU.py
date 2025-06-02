from pyspark.ml.linalg import Vectors, DenseVector 
from pyspark.ml.feature import Normalizer
from pyspark.sql.types import FloatType
from pyspark.sql.functions import udf, collect_list, struct
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, when, sum as spark_sum, explode
from pyspark.ml.feature import StringIndexer, IndexToString
from pyspark.ml.recommendation import ALS
from datetime import datetime, timezone
import happybase

spark = (SparkSession.builder
         .appName("reco_streaming")
         .getOrCreate())

rate_df = (spark.readStream
           .format("rate")
           .option("rowsPerSecond", 1)
           .load())

def cosine_sim(v1, v2):
    v1 = DenseVector(v1)
    v2 = DenseVector(v2)
    return float(v1.dot(v2) / (v1.norm(2) * v2.norm(2))) if v1.norm(2) > 0 and v2.norm(2) > 0 else 0.0

cosine_udf = udf(cosine_sim, FloatType())

def write_to_hbase(partition):
    import happybase
    conn = happybase.Connection('hbase', port=9090)
    table = conn.table('user_recommendations')
    for row in partition:
        uid = row['user_id']
        pid = row['product_id']
        score = str(row['score'])
        print(f"[DEBUG][WRITE] Writing: user={uid}, product={pid}, score={score}")
        table.put(uid.encode(), {f'cf:{pid}'.encode(): score.encode()})
    conn.close()

def process_batch(df, epoch_id):
    print(f"\n===== [BATCH {epoch_id}] Traitement =====")

    try:
        last_ts = spark.sql("SELECT COALESCE(MAX(ts_processed), 0) AS last_ts FROM offsets") \
                       .collect()[0].last_ts
    except Exception:
        spark.createDataFrame([(0,)], ["ts_processed"]).createOrReplaceTempView("offsets")
        last_ts = 0
    print(f"[DEBUG] Dernier timestamp traité : {last_ts}")
    conn = happybase.Connection('hbase', port=9090)
    table = conn.table('user_actions')
    rows = []
    for key, data in table.scan():
        try:
            ts_str = data.get(b'cf:timestamp', b'').decode()
            dt = datetime.fromisoformat(ts_str.replace("Z", "+00:00"))
            ts_ms = int(dt.timestamp() * 1000)

        # === MODIFICATION ICI ===
        # On commente le filtre pour garder tous les logs
        # if ts_ms > last_ts:
            rows.append((
                data.get(b'cf:user', b'').decode(),
                data.get(b'cf:product', b'').decode(),
                data.get(b'cf:action', b'').decode(),
                ts_ms
            ))
        # =========================

        except Exception as e:
            print(f"[ERROR] Parsing row failed: {e}")
    conn.close() 


    if not rows:
        print("[DEBUG] Aucun nouveau log à traiter.")
        return

    logs_df = spark.createDataFrame(rows, ['user_id', 'product_id', 'action', 'timestamp'])
    print(f"[DEBUG] Nombre de logs à traiter : {logs_df.count()}")
    logs_df.show(truncate=False)

    max_ts = logs_df.agg({"timestamp": "max"}).collect()[0][0]
    spark.createDataFrame([(max_ts,)], ["ts_processed"]).createOrReplaceTempView("offsets")

    interactions = logs_df.withColumn("rating",
        when(col("action") == "purchase", 3)
        .when(col("action") == "PutInCart", 2)
        .when(col("action") == "click", 1)
        .otherwise(0)
    )
    print("[DEBUG] Interactions avec scores :")
    interactions.show()

    ratings = interactions.groupBy("user_id", "product_id") \
                          .agg(spark_sum("rating").alias("rating"))
    print("[DEBUG] Ratings agrégés :")
    ratings.show()

    pivot_df = ratings.groupBy("user_id") \
                      .pivot("product_id") \
                      .agg(spark_sum("rating")) \
                      .fillna(0)
    print("[DEBUG] Pivot user-product :")
    pivot_df.show()

    cols = pivot_df.columns[1:]  # sauf user_id
    def row_to_vec(row):
        return (row["user_id"], Vectors.dense([row[c] for c in cols]))
    vec_df = pivot_df.rdd.map(row_to_vec).toDF(["user_id", "features"])

    norm_df = Normalizer(inputCol="features", outputCol="norm_features").transform(vec_df)

    similarities = (norm_df.alias("a")
        .crossJoin(norm_df.alias("b"))
        .where(col("a.user_id") != col("b.user_id"))
        .select(
            col("a.user_id").alias("user1"),
            col("b.user_id").alias("user2"),
            cosine_udf("a.norm_features", "b.norm_features").alias("similarity"))
        .filter(col("similarity") > 0.5)
    )
    print("[DEBUG] Similarités utilisateur > 0.5 :")
    similarities.show()

    top_similar = similarities.groupBy("user1") \
        .agg(collect_list(struct("similarity", "user2")).alias("neighbors"))
    top_similar.show()

    user_items = ratings.groupBy("user_id") \
                        .agg(collect_list("product_id").alias("products"))
    print("[DEBUG] Produits par utilisateur :")
    user_items.show()

    top_similar_with_items = top_similar.join(user_items, top_similar.user1 == user_items.user_id, "left")

    recommendations = []
    for row in top_similar_with_items.collect():
        user = row['user1']
        seen = set(row['products']) if row['products'] else set()
        neighbors = row['neighbors']
        neighbor_ids = [n['user2'] for n in neighbors]

        neighbor_products = (ratings
            .filter(col("user_id").isin(neighbor_ids))
            .groupBy("product_id")
            .agg(spark_sum("rating").alias("score"))
            .orderBy(col("score").desc())
            .limit(5)
        )

        unseen = neighbor_products.filter(~col("product_id").isin(seen)).collect()
        for p in unseen:
            print(f"[DEBUG] Recommendation for user={user} : product={p['product_id']} score={p['score']}")
            recommendations.append((user, p["product_id"], float(p["score"])))

    if not recommendations:
        print("[DEBUG] Aucune recommandation générée.")
        return

    rec_df = spark.createDataFrame(recommendations, ["user_id", "product_id", "score"])
    print(f"[DEBUG] Recommandations générées ({rec_df.count()} lignes) :")
    rec_df.show()
    rec_df.foreachPartition(lambda part: write_to_hbase(part))

(stream_id := rate_df.writeStream
 .trigger(processingTime="30 seconds")
 .foreachBatch(process_batch)
 .option("checkpointLocation", "/tmp/checkpoint_reco")
 .start())

stream_id.awaitTermination()
