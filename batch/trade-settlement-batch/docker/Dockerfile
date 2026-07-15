# Build the JAR on the dev machine first:
#   mvn package -DskipTests
# Then copy the JAR to docker/ and build the image on the Docker server:
#   docker build -t trade-settlement-batch:latest -f docker/Dockerfile docker/

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# The JAR is built on the dev machine and transferred to docker/ via SCP
COPY trade-settlement-batch-1.0.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
