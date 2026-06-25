FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/app/target/jobportal-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
