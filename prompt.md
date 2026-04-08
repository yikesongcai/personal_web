# 角色与背景
你是一位精通 Spring Boot 3、Vue 3 以及大模型 AI 应用开发的资深架构师。我们正在开发一个包含项目作品和文章展示的个人网站。
现在，我们需要在系统中引入基于 RAG (检索增强生成) 技术的 AI 智能问答助手，允许用户通过自然语言检索和了解网站内的作品与文章。

# 核心技术栈架构
- **后端框架**：Spring Boot 3 + Spring AI。
- **大模型接口**：使用标准的 OpenAI API 格式（支持配置不同的 Base URL 以接入各类兼容模型）。
- **向量数据库 (Vector Store)**：**采用 Pinecone (Serverless)**。要求提供使用 Spring AI 官方 `PineconeVectorStore` 的集成配置。
- **前端交互**：Vue 3 (Composition API) + SSE (Server-Sent Events) 实现流式打字机效果的对话界面。

# AI 核心模块设计需求
请帮我设计并生成以下核心环节的代码方案：

1. **依赖与配置 (Configuration)**：
   - 提供 `pom.xml` 中 Spring AI 结合 Pinecone 的必要依赖。
   - 提供 `application.yml` 中关于 Pinecone API Key、Index Name 等配置项的模板。

2. **知识入库 (Document Ingestion) 模块**：
   - 编写一个 Service，演示如何将结构化数据转为文本并入库。
   - **Mock 数据要求**：请在代码中构造一条逼真的 Mock 测试数据进行入库演示。例如，项目名为 `Sketched-AirDefense`，内容包含：“专注于空中计算 (AirComp) 联邦学习中的拜占庭攻击防御机制，偏向算法优化方向，引入了 Gradient Sketching 与自监督预测技术……” 等硬核描述。
   - 使用 Spring AI 的 `TokenTextSplitter` 对长文本进行切分。
   - 调用 EmbeddingModel 生成向量，并存入 Pinecone VectorStore。

3. **检索增强问答 (RAG Chat) 模块**：
   - 设计一个 `/api/chat` 的 RESTful 接口。
   - 接收用户提问，通过 `PineconeVectorStore` 进行相似度检索 (Similarity Search)，获取 Top-K 相关的上下文片段。
   - 组装 System Prompt（例如：“你是一个专业的个人网站智能导览助手。请严格基于以下提供的网站内容回答用户问题。内容：{context}”）。
   - 调用 ChatModel 处理请求，并返回 `Flux<String>` 实现 SSE 流式响应。

4. **前端悬浮聊天组件 (Vue 3 Chat Widget)**：
   - 编写一个可复用的 Vue 3 组件，固定在网页右下角。
   - 实现对接后端 SSE 接口 (`fetch` API 处理流式响应) 的逻辑，能够平滑渲染 AI 的打字机输出效果。

# 任务输出要求
请按模块化顺序，清晰地给出你的架构配置、后端的关键 Java 代码（请重点确保 Spring AI 与 Pinecone 交互的准确性）以及前端的 Vue 3 组件代码。包含必要的中文注释。