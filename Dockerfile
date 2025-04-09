FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-ubi9-minimal
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/*.jar
EXPOSE 8080
# Копируем CSV-файлы
COPY data/ /app/data/
ENTRYPOINT ["java", "-jar", "/app/*.jar"]