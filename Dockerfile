FROM eclipse-temurin:17-jre

WORKDIR /app

COPY dist/food-delivery.jar app.jar

EXPOSE 8080

VOLUME ["/app/data"]

ENTRYPOINT ["java", "-jar", "app.jar"]
