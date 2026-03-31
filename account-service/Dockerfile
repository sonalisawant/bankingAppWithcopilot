FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY common/pom.xml common/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY common/src common/src
COPY account-service/src account-service/src

RUN mvn -pl account-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/account-service/target/account-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
