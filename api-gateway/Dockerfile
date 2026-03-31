FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src

RUN mvn -pl api-gateway -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
