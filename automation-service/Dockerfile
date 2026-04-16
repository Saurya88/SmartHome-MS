FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew :automation-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/automation-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
