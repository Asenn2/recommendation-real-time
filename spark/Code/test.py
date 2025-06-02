import happybase

# Remplace l'adresse IP par celle de ton serveur HBase
connection = happybase.Connection(host='hbase',port=9090)  # ou '192.168.x.x' si distant
connection.open()

# Liste les tables existantes
print("Tables disponibles :", connection.tables())
