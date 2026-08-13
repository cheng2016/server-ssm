FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY game-common game-common
COPY game-protocol game-protocol
COPY game-persistence game-persistence
COPY game-network game-network
COPY game-app game-app
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/game-app/target/game-app-*.jar /app/app.jar
EXPOSE 8080 9000 9001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
