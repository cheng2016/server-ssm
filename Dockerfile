FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY game-common game-common
COPY game-protocol game-protocol
COPY game-persistence game-persistence
COPY game-network game-network
COPY game-app game-app
RUN chmod +x mvnw && ./mvnw -B -ntp verify

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/game-app/target/game-app-*.jar /app/app.jar
EXPOSE 8080 9000 9001
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
