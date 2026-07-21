# Build stage — compiles the full Maven reactor
FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
COPY util/pom.xml util/pom.xml
COPY domain/pom.xml domain/pom.xml
COPY application/pom.xml application/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY presentation/pom.xml presentation/pom.xml
COPY web/pom.xml web/pom.xml
COPY . .
RUN mvn -B -P=local -pl web -am clean package -DskipTests

# Runtime stage — JRE only
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/web/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
