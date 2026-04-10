# ----- 前端构建与部署 Dockerfile -----
FROM node:20-alpine AS builder
WORKDIR /app

# 1. 复制依赖清单，安装前端依赖
COPY package*.json ./
RUN npm install

# 2. 复制源码至工作目录，进行打包构建
COPY . .
RUN npm run build


# 3. 使用轻量化的 Nginx 提供服务
FROM nginx:alpine

# 将第一阶段的构建产物(通常是 /dist) 拷贝到 Nginx 默认静态站点目录下
COPY --from=builder /app/dist /usr/share/nginx/html

# ----- 【配置信息说明】 -----
# 前端通常属于纯静态资源，所有的环境变量大多在构建阶段固定。
# 为了避免跨域并且不需要把后端 API 直接暴露给公网，
# 服务器部署时我们一般利用下面提供的 nginx.conf 来配置反向代理：
# 将访问本服务器 /api/* 的请求路由转发给名为 backend 容器的 8080 端口。
# ------------------------------

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]