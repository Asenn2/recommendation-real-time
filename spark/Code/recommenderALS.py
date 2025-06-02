from pyspark.sql import SparkSession
from pyspark.sql.functions import col, when, sum as spark_sum, explode
from pyspark.ml.feature import StringIndexer, IndexToString
from pyspark.ml.recommendation import ALS
from datetime import datetime, timezone
import happybase

# 1. Créer la session Spark
spark = (SparkSession.builder
         .appName("reco_streaming")
         .getOrCreate())

# 2. Source factice de streaming
rate_df = (spark.readStream
           .format("rate")
           .option("rowsPerSecond", 1)
           .load())

def process_batch(df, epoch_id):
    print(f"\n===== Traitement du batch {epoch_id} =====")

    # Créer / mettre à jour la vue offsets
    try:
        last_ts = spark.sql("SELECT COALESCE(MAX(ts_processed), 0) AS last_ts FROM offsets") \
                       .collect()[0].last_ts
    except Exception:
        # première exécution
        spark.createDataFrame([(0,)], ["ts_processed"]) \
             .createOrReplaceTempView("offsets")
        last_ts = 0
    print(f"[INFO] Dernier timestamp traité : {last_ts}")

    # 3. Lecture des nouveaux logs dans HBase 1.4 via HappyBase
    conn = happybase.Connection('hbase', port=9090)
    table = conn.table('user_actions')
    rows = []
    for key, data in table.scan():
        ts_str = data.get(b'cf:timestamp', b'').decode()  
    # 1) Parser l'ISO et convertir en epoch ms
        dt = datetime.fromisoformat(ts_str.replace("Z", "+00:00"))
        ts_ms = int(dt.timestamp() * 1000)
        if ts_ms > last_ts:
            rows.append((
                data.get(b'cf:user', b'').decode(),
                data.get(b'cf:product', b'').decode(),
                data.get(b'cf:action', b'').decode(),
                ts_ms
            ))
    conn.close()

    if not rows:
        print("[INFO] Aucun nouveau log à traiter.")
        return

    # Créer un DataFrame Spark à partir des rows
    logs_df = spark.createDataFrame(
        rows,
        schema=['user_id', 'product_id', 'action', 'timestamp']
    )

    # Mettre à jour l’offset en mémoire
    max_ts = logs_df.agg({"timestamp": "max"}).collect()[0][0]
    spark.createDataFrame([(max_ts,)], ["ts_processed"]) \
         .createOrReplaceTempView("offsets")

    # 4. Transformations et ALS (identique)
    interactions = logs_df.withColumn("rating",
        when(col("action") == "purchase", 4)
	.when(col("action") == "putInCart",3)
        .when(col("action") == "like", 2)
        .when(col("action") == "click", 1)
        .otherwise(0)
    )
    ratings = (interactions
               .groupBy("user_id", "product_id")
               .agg(spark_sum("rating").alias("rating")))

    user_indexer = StringIndexer(inputCol="user_id", outputCol="user_idx").fit(ratings)
    item_indexer = StringIndexer(inputCol="product_id", outputCol="item_idx").fit(ratings)

    idx = (item_indexer
           .transform(user_indexer.transform(ratings))
           .select(col("user_idx").cast("int"),
                   col("item_idx").cast("int"),
                   "rating"))

    als = ALS(userCol="user_idx", itemCol="item_idx", ratingCol="rating",
              coldStartStrategy="drop", nonnegative=True)
    model = als.fit(idx)

    recs = model.recommendForAllUsers(5)
    flat = (recs
            .select("user_idx", explode("recommendations").alias("rec"))
            .select("user_idx",
                    col("rec.item_idx").alias("item_idx"),
                    col("rec.rating").alias("score")))

    rev_user = IndexToString(inputCol="user_idx", outputCol="user_id_orig",
                             labels=user_indexer.labels)
    rev_item = IndexToString(inputCol="item_idx", outputCol="product_id_orig",
                             labels=item_indexer.labels)
    final_recs = rev_item.transform(rev_user.transform(flat)) \
                        .select("user_id_orig", "product_id_orig", "score")

    print(f"[INFO] Recommandations générées pour {final_recs.count()} lignes.")

    # 5. Écriture dans HBase (inchangé)
    final_recs.foreachPartition(lambda part: write_to_hbase(part))

def write_to_hbase(partition):
    import happybase
    conn = happybase.Connection('hbase', port=9090)
    table = conn.table('user_recommendations')
    for row in partition:
        uid = row['user_id_orig']
        pid = row['product_id_orig']
        score = str(row['score'])
        table.put(uid.encode(), {f'cf:{pid}'.encode(): score.encode()})
    conn.close()

# 6. Lancer le streaming
(stream_id := rate_df.writeStream
 .trigger(processingTime="30 seconds")
 .foreachBatch(process_batch)
 .option("checkpointLocation", "/tmp/checkpoint_reco")
 .start())

stream_id.awaitTermination()
