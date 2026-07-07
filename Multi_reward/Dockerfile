# ---- 构建层:maven 打包(依赖单独一层,改代码不重下依赖) ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- 运行层:仅 JRE + jar ----
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV LOG_HOME=/app/logs
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
