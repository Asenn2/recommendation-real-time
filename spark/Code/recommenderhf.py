import happybase
import pandas as pd
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, lit, when, sum as spark_sum
from pyspark.ml.feature import StringIndexer
from pyspark.ml.recommendation import ALS

# Connexion à HBase
connection = happybase.Connection('hbase', port=9090)
table = connection.table('user_actions')

# Lire les données de HBase
data = []
for key, row in table.scan():
    data.append({
        'user_id': row[b'cf:user'].decode(),
        'product_id': row[b'cf:product'].decode(),
        'action': row[b'cf:action'].decode(),
        'timestamp': row[b'cf:timestamp'].decode()
    })

# Convertir en DataFrame Pandas
df = pd.DataFrame(data)

# Créer une session Spark
spark = SparkSession.builder.appName("reco").getOrCreate()

# Convertir le DataFrame Pandas en DataFrame Spark
spark_df = spark.createDataFrame(df)

# Créer une colonne 'rating'
interactions = spark_df.withColumn("rating", when(col("action") == "purchase", 3)
                                   .when(col("action") == "click", 2)
                                   .when(col("action") == "view", 1)
                                   .otherwise(0))

# Agréger par user_id et product_id
ratings = interactions.groupBy("user_id", "product_id").agg(
    spark_sum("rating").alias("rating")
)

# Créer les indexeurs pour transformer user_id et product_id en indices numériques
user_indexer = StringIndexer(inputCol="user_id", outputCol="user_idx").fit(ratings)
item_indexer = StringIndexer(inputCol="product_id", outputCol="item_idx").fit(ratings)

# Appliquer les transformations d'indexation
ratings_indexed = user_indexer.transform(ratings)
ratings_indexed = item_indexer.transform(ratings_indexed)

# Assurez-vous que les colonnes user_idx et item_idx sont de type integer
ratings_indexed = ratings_indexed.withColumn("user_idx", col("user_idx").cast("int"))
ratings_indexed = ratings_indexed.withColumn("item_idx", col("item_idx").cast("int"))

# Entraîner le modèle ALS
als = ALS(
    userCol="user_idx",    # Utiliser user_idx au lieu de user_id
    itemCol="item_idx",    # Utiliser item_idx au lieu de product_id
    ratingCol="rating",
    coldStartStrategy="drop",
    nonnegative=True
)

# Entraîner le modèle
model = als.fit(ratings_indexed)

# Recommander 5 produits par utilisateur
user_recommendations = model.recommendForAllUsers(5)
# user_recommendations.show()

from pyspark.ml.feature import IndexToString
from pyspark.sql.functions import explode, col

# 1. Aplatir les recommandations
flat_recs = user_recommendations.select(
    col("user_idx"),
    explode("recommendations").alias("rec")
).select(
    col("user_idx"),
    col("rec.item_idx").alias("item_idx"),
    col("rec.rating").alias("score")
)

# 2. Créer les transformers pour revenir en string d'origine
user_ito = IndexToString(inputCol="user_idx", outputCol="user_id_orig",
                         labels=user_indexer.labels)
item_ito = IndexToString(inputCol="item_idx", outputCol="product_id_orig",
                         labels=item_indexer.labels)

# 3. Appliquer et ne garder que les colonnes lisibles
readable_recs = user_ito.transform(item_ito.transform(flat_recs)) \
    .select("user_id_orig", "product_id_orig", "score")

readable_recs.show(truncate=False)
