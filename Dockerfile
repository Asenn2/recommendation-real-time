# --- STAGE 1: Build de l’application avec Maven
FROM maven:3.8.6-openjdk-17 AS builder

# 1. On se place dans /build
WORKDIR /build

# 2. On copie pom + wrapper pour profiter du cache Docker si pom.xml ne change pas
COPY ./emarket/mvnw .
COPY ./emarket/.mvn .mvn

# 3. On télécharge les dépendances (cache)
RUN ./mvnw dependency:go-offline -B

# 4. On copie le code source et on compile
COPY src src
RUN ./mvnw package -DskipTests -B

# --- STAGE 2: Image finale d’exécution
FROM eclipse-temurin:17-jre

# 1. Création du répertoire d’exécution
WORKDIR /app

# 2. On récupère le JAR construit dans le stage précédent
COPY --from=builder /build/target/*.jar app.jar

# 3. Exposition du port (aligné sur Docker-Compose : conteneur 8080)
EXPOSE 8080

# 4. Commande de démarrage
ENTRYPOINT ["java","-jar","app.jar"]
