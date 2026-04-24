<template>
  <main class="page">
    <section class="hero">
      <p class="kicker">{{ config.hero_kicker || "SONG'S LAB" }}</p>
      <h1>{{ config.hero_title || 'Personal Web Portfolio' }}</h1>
      <p>{{ config.hero_subtitle || '聚合项目作品、技术文章与研究笔记，并通过 RAG 助手实现自然语言导览。' }}</p>

      <div class="hero-actions">
        <router-link class="btn primary" to="/projects">浏览项目</router-link>
        <router-link class="btn ghost" to="/articles">阅读文章</router-link>
      </div>
    </section>

    <section id="projects" class="section">
      <div class="section-title">
        <h2>{{ config.projects_section_title || '项目精选' }}</h2>
        <p>{{ config.projects_section_subtitle || '' }}</p>
      </div>
      <div class="cards">
        <article class="card" v-for="project in projects" :key="project.id" @click="$router.push('/projects/' + project.id)">
          <h3>{{ project.title }}</h3>
          <p v-if="project.frameworks" class="tag">{{ project.frameworks }}</p>
          <p>{{ project.summary || (project.content ? project.content.replace(/#+ /g, '').slice(0, 80) + '…' : '暂无描述') }}</p>
        </article>
      </div>
    </section>

    <section id="articles" class="section">
      <div class="section-title">
        <h2>{{ config.articles_section_title || '最新文章' }}</h2>
        <p>{{ config.articles_section_subtitle || '' }}</p>
      </div>
      <div class="cards">
        <article class="card" v-for="article in articles" :key="article.id" @click="$router.push('/articles/' + article.id)">
          <h3>{{ article.title }}</h3>
          <p>{{ new Date(article.createdAt).toLocaleDateString() }}</p>
        </article>
      </div>
    </section>

  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const projects = ref([])
const articles = ref([])
const config = ref({})

onMounted(async () => {
  try {
    const res = await fetch('/api/home')
    if (res.ok) {
      const data = await res.json()
      projects.value = data.projects || []
      articles.value = data.articles || []
      config.value = data.config || {}
    }
  } catch(e) {
    console.error(e)
  }
})
</script>

<style scoped>
.page {
  min-height: calc(100vh - 60px);
  padding-bottom: 40px;
}

.hero {
  max-width: 980px;
  margin: 0 auto;
  padding: 68px 20px 48px;
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
  cursor: pointer;
}

.btn.primary {
  color: #0b1324;
  background: linear-gradient(120deg, #2dd4bf, #fde047);
}

.btn.ghost {
  color: #115e59;
  border: 1px solid rgba(15, 118, 110, 0.36);
  background: transparent;
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
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12);
}

.card h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.card p {
  margin: 8px 0 0;
  line-height: 1.5;
  color: #334155;
  font-size: 14px;
  flex-grow: 1;
}

.tag {
  display: inline-block;
  padding: 2px 6px;
  background: #e0f2fe;
  color: #0284c7;
  border-radius: 4px;
  font-size: 0.75rem;
  margin: 6px 0 0;
  width: auto;
  align-self: flex-start;
  flex-grow: 0 !important;
}

@media (max-width: 680px) {
  .hero { padding-top: 52px; }
  .hero-actions { flex-wrap: wrap; }
}
</style>
