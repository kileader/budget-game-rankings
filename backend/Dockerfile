FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY backend/ .
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd -r -u 1001 -g root appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser app.jar
USER appuser
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
