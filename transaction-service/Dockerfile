FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY common/pom.xml common/pom.xml
COPY transaction-service/pom.xml transaction-service/pom.xml
COPY common/src common/src
COPY transaction-service/src transaction-service/src

RUN mvn -pl transaction-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/transaction-service/target/transaction-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
