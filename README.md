# Personal Web AI Assistant

本仓库已按需求完成一个最小可用的 RAG 智能问答实现，包含：
- Spring Boot 3 + Spring AI 后端
- `/api/chat` SSE 流式问答接口（`sources/token/done` 事件）
- Mock 项目知识入库（Sketched-AirDefense）
- 文章/项目批量入库接口
- 对话历史持久化（本地 H2 文件库）
- Vue 3 右下角悬浮聊天组件

说明：当运行环境未提供可用的 `VectorStore` Bean 时，系统会自动回退到本地 `SimpleVectorStore`，保证接口可启动联调。

## 1. 后端配置

文件：`backend/src/main/resources/application.yml`

建议通过环境变量覆盖以下配置：

- `OPENAI_API_KEY`
- `OPENAI_BASE_URL`（可改为任意 OpenAI 兼容网关）
- `OPENAI_CHAT_MODEL`
- `OPENAI_EMBEDDING_MODEL`
- `DASHVECTOR_API_KEY`
- `DASHVECTOR_ENDPOINT`
- `DASHVECTOR_COLLECTION_NAME`
- `APP_DB_URL`（可选，默认本地文件 H2）
- `APP_DB_USER`
- `APP_DB_PASSWORD`

## 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

启动时会自动执行一次 Mock 数据入库。

## 3. 聊天接口测试

```bash
curl -N -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"sessionId":"demo-session-1","question":"Sketched-AirDefense 的核心创新是什么？"}'
```

历史记录查询：

```bash
curl http://localhost:8080/api/chat/history/demo-session-1
```

批量入库：

```bash
curl -X POST http://localhost:8080/api/knowledge/ingest/batch \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "type": "project",
        "title": "Sketched-AirDefense",
        "url": "/projects/sketched-airdefense",
        "summary": "AirComp 联邦学习拜占庭防御",
        "content": "项目正文...",
        "tags": ["AirComp", "FL", "Security"]
      },
      {
        "type": "article",
        "title": "RAG 架构实践",
        "url": "/articles/rag-architecture",
        "summary": "个人网站的 RAG 分层设计",
        "content": "文章正文...",
        "tags": ["RAG", "Spring AI"]
      }
    ]
  }'
```

## 4. 前端组件接入

核心组件：`frontend/src/components/AiChatWidget.vue`

在任意页面中引入并使用：

```vue
<AiChatWidget api-url="/api/chat" history-url="/api/chat/history" />
```

前端开发代理已配置在 `frontend/vite.config.js`：

- `/api` -> `http://localhost:8080`

## 5. 关键文件说明

- `backend/pom.xml`：后端依赖（WebFlux/JDBC/H2）
- `backend/src/main/java/com/personalweb/ai/service/KnowledgeIngestionService.java`：知识切分、向量化与入库
- `backend/src/main/java/com/personalweb/ai/controller/KnowledgeController.java`：批量入库接口
- `backend/src/main/java/com/personalweb/ai/service/RagChatService.java`：相似度检索 + Prompt 组装 + 结构化 SSE 事件
- `backend/src/main/java/com/personalweb/ai/controller/ChatController.java`：`/api/chat` SSE 接口
- `backend/src/main/java/com/personalweb/ai/service/ChatHistoryService.java`：对话历史持久化与查询
- `frontend/src/components/AiChatWidget.vue`：悬浮聊天组件与流式打字机渲染
