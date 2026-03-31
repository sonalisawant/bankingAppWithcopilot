FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY common/pom.xml common/pom.xml
COPY audit-service/pom.xml audit-service/pom.xml
COPY common/src common/src
COPY audit-service/src audit-service/src

RUN mvn -pl audit-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/audit-service/target/audit-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
