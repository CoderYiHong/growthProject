# ============================================================
# YiHong 个人成长作品集 — 后端 Dockerfile
# 构建: docker build -t growth-backend .
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/growth-1.0.0.jar app.jar
# 上传文件存储目录
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
