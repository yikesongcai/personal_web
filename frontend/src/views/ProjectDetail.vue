<template>
  <div class="detail" v-if="item">
    <!-- Cover Image -->
    <div class="cover-hero" v-if="item.coverImage">
      <img :src="item.coverImage" :alt="item.title" class="cover-img" />
    </div>

    <div class="header">
      <h2>{{ item.title }}</h2>
      <p v-if="item.summary" class="summary">{{ item.summary }}</p>

      <div class="tags" v-if="item.frameworks">
        <span v-for="fw in item.frameworks.split(',')" :key="fw" class="tag">{{ fw.trim() }}</span>
      </div>

      <div class="meta-links">
        <a v-if="item.onlineUrl" :href="item.onlineUrl" target="_blank" class="link-btn live">
          🌐 在线演示
        </a>
        <a v-if="item.githubUrl" :href="item.githubUrl" target="_blank" class="link-btn github">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/></svg>
          GitHub
        </a>
        <span v-if="item.githubUrl === null || item.githubUrl === ''" class="closed-source">🔒 非开源项目</span>
        <span class="date">{{ new Date(item.createdAt).toLocaleDateString('zh-CN') }}</span>
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
  const content = item.value.content || item.value.readme || ''
  return DOMPurify.sanitize(marked(content))
})

onMounted(fetchItem)
watch(() => route.params.id, fetchItem)
</script>

<style scoped>
.detail { max-width: 860px; margin: 0 auto; padding: 2rem; }

/* Cover */
.cover-hero { width: 100%; max-height: 340px; overflow: hidden; border-radius: 16px; margin-bottom: 2rem; }
.cover-img { width: 100%; height: 340px; object-fit: cover; display: block; }

/* Header */
.header { margin-bottom: 2rem; border-bottom: 1px solid #e2e8f0; padding-bottom: 1.5rem; }
.header h2 { font-size: 2.2rem; margin: 0 0 0.5rem 0; color: #0f172a; line-height: 1.3; }
.summary { margin: 0 0 1rem 0; font-size: 1.1rem; color: #475569; line-height: 1.6; }

/* Framework tags */
.tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 1rem; }
.tag { padding: 4px 10px; background: #e0f2fe; color: #0284c7; border-radius: 6px; font-size: 0.85rem; font-weight: 500; }

/* Links row */
.meta-links { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.link-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 16px; border-radius: 8px; font-size: 0.9rem; font-weight: 500;
  text-decoration: none; transition: all 0.2s;
}
.link-btn.live { background: #0ea5e9; color: white; }
.link-btn.live:hover { background: #0284c7; }
.link-btn.github { background: #1e293b; color: white; }
.link-btn.github:hover { background: #0f172a; }
.closed-source { color: #94a3b8; font-size: 0.85rem; padding: 7px 12px; background: #f1f5f9; border-radius: 8px; }
.date { color: #94a3b8; font-size: 0.85rem; margin-left: auto; }

/* Markdown */
.loading { text-align: center; padding: 4rem; color: #94a3b8; }
.markdown-body { line-height: 1.75; font-size: 1.05rem; color: #334155; }
:deep(.markdown-body h1), :deep(.markdown-body h2), :deep(.markdown-body h3) { color: #0f172a; margin-top: 2rem; }
:deep(.markdown-body a) { color: #0ea5e9; text-decoration: none; }
:deep(.markdown-body a:hover) { text-decoration: underline; }
:deep(.markdown-body pre) { background: #1e293b; color: #f8fafc; padding: 1.2rem; border-radius: 10px; overflow-x: auto; }
:deep(.markdown-body code) { background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; font-size: 0.9em; }
:deep(.markdown-body pre code) { background: none; padding: 0; color: inherit; }
:deep(.markdown-body img) { max-width: 100%; border-radius: 8px; }
:deep(.markdown-body blockquote) { border-left: 4px solid #cbd5e1; margin: 0; padding-left: 1rem; color: #64748b; }
:deep(.markdown-body table) { width: 100%; border-collapse: collapse; }
:deep(.markdown-body th), :deep(.markdown-body td) { border: 1px solid #e2e8f0; padding: 8px 12px; }
:deep(.markdown-body th) { background: #f8fafc; }
</style>

