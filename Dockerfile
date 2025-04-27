# Используем базовый образ с OpenJDK (Java 17)
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файлы Maven проекта (pom.xml) и исходники
# Сначала копируем pom.xml и зависимости, чтобы их кешировать
COPY pom.xml .
COPY src ./src

# Выполняем сборку проекта с помощью Maven
RUN apt-get update && apt-get install -y maven -y --no-install-recommends && \
    mvn clean package -DskipTests=true


# Запускаем приложение, когда контейнер стартует
# Spring Boot по умолчанию запускается на порту 8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/target/*.jar"]