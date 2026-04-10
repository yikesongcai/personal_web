<template>
  <div class="projects">
    <h2>Projects</h2>
    <div class="project-grid">
      <div v-for="project in projects" :key="project.id" class="project-card" @click="$router.push('/projects/' + project.id)">
        <img :src="project.coverUrl || 'https://via.placeholder.com/300x150'" alt="cover" class="cover" />
        <div class="content">
          <h3>{{ project.title }}</h3>
          <span class="framework">{{ project.framework }}</span>
          <p>{{ project.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const projects = ref([])

onMounted(async () => {
  const res = await fetch('/api/projects')
  projects.value = await res.json()
})
</script>

<style scoped>
.projects { max-width: 1200px; margin: 0 auto; padding: 2rem; }
.project-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 2rem; }
.project-card { border: 1px solid #eee; border-radius: 8px; overflow: hidden; cursor: pointer; transition: transform 0.2s; }
.project-card:hover { transform: translateY(-4px); box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); }
.cover { width: 100%; height: 150px; object-fit: cover; }
.content { padding: 1.5rem; }
.content h3 { margin: 0 0 0.5rem 0; }
.framework { display: inline-block; padding: 4px 8px; background: #e0f2fe; color: #0284c7; border-radius: 4px; font-size: 0.8rem; margin-bottom: 1rem; }
</style>
