FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /workspace/app

COPY . .

RUN mvn install

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /workspace/app

COPY --from=build /workspace/app/target/edm-api-0.0.1-SNAPSHOT.jar /workspace/app/target/edm-api-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","/workspace/app/target/edm-core-0.0.1-SNAPSHOT.jar"]