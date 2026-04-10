# ----- 后端构建与部署 Dockerfile -----
FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /app

# 先复制 pom 和源码并编译（采用多阶段构建，使线上镜像最小化）
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# 正式运行环境
FROM eclipse-temurin:17-jre
WORKDIR /app

# ----- 【重要部署说明】 -----
# 持久化挂载目录：本项目运行产生的日志
VOLUME /app/logs

COPY --from=builder /app/target/*.jar app.jar

# ----- 【配置信息说明】 -----
# ！！不要把密钥硬编码在镜像里！！
# 以下敏感依赖变量在服务器上部署时需要通过 docker run -e 或
# docker-compose.yml 中的 environment 注射进来：
#
# ==== 数据库配置 ====
# - APP_DB_URL: 等待 Compose 注入 (例如 jdbc:mysql://core-mysql:3306/personal_web_ai...)
# - APP_DB_USER: root
# - APP_DB_PASSWORD: 你的密码
# ==== OpenAI 代理/大模型 配置 ====
# - OPENAI_API_KEY: 你的密钥
# - OPENAI_BASE_URL: 自建代理网关（例如 https://api.openai-proxy.com）
# - OPENAI_CHAT_MODEL: 模型（例如 gpt-4o-mini）
#
# ==== DashVector 配置 ====
# - DASHVECTOR_API_KEY: 你的阿里云向量检索密钥
# - DASHVECTOR_ENDPOINT: 你的集群访问 endpoint
# - DASHVECTOR_COLLECTION_NAME: 集合名称 (默认为 personal-portfolio)
# ------------------------------

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]