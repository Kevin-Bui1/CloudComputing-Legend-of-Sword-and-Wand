FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java","-jar","/app/app.jar"]
