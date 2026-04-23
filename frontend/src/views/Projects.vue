<template>
  <div class="projects">
    <h2>Projects</h2>
    <div class="project-grid">
      <div v-for="project in projects" :key="project.id" class="project-card" @click="$router.push('/projects/' + project.id)">
        <div class="cover-wrap">
          <img v-if="project.coverImage" :src="project.coverImage" alt="cover" class="cover" />
          <div v-else class="cover-placeholder">{{ project.title.charAt(0) }}</div>
        </div>
        <div class="content">
          <h3>{{ project.title }}</h3>
          <span v-if="project.frameworks" class="framework">{{ project.frameworks }}</span>
          <p>{{ project.summary || (project.content ? project.content.replace(/#+ /g, '').slice(0, 100) + '…' : '') }}</p>
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
.project-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 2rem; margin-top: 1.5rem; }
.project-card { border: 1px solid #eee; border-radius: 12px; overflow: hidden; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; background: white; }
.project-card:hover { transform: translateY(-4px); box-shadow: 0 10px 24px rgba(0,0,0,0.1); }
.cover-wrap { width: 100%; height: 150px; overflow: hidden; }
.cover { width: 100%; height: 150px; object-fit: cover; display: block; }
.cover-placeholder { width: 100%; height: 150px; background: linear-gradient(135deg, #0ea5e9, #6366f1); display: flex; align-items: center; justify-content: center; font-size: 3rem; font-weight: 700; color: white; letter-spacing: 2px; }
.content { padding: 1.5rem; }
.content h3 { margin: 0 0 0.5rem 0; color: #0f172a; font-size: 1.1rem; }
.content p { margin: 0.5rem 0 0; color: #64748b; font-size: 0.9rem; line-height: 1.5; }
.framework { display: inline-block; padding: 3px 8px; background: #e0f2fe; color: #0284c7; border-radius: 4px; font-size: 0.8rem; margin-bottom: 0.5rem; }
</style>
