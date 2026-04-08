<template>
  <main class="page">
    <section class="hero">
      <p class="kicker">SONG'S LAB</p>
      <h1>Personal Web Portfolio</h1>
      <p>聚合项目作品、技术文章与研究笔记，并通过 RAG 助手实现自然语言导览。</p>

      <div class="hero-actions">
        <a class="btn primary" href="#projects">浏览项目</a>
        <a class="btn ghost" href="#articles">阅读文章</a>
      </div>
    </section>

    <section id="projects" class="section">
      <div class="section-title">
        <h2>项目精选</h2>
        <p>围绕 AI 系统、联邦学习安全与工程效率的长期实践。</p>
      </div>
      <div class="cards">
        <article class="card">
          <h3>Sketched-AirDefense</h3>
          <p>面向 AirComp 联邦学习的拜占庭鲁棒聚合算法研究。</p>
        </article>
        <article class="card">
          <h3>VisionOps Toolkit</h3>
          <p>用于视觉模型部署与性能回归追踪的自动化工具链。</p>
        </article>
        <article class="card">
          <h3>Prompt Eval Bench</h3>
          <p>统一多模型提示词评估指标与数据集管理流程。</p>
        </article>
      </div>
    </section>

    <section id="articles" class="section">
      <div class="section-title">
        <h2>最新文章</h2>
        <p>记录系统设计、工程复盘与模型落地经验。</p>
      </div>
      <div class="cards">
        <article class="card">
          <h3>RAG 在个人网站中的分层设计</h3>
          <p>从文档入库到检索编排，拆解可维护的问答系统结构。</p>
        </article>
        <article class="card">
          <h3>Vue + SSE 打字机流体验优化</h3>
          <p>低成本打造稳定流式交互，规避前端缓存与粘包问题。</p>
        </article>
      </div>
    </section>

    <AiChatWidget api-url="/api/chat" history-url="/api/chat/history" />
  </main>
</template>

<script setup>
import AiChatWidget from './components/AiChatWidget.vue';
</script>

<style scoped>
.page {
  min-height: 100vh;
  margin: 0;
  background:
    radial-gradient(circle at 15% -5%, rgba(20, 184, 166, 0.22), transparent 24%),
    radial-gradient(circle at 92% 5%, rgba(249, 115, 22, 0.22), transparent 24%),
    linear-gradient(180deg, #fffbeb 0%, #f0fdfa 100%);
  color: #0f172a;
  font-family: 'IBM Plex Sans', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

.hero {
  max-width: 980px;
  margin: 0 auto;
  padding: 88px 20px 48px;
}

.kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.2em;
  color: #0f766e;
  font-weight: 700;
  text-transform: uppercase;
}

.hero h1 {
  margin: 10px 0 0;
  font-size: clamp(38px, 8vw, 66px);
  letter-spacing: -0.02em;
}

.hero p {
  margin-top: 14px;
  max-width: 760px;
  font-size: 19px;
  line-height: 1.6;
}

.hero-actions {
  margin-top: 22px;
  display: flex;
  gap: 12px;
}

.btn {
  border-radius: 999px;
  padding: 10px 16px;
  text-decoration: none;
  font-weight: 700;
  font-size: 14px;
}

.btn.primary {
  color: #0b1324;
  background: linear-gradient(120deg, #2dd4bf, #fde047);
}

.btn.ghost {
  color: #115e59;
  border: 1px solid rgba(15, 118, 110, 0.36);
}

.section {
  max-width: 980px;
  margin: 0 auto;
  padding: 20px 20px 36px;
}

.section-title h2 {
  margin: 0;
  font-size: 30px;
}

.section-title p {
  margin: 8px 0 0;
  color: #334155;
}

.cards {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.card {
  padding: 18px;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.card h3 {
  margin: 0;
  font-size: 18px;
}

.card p {
  margin: 8px 0 0;
  line-height: 1.5;
  color: #334155;
}

@media (max-width: 680px) {
  .hero {
    padding-top: 72px;
  }

  .hero-actions {
    flex-wrap: wrap;
  }
}
</style>
