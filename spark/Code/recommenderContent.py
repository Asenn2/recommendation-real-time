from pyspark.sql import SparkSession
from pyspark.sql.functions import concat_ws, col, collect_list, struct
from pyspark.ml.feature import Tokenizer, StopWordsRemover, HashingTF, IDF
from pyspark.ml.linalg import DenseVector
from pyspark.sql.types import FloatType
from pyspark.sql.functions import udf

import happybase

def write_recommendations_to_hbase(partition):
    connection = happybase.Connection('hbase', port=9090)
    table = connection.table('product_recommendations')
    
    for row in partition:
        product1 = row['product1']
        similar_items = row['similar_items']  # list of Row(product2=..., similarity=...)

        data = {}
        for item in similar_items:
            product2 = item['product2']
            similarity = str(round(item['similarity'], 4))
            col_name = f'cf:{product2}'
            data[col_name.encode()] = similarity.encode()

        print(f"[DEBUG] Writing recommendations for {product1} -> {len(data)} items")
        table.put(product1.encode(), data)

    connection.close()

# 1. Créer la SparkSession
spark = SparkSession.builder.appName("ContentBasedRecommender").getOrCreate()

# 2. Charger les données JSON
df = spark.read.json("code/products.json", multiLine=True)

# 3. Fusionner les colonnes textuelles pour former un profil produit
df = df.withColumn(
    "text_profile",
    concat_ws(" ", col("title"), col("brand"), col("description"), col("features"), col("categories"))
)

# 4. Pipeline de traitement de texte (Tokenisation + StopWords + TF-IDF)
tokenizer = Tokenizer(inputCol="text_profile", outputCol="words")
words_df = tokenizer.transform(df)

remover = StopWordsRemover(inputCol="words", outputCol="filtered")
filtered_df = remover.transform(words_df)

hashingTF = HashingTF(inputCol="filtered", outputCol="rawFeatures", numFeatures=1000)
featurized_df = hashingTF.transform(filtered_df)

idf = IDF(inputCol="rawFeatures", outputCol="tfidf_features")
idf_model = idf.fit(featurized_df)
tfidf_df = idf_model.transform(featurized_df)

# 5. UDF pour calcul de similarité cosinus
def cosine_sim(v1, v2):
    v1 = DenseVector(v1)
    v2 = DenseVector(v2)
    return float(v1.dot(v2) / (v1.norm(2) * v2.norm(2))) if v1.norm(2) > 0 and v2.norm(2) > 0 else 0.0

cosine_udf = udf(cosine_sim, FloatType())

first_product_title = tfidf_df.select("title").first()["title"]
test_similarities = (
    tfidf_df.alias("a")
    .join(tfidf_df.alias("b"), col("a.title") != col("b.title"))
    .where(col("a.title") == first_product_title)
    .select(
        col("a.title").alias("product1"),
        col("b.title").alias("product2"),
        cosine_udf(col("a.tfidf_features"), col("b.tfidf_features")).alias("similarity")
    )
    .filter(col("similarity") > 0.3)  # facultatif : seuil
)

# 7. Afficher les similarités pour le premier produit
test_similarities.show(truncate=False)




recommendations.foreachPartition(write_recommendations_to_hbase)
# 8. Afficher les résultats
# recommendations.show(truncate=False)

# 9. Optionnel : enregistrer en JSON
# recommendations.write.mode("overwrite").json("/path/to/output/recommendations")
