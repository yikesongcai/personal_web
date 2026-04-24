<template>
  <div class="manage-page">
    <div class="header">
      <h2>首页内容管理 (Homepage)</h2>
      <button class="btn primary" @click="saveAll" :disabled="saving">{{ saving ? '保存中…' : '💾 保存所有设置' }}</button>
    </div>

    <!-- Hero Section -->
    <div class="section-card">
      <h3 class="section-label">🏠 Hero 区域</h3>
      <div class="form-grid">
        <div class="form-group">
          <label>顶部小标题 (Kicker)</label>
          <input v-model="config.hero_kicker" placeholder="SONG'S LAB" />
        </div>
        <div class="form-group">
          <label>主标题 (Title)</label>
          <input v-model="config.hero_title" placeholder="Personal Web Portfolio" />
        </div>
        <div class="form-group full">
          <label>副标题 (Subtitle)</label>
          <textarea v-model="config.hero_subtitle" rows="2"></textarea>
        </div>
      </div>
    </div>

    <!-- Projects Section -->
    <div class="section-card">
      <h3 class="section-label">📦 项目精选区域</h3>
      <div class="form-grid">
        <div class="form-group">
          <label>区块标题</label>
          <input v-model="config.projects_section_title" placeholder="项目精选" />
        </div>
        <div class="form-group full">
          <label>区块副标题</label>
          <input v-model="config.projects_section_subtitle" />
        </div>
      </div>
      <p class="hint">💡 首页展示「已设为精选」的项目；未设精选时展示最新 3 条。进入<router-link to="/admin/projects">项目管理</router-link>设置精选与排序。</p>

      <div class="mini-list">
        <div v-for="p in featuredProjects" :key="p.id" class="mini-item">
          <span class="star">⭐</span>
          <span class="title">{{ p.title }}</span>
          <span class="tag" v-if="p.frameworks">{{ p.frameworks }}</span>
          <span class="order">排序: {{ p.sortOrder }}</span>
        </div>
        <div v-if="featuredProjects.length === 0" class="empty">暂无精选项目，将展示最新 3 条。</div>
      </div>
    </div>

    <!-- Articles Section -->
    <div class="section-card">
      <h3 class="section-label">📝 文章精选区域</h3>
      <div class="form-grid">
        <div class="form-group">
          <label>区块标题</label>
          <input v-model="config.articles_section_title" placeholder="最新文章" />
        </div>
        <div class="form-group full">
          <label>区块副标题</label>
          <input v-model="config.articles_section_subtitle" />
        </div>
      </div>
      <p class="hint">💡 首页展示「已设为精选」的文章；未设精选时展示最新 3 条。进入<router-link to="/admin/articles">文章管理</router-link>设置精选与排序。</p>

      <div class="mini-list">
        <div v-for="a in featuredArticles" :key="a.id" class="mini-item">
          <span class="star">⭐</span>
          <span class="title">{{ a.title }}</span>
          <span class="tag" v-if="a.tags">{{ a.tags }}</span>
          <span class="order">排序: {{ a.sortOrder }}</span>
        </div>
        <div v-if="featuredArticles.length === 0" class="empty">暂无精选文章，将展示最新 3 条。</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const adminToken = sessionStorage.getItem('adminToken')
const config = ref({
  hero_kicker: '',
  hero_title: '',
  hero_subtitle: '',
  projects_section_title: '',
  projects_section_subtitle: '',
  articles_section_title: '',
  articles_section_subtitle: ''
})
const featuredProjects = ref([])
const featuredArticles = ref([])
const saving = ref(false)

const fetchAll = async () => {
  // Load site config
  const cRes = await fetch('/api/admin/site-config', { headers: { 'X-Admin-Token': adminToken } })
  if (cRes.ok) config.value = { ...config.value, ...(await cRes.json()) }

  // Load home data for featured preview
  const hRes = await fetch('/api/home')
  if (hRes.ok) {
    const data = await hRes.json()
    featuredProjects.value = data.projects || []
    featuredArticles.value = data.articles || []
  }
}

const saveAll = async () => {
  saving.value = true
  const res = await fetch('/api/admin/site-config', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'X-Admin-Token': adminToken },
    body: JSON.stringify(config.value)
  })
  saving.value = false
  if (res.ok) {
    alert('保存成功！')
  } else {
    alert('保存失败')
  }
}

onMounted(fetchAll)
</script>

<style scoped>
.manage-page { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.header h2 { margin: 0; color: #0f172a; }
.btn { padding: 8px 20px; border-radius: 6px; font-weight: 500; cursor: pointer; border: none; font-size: 14px; }
.btn.primary { background: #0ea5e9; color: white; transition: background 0.2s; }
.btn.primary:hover { background: #0284c7; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }

.section-card { border: 1px solid #e2e8f0; border-radius: 10px; padding: 1.5rem; margin-bottom: 1.5rem; }
.section-label { margin: 0 0 1.2rem 0; font-size: 1rem; color: #334155; font-weight: 600; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.form-group { display: flex; flex-direction: column; gap: 4px; }
.form-group.full { grid-column: 1 / -1; }
.form-group label { font-size: 13px; font-weight: 500; color: #475569; }
.form-group input, .form-group textarea { padding: 8px 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; font-family: inherit; }
.form-group input:focus, .form-group textarea:focus { outline: none; border-color: #0ea5e9; box-shadow: 0 0 0 2px rgba(14,165,233,0.1); }

.hint { margin: 1rem 0 0.8rem; font-size: 13px; color: #64748b; background: #f8fafc; padding: 8px 12px; border-radius: 6px; }
.hint a { color: #0ea5e9; text-decoration: none; }
.hint a:hover { text-decoration: underline; }

.mini-list { display: flex; flex-direction: column; gap: 6px; margin-top: 8px; }
.mini-item { display: flex; align-items: center; gap: 10px; padding: 8px 12px; background: #f8fafc; border-radius: 6px; font-size: 13px; }
.star { font-size: 14px; }
.title { font-weight: 500; color: #0f172a; flex: 1; }
.tag { background: #e0f2fe; color: #0284c7; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.order { color: #94a3b8; font-size: 12px; }
.empty { color: #94a3b8; font-size: 13px; padding: 8px 12px; font-style: italic; }
</style>
