FROM maven:3.8.4-jdk-8-slim AS build
WORKDIR /app
COPY .. /app
RUN mvn clean package

FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/target/profile-embedded-0.1.0.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]

