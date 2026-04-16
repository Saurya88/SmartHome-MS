FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew :device-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/device-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
