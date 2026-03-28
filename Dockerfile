FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY *-service/target/*.jar service.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","service.jar"]
