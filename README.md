# Système de Recommandation en Temps Réel (E-Commerce)

## 📋 Description

Ce projet est un système complet de recommandation en temps réel pour une plateforme e-commerce. Il combine une application web Spring Boot avec un pipeline de traitement de données Big Data pour fournir des recommandations personnalisées aux utilisateurs en fonction de leurs interactions avec les produits.

Le système capture les actions des utilisateurs (vues, likes, achats), les traite en temps réel via Apache Flume et HBase, puis génère des recommandations personnalisées en utilisant Apache Spark avec différents algorithmes de Machine Learning (ALS, Content-Based, User-to-User).

### Fonctionnalités Principales

- **Application E-Commerce** : Interface web complète avec authentification, catalogue de produits, panier et système de commande
- **Capture d'Actions Utilisateur** : Logging en temps réel des interactions (vues, likes, achats)
- **Ingestion de Données** : Pipeline Apache Flume pour collecter et stocker les logs dans HBase
- **Recommandations ML** : Algorithmes de recommandation utilisant Spark MLlib
  - ALS (Alternating Least Squares) - Filtrage collaboratif
  - Content-Based Filtering - Recommandations basées sur le contenu
  - User-to-User Collaborative Filtering
- **Architecture Microservices** : Déploiement via Docker Compose

## 🏗️ Architecture

Le système est composé de plusieurs services containerisés :

```
┌─────────────────┐     ┌──────────────┐     ┌─────────────┐
│  Application    │────▶│    Flume     │────▶│   HBase     │
│  Spring Boot    │     │   (Logs)     │     │  (Storage)  │
│  (Emarket)      │     └──────────────┘     └─────────────┘
└─────────────────┘                                  │
        │                                            │
        │                                            ▼
        │                                    ┌─────────────┐
        └───────────────────────────────────▶│    Spark    │
                 (Récupération recommandations)│  (ML/Reco)  │
                                              └─────────────┘
```

### Services Docker

1. **HBase** : Stockage NoSQL des actions utilisateurs
2. **Spark Master/Worker** : Traitement distribué et génération de recommandations
3. **Flume** : Collecte et ingestion des logs
4. **Emarket** : Application web Spring Boot

## 🔧 Prérequis

Avant d'installer le projet, assurez-vous d'avoir les outils suivants installés :

- **Docker** (version 20.10 ou supérieure)
- **Docker Compose** (version 2.0 ou supérieure)
- **Java JDK 17** (pour le développement local)
- **Maven 3.8+** (pour le build local)
- **Git** (pour cloner le projet)

### Ressources Système Recommandées

- **RAM** : Minimum 8 GB (16 GB recommandé)
- **CPU** : Minimum 4 cores
- **Espace Disque** : Minimum 10 GB

## 📦 Installation

### 1. Cloner le Répertoire

```bash
git clone https://github.com/Asenn2/recommendation-real-time.git
cd recommendation-real-time
```

### 2. Construire les Images Docker

Le projet utilise des images Docker personnalisées. Construisez-les avec :

```bash
# Image Spark avec support HBase
cd spark/DockerF
docker build -t sparkhbase:latest .
cd ../..

# Image Flume avec support HBase
cd Flume
docker build -t flume_hbase:latest .
cd ..
```

### 3. Démarrer les Services

Lancez tous les services avec Docker Compose :

```bash
docker-compose up -d
```

Cette commande démarre :
- HBase sur les ports 16010 (UI), 2181 (Zookeeper), 9090 (Thrift)
- Spark Master sur les ports 7077 (cluster), 8080 (UI)
- Spark Worker
- Flume agent

### 4. Initialiser HBase

Créez la table nécessaire pour stocker les actions utilisateurs :

```bash
# Accéder au conteneur HBase
docker exec -it hbase bash

# Lancer le shell HBase
hbase shell

# Créer la table user_actions
create 'user_actions', 'cf'

# Vérifier la création
list

# Quitter
exit
exit
```

### 5. Construire et Démarrer l'Application Emarket

```bash
cd Emarket

# Build avec Maven
./mvnw clean package

# Lancer l'application (alternative au Docker)
./mvnw spring-boot:run
```

Ou utilisez Docker pour l'application :

```bash
# À la racine du projet
docker build -f Dockerfile -t emarket:latest .
docker run -p 8080:8080 --network recommendation-real-time_flume-net emarket:latest
```

## 🚀 Utilisation

### Accéder à l'Application Web

Une fois tous les services démarrés, accédez à l'application :

```
http://localhost:8080
```

### Interfaces d'Administration

- **HBase Web UI** : http://localhost:16010
- **Spark Master UI** : http://localhost:8080 (si pas de conflit avec Emarket)

### Exemple d'Utilisation

#### 1. Créer un Compte Utilisateur

1. Accédez à l'application web
2. Cliquez sur "S'inscrire"
3. Remplissez le formulaire d'inscription
4. Connectez-vous avec vos identifiants

#### 2. Naviguer et Interagir avec les Produits

1. Parcourez le catalogue de produits
2. Cliquez sur un produit pour voir les détails
3. Likez des produits qui vous intéressent
4. Ajoutez des produits au panier
5. Finalisez une commande

Toutes ces actions sont automatiquement loggées dans le fichier `logs/user_actions.log` et ingérées par Flume vers HBase.

#### 3. Voir les Recommandations

Les recommandations sont générées automatiquement et affichées sur la page d'accueil ou sur une page dédiée selon l'interface. Les recommandations sont basées sur :
- Vos interactions passées (vues, likes)
- Les achats effectués
- Les similarités avec d'autres utilisateurs

### Lancer Manuellement les Scripts de Recommandation

Pour tester les différents algorithmes de recommandation :

```bash
# Accéder au conteneur Spark Master
docker exec -it spark-master bash

# Lancer l'algorithme ALS (Collaborative Filtering)
spark-submit \
  --master spark://spark-master:7077 \
  --packages com.google.guava:guava:31.1-jre \
  /opt/bitnami/spark/code/recommenderALS.py

# Lancer l'algorithme Content-Based
spark-submit \
  --master spark://spark-master:7077 \
  /opt/bitnami/spark/code/recommenderContent.py

# Lancer l'algorithme User-to-User
spark-submit \
  --master spark://spark-master:7077 \
  /opt/bitnami/spark/code/recommenderUTU.py
```

## 📁 Structure du Projet

```
recommendation-real-time/
│
├── Emarket/                    # Application Spring Boot principale
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/market/main/emarket/
│   │   │   │       ├── model/           # Entités JPA
│   │   │   │       ├── repositories/    # Repositories Spring Data
│   │   │   │       ├── services/        # Services métier
│   │   │   │       ├── controller/      # Contrôleurs REST/MVC
│   │   │   │       ├── security/        # Configuration sécurité
│   │   │   │       └── config/          # Configuration HBase/Thrift
│   │   │   └── resources/               # Templates Thymeleaf, config
│   │   └── test/                        # Tests unitaires
│   ├── pom.xml                          # Dépendances Maven
│   └── mvnw                             # Maven wrapper
│
├── spark/                      # Scripts Spark pour les recommandations
│   ├── Code/
│   │   ├── recommenderALS.py            # Algorithme ALS
│   │   ├── recommenderContent.py        # Filtrage basé contenu
│   │   ├── recommenderUTU.py            # User-to-User filtering
│   │   └── products.json                # Données produits
│   └── DockerF/                         # Dockerfile Spark personnalisé
│
├── Flume/                      # Configuration Apache Flume
│   ├── Dockerfile                       # Image Flume avec HBase
│   ├── flume.conf                       # Configuration agent Flume
│   └── lib/                             # Librairies personnalisées
│
├── conf/                       # Fichiers de configuration
│   ├── flume-agent.conf                 # Config Flume
│   └── hbase-site.xml                   # Config HBase
│
├── logs/                       # Logs applicatifs
│   └── user_actions.log                 # Logs des actions utilisateurs
│
├── hbase_data/                 # Données persistantes HBase
├── docker-compose.yml          # Orchestration des services
├── Dockerfile                  # Image Docker pour Emarket
└── README.md                   # Ce fichier
```

## 🛠️ Technologies Utilisées

### Backend
- **Java 17** - Langage de programmation
- **Spring Boot 3.4.3** - Framework web
- **Spring Security** - Authentification et autorisation
- **Spring Data JPA** - Accès aux données
- **MySQL** - Base de données relationnelle
- **Thymeleaf** - Moteur de templates

### Big Data & ML
- **Apache HBase 1.4** - Base de données NoSQL distribuée
- **Apache Flume** - Ingestion de données en temps réel
- **Apache Spark 3.5.0** - Traitement distribué et ML
- **PySpark MLlib** - Algorithmes de Machine Learning
- **HappyBase** - Client Python pour HBase

### DevOps
- **Docker** - Containerisation
- **Docker Compose** - Orchestration multi-conteneurs
- **Maven** - Gestion des dépendances Java

### Autres
- **Lombok** - Réduction de code boilerplate
- **Logstash Logback** - Logging structuré JSON
- **Gson** - Sérialisation JSON

## 🔍 Algorithmes de Recommandation

### 1. ALS (Alternating Least Squares)
Algorithme de filtrage collaboratif basé sur la factorisation matricielle. Recommande des produits en trouvant des patterns dans les interactions utilisateur-produit.

### 2. Content-Based Filtering
Recommande des produits similaires à ceux que l'utilisateur a aimés, basé sur les caractéristiques des produits (catégorie, prix, etc.).

### 3. User-to-User Collaborative Filtering
Trouve des utilisateurs similaires et recommande les produits qu'ils ont appréciés.

## 📊 Monitoring et Logs

### Vérifier les Logs Flume
```bash
docker logs flume
```

### Vérifier les Données dans HBase
```bash
docker exec -it hbase bash
hbase shell
scan 'user_actions', {LIMIT => 10}
```

### Voir les Logs de l'Application
```bash
docker logs spark-master
tail -f logs/user_actions.log
```

## 🐛 Dépannage

### Les services ne démarrent pas
```bash
# Vérifier l'état des conteneurs
docker-compose ps

# Vérifier les logs d'un service spécifique
docker-compose logs <nom_service>

# Redémarrer tous les services
docker-compose restart
```

### HBase n'est pas accessible
```bash
# Vérifier que HBase est bien démarré
docker exec -it hbase bash
hbase version

# Vérifier le processus Thrift
jps | grep Thrift
```

### Problème de connexion à HBase depuis Spark
Assurez-vous que le fichier `conf/hbase-site.xml` est correctement monté dans les conteneurs et contient la bonne configuration.

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Forkez le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Poussez vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📝 License

Ce projet est un projet éducatif/démonstration. Veuillez consulter les licenses des différentes technologies utilisées.

## 👥 Auteurs

- Asenn2 - *Travail initial* - [Asenn2](https://github.com/Asenn2)

## 🙏 Remerciements

- Apache Software Foundation pour HBase, Flume et Spark
- L'équipe Spring pour Spring Boot
- La communauté open-source pour tous les outils utilisés

---

**Note** : Ce README suppose que vous avez une configuration MySQL existante pour l'application Emarket. Assurez-vous de configurer les propriétés de connexion dans `Emarket/src/main/resources/application.properties`.
