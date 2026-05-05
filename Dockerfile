#ARG MODULE
#
#FROM maven:3.9-eclipse-temurin-21 AS build
#ARG MODULE
#WORKDIR /workspace
#COPY pom.xml .
#COPY banking-common ./banking-common
#COPY api-gateway ./api-gateway
#COPY identity-service ./identity-service
#COPY profile-service ./profile-service
#COPY account-service ./account-service
#COPY payment-service ./payment-service
#COPY notification-service ./notification-service
#RUN mvn -B -pl ${MODULE} -am -DskipTests package
#
#FROM eclipse-temurin:21-jre
#ARG MODULE
#WORKDIR /app
#RUN addgroup --system banking && adduser --system --ingroup banking banking
#COPY --from=build /workspace/${MODULE}/target/*.jar /app/app.jar
#USER banking
#EXPOSE 8080
#ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
