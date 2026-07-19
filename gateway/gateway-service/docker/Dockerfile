# Single-stage runtime image — the JAR is built on the Windows dev machine
# (mvn clean package) and transferred here via SCP/SMB, same pattern as the
# rest of the portfolio. No Maven/JDK toolchain runs inside this container.
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY gateway-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
