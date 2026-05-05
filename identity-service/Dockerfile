FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copier uniquement ce qui est nécessaire
COPY pom.xml .
COPY ../banking-common ./banking-common
COPY . .

# Build seulement ce service + dépendances
RUN mvn -B clean package -DskipTests

# Image finale
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN addgroup --system banking && adduser --system --ingroup banking banking

COPY --from=build /build/identity-service/target/*.jar app.jar

USER banking

EXPOSE 8081

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]