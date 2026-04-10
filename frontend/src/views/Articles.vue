<template>
  <div class="articles">
    <h2>Articles</h2>
    <div class="article-list">
      <div v-for="article in articles" :key="article.id" class="article-item" @click="$router.push('/articles/' + article.id)">
        <h3>{{ article.title }}</h3>
        <span class="date">{{ new Date(article.createdAt).toLocaleDateString() }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const articles = ref([])

onMounted(async () => {
  const res = await fetch('/api/articles')
  articles.value = await res.json()
})
</script>

<style scoped>
.articles { max-width: 800px; margin: 0 auto; padding: 2rem; }
.article-item { padding: 1rem; border-bottom: 1px solid #eee; cursor: pointer; }
.article-item:hover { background: #f8fafc; }
.date { color: #94a3b8; font-size: 0.9em; }
</style>
