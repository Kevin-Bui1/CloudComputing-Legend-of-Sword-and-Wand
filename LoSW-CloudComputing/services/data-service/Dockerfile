FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 5003
ENTRYPOINT ["java","-jar","/app/app.jar"]
