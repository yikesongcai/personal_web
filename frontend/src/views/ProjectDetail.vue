<template>
  <div class="detail" v-if="item">
    <div class="header">
      <h2>{{ item.title }}</h2>
      <div class="meta" v-if="item.framework">
        <span class="tag">{{ item.framework }}</span>
        <a v-if="item.projectUrl" :href="item.projectUrl" target="_blank">View Live</a>
        <a v-if="item.githubUrl" :href="item.githubUrl" target="_blank">GitHub</a>
      </div>
      <div class="meta" v-else>
        <span>{{ new Date(item.createdAt).toLocaleDateString() }}</span>
      </div>
    </div>
    
    <div class="markdown-body" v-html="renderedContent"></div>
  </div>
  <div v-else class="loading">Loading...</div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const item = ref(null)

const type = route.path.startsWith('/projects') ? 'projects' : 'articles'

const fetchItem = async () => {
  try {
    const res = await fetch(`/api/${type}/${route.params.id}`)
    if (res.ok) {
      item.value = await res.json()
    }
  } catch (err) {
    console.error(err)
  }
}

const renderedContent = computed(() => {
  if (!item.value) return ''
  // For backwards compatibility or different content sources
  const content = item.value.content || item.value.readme || ''
  return DOMPurify.sanitize(marked(content))
})

onMounted(fetchItem)
watch(() => route.params.id, fetchItem)
</script>

<style scoped>
.detail { max-width: 800px; margin: 0 auto; padding: 2rem; }
.header { margin-bottom: 2rem; border-bottom: 1px solid #eee; padding-bottom: 1rem; }
.header h2 { font-size: 2.5rem; margin-top: 0; }
.meta { display: flex; gap: 1rem; align-items: center; color: #64748b; }
.tag { padding: 4px 8px; background: #e0f2fe; color: #0284c7; border-radius: 4px; font-size: 0.9rem; }
.loading { text-align: center; padding: 4rem; color: #94a3b8; }

/* Markdown Styles */
.markdown-body { line-height: 1.6; font-size: 1.1rem; color: #334155; }
:deep(.markdown-body h1), :deep(.markdown-body h2), :deep(.markdown-body h3) { color: #0f172a; margin-top: 2rem; }
:deep(.markdown-body a) { color: #0ea5e9; text-decoration: none; }
:deep(.markdown-body a:hover) { text-decoration: underline; }
:deep(.markdown-body pre) { background: #1e293b; color: #f8fafc; padding: 1rem; border-radius: 8px; overflow-x: auto; }
:deep(.markdown-body code) { background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; font-size: 0.9em; }
:deep(.markdown-body pre code) { background: none; padding: 0; color: inherit; }
:deep(.markdown-body img) { max-width: 100%; border-radius: 8px; }
:deep(.markdown-body blockquote) { border-left: 4px solid #cbd5e1; margin: 0; padding-left: 1rem; color: #64748b; }
</style>
