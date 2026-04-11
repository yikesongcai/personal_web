# ---- 第一阶段：Maven 编译 ----
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build-stage
WORKDIR /app
COPY pom.xml .
# 预先下载依赖以利用 Docker 缓存机制
RUN mvn dependency:go-offline -B
COPY src ./src
# 执行打包命令
RUN mvn clean package -DskipTests

# ---- 第二阶段：JRE 运行 ----
FROM eclipse-temurin:17-jre-alpine AS production-stage
WORKDIR /app
COPY --from=build-stage /app/target/*.jar app.jar
VOLUME /app/logs
VOLUME /app/uploads

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
