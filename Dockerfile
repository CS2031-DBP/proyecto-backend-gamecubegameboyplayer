FROM openjdk:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/backend-*.jar app.jar
CMD ["java", "-jar", "app.jar"]
