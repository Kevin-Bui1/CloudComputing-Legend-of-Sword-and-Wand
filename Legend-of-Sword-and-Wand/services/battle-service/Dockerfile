FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 5001
ENTRYPOINT ["java","-jar","/app/app.jar"]
