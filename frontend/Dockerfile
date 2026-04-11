# ---- 第一阶段：Node.js 编译 ----
FROM node:18-alpine as build-stage
WORKDIR /app
COPY package*.json ./
# 使用国内淘宝镜像加速 npm 安装
RUN npm config set registry https://registry.npmmirror.com && npm install
# 复制前端代码
COPY . .
# 执行打包命令
RUN npm run build

# ---- 第二阶段：Nginx 运行 ----
FROM nginx:alpine as production-stage
# 把第一阶段编译好的 dist 目录拷过来
COPY --from=build-stage /app/dist /usr/share/nginx/html
# 暴露前端默认端口
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
