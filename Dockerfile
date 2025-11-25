FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src
COPY scripts ./scripts

# Compilamos
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-pip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


COPY scripts/ai/requirements.txt /app/scripts/ai/requirements.txt
RUN pip3 install --no-cache-dir -r /app/scripts/ai/requirements.txt

COPY scripts/ai /app/scripts/ai
COPY --from=build /app/target/backend-*.jar app.jar

ENV APP_SCRIPTS_PATH=/app/scripts/ai/
ENV APP_PYTHON_COMMAND=python3

CMD ["java", "-jar", "app.jar"]