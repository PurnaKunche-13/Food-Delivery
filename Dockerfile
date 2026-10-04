FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B clean verify
FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system foodexpress && useradd --system --gid foodexpress foodexpress
WORKDIR /app
RUN mkdir /app/data && chown -R foodexpress:foodexpress /app
COPY --from=build --chown=foodexpress:foodexpress /build/target/food-delivery.jar /app/app.jar
USER foodexpress
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
