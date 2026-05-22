<template>
  <div class="manage-page">
    <div class="header">
      <h2>RAG 知识库管理</h2>
      <div class="header-actions">
        <button class="btn warning" @click="reindexAll"><SyncOutlined /> 批量重新索引</button>
        <button class="btn" @click="showIngestModal = true"><PlusOutlined /> 手动入库</button>
      </div>
    </div>

    <div class="stats-bar">
      <span>共 <strong>{{ total }}</strong> 条知识切块</span>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th style="width:60px">ID</th>
            <th style="width:120px">文档ID</th>
            <th style="width:70px">类型</th>
            <th style="width:150px">标题</th>
            <th>内容摘要</th>
            <th style="width:60px">切块</th>
            <th style="width:140px">入库时间</th>
            <th style="width:80px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="chunks.length === 0">
            <td colspan="8" style="text-align:center;color:#94a3b8;padding:2rem">暂无知识条目</td>
          </tr>
          <tr v-for="chunk in chunks" :key="chunk.id">
            <td>{{ chunk.id }}</td>
            <td><code>{{ chunk.docId }}</code></td>
            <td><span :class="['badge', chunk.docType]">{{ chunk.docType }}</span></td>
            <td>{{ chunk.title }}</td>
            <td class="content-preview">{{ truncate(chunk.content, 80) }}</td>
            <td>{{ chunk.chunkIndex }}</td>
            <td>{{ formatTime(chunk.createdAt) }}</td>
            <td>
              <button class="btn-sm danger" @click="confirmDeleteChunk(chunk)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="total > size">
      <button class="btn-sm" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ Math.ceil(total / size) }} 页（{{ total }} 条）</span>
      <button class="btn-sm" :disabled="page >= Math.ceil(total / size)" @click="changePage(page + 1)">下一页</button>
    </div>

    <!-- Re-index section -->
    <div class="section">
      <h3>重新索引文章/项目</h3>
      <div class="reindex-row">
        <select v-model="reindexType" class="field">
          <option value="article">文章 (Article)</option>
          <option value="project">项目 (Project)</option>
        </select>
        <input v-model.number="reindexId" type="number" placeholder="输入 ID" class="field" style="width:120px" />
        <button class="btn" @click="doReindex">执行重新索引</button>
      </div>
      <p class="hint">输入对应文章或项目的 ID，将其内容重新同步到向量库。</p>
    </div>

    <!-- Manual Ingest Modal -->
    <div v-if="showIngestModal" class="modal-overlay" @click.self="showIngestModal = false">
      <div class="modal-box">
        <h3>手动入库知识</h3>
        <div class="form-group">
          <label>类型</label>
          <select v-model="ingestForm.docType" class="field">
            <option value="project">project</option>
            <option value="article">article</option>
            <option value="manual">manual</option>
          </select>
        </div>
        <div class="form-group">
          <label>标题</label>
          <input v-model="ingestForm.title" class="field" placeholder="知识标题" />
        </div>
        <div class="form-group">
          <label>URL（可选）</label>
          <input v-model="ingestForm.url" class="field" placeholder="/path/to/resource" />
        </div>
        <div class="form-group">
          <label>摘要</label>
          <input v-model="ingestForm.summary" class="field" placeholder="简短摘要" />
        </div>
        <div class="form-group">
          <label>标签（逗号分隔）</label>
          <input v-model="ingestForm.tags" class="field" placeholder="AI, Security" />
        </div>
        <div class="form-group">
          <label>正文内容 (Markdown)</label>
          <textarea v-model="ingestForm.content" class="field" rows="8" placeholder="知识正文..."></textarea>
        </div>
        <div class="modal-buttons">
          <button class="btn" @click="doIngest">确认入库</button>
          <button class="btn cancel" @click="showIngestModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { SyncOutlined, PlusOutlined } from '@ant-design/icons-vue'

const chunks = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const showIngestModal = ref(false)
const reindexType = ref('article')
const reindexId = ref(null)

const ingestForm = ref({
  docType: 'manual',
  title: '',
  url: '',
  summary: '',
  tags: '',
  content: ''
})

const token = () => sessionStorage.getItem('adminToken')
const headers = () => ({ 'Content-Type': 'application/json', 'X-Admin-Token': token() })

const fetchChunks = async () => {
  const res = await fetch(`/api/admin/knowledge/chunks?page=${page.value}&size=${size}`, { headers: headers() })
  const data = await res.json()
  chunks.value = data.data
  total.value = data.total
}

const changePage = (p) => { page.value = p; fetchChunks() }

const confirmDeleteChunk = (chunk) => {
  if (!confirm(`确认删除文档 [${chunk.docId}] 的全部知识切块？\n将同时从 MySQL 镜像表和向量库中移除。`)) return
  fetch(`/api/admin/knowledge/chunks/${chunk.id}`, { method: 'DELETE', headers: headers() })
    .then(async (res) => {
      if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        throw new Error(data.message || '删除失败')
      }
    })
    .then(() => fetchChunks())
    .catch((error) => alert(error.message))
}

const doReindex = async () => {
  if (!reindexId.value) return alert('请输入 ID')
  const res = await fetch(`/api/admin/knowledge/reindex/${reindexType.value}/${reindexId.value}`, {
    method: 'POST', headers: headers()
  })
  const data = await res.json()
  alert(data.ok ? `已重新索引: ${data.title}` : data.error)
  fetchChunks()
}

const reindexAll = async () => {
  if (!confirm('确认批量重新索引所有文章和项目？此操作可能耗时较长。')) return
  const res = await fetch('/api/admin/knowledge/reindex-all', { method: 'POST', headers: headers() })
  const data = await res.json()
  alert(`批量重新索引完成，共 ${data.total} 篇`)
  fetchChunks()
}

const doIngest = async () => {
  const f = ingestForm.value
  if (!f.title || !f.content) return alert('标题和内容不能为空')
  const tags = f.tags ? f.tags.split(',').map(t => t.trim()).filter(Boolean) : []
  const body = {
    type: f.docType,
    title: f.title,
    url: f.url || '/knowledge/manual',
    summary: f.summary,
    content: f.content,
    tags
  }
  const res = await fetch('/api/admin/knowledge/ingest', {
    method: 'POST', headers: headers(), body: JSON.stringify(body)
  })
  const data = await res.json()
  alert(`入库完成，生成 ${data.chunks} 个切块`)
  showIngestModal.value = false
  ingestForm.value = { docType: 'manual', title: '', url: '', summary: '', tags: '', content: '' }
  fetchChunks()
}

const truncate = (text, len) => text && text.length > len ? text.substring(0, len) + '...' : (text || '')

const formatTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchChunks)
</script>

<style scoped>
.manage-page { max-width: 1200px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.header h2 { margin: 0; }
.header-actions { display: flex; gap: 0.5rem; }
.stats-bar { margin-bottom: 1rem; color: #64748b; }
.content-preview { max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
code { background: #f1f5f9; padding: 2px 6px; border-radius: 3px; font-size: 0.85rem; }
.badge { padding: 2px 8px; border-radius: 10px; font-size: 0.8rem; font-weight: 500; }
.badge.article { background: #dbeafe; color: #1e40af; }
.badge.project { background: #dcfce7; color: #166534; }
.badge.manual { background: #fef3c7; color: #92400e; }
.section { margin-top: 2rem; padding: 1.5rem; background: white; border-radius: 8px; border: 1px solid #e2e8f0; }
.section h3 { margin-top: 0; }
.reindex-row { display: flex; gap: 0.5rem; align-items: center; }
.field { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.9rem; }
.hint { color: #94a3b8; font-size: 0.85rem; margin-top: 0.5rem; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-box { background: white; border-radius: 12px; padding: 2rem; width: 600px; max-height: 80vh; overflow-y: auto; }
.modal-box h3 { margin-top: 0; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.3rem; font-weight: 500; }
.form-group .field { width: 100%; box-sizing: border-box; }
textarea.field { resize: vertical; }
.modal-buttons { display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; }
.btn { padding: 8px 18px; background: #1e293b; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 0.9rem; }
.btn:hover { background: #334155; }
.btn.warning { background: #f59e0b; color: white; }
.btn.warning:hover { background: #d97706; }
.btn.cancel { background: #e2e8f0; color: #334155; }
.btn-sm { padding: 4px 10px; font-size: 0.8rem; border-radius: 4px; cursor: pointer; }
.btn-sm.danger { background: #fee2e2; color: #dc2626; border: 1px solid #fecaca; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1rem; }
.table-container { background: white; border-radius: 8px; border: 1px solid #e2e8f0; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 12px 14px; background: #f8fafc; color: #475569; font-weight: 600; font-size: 0.85rem; border-bottom: 1px solid #e2e8f0; }
td { padding: 10px 14px; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem; }
tr:hover td { background: #f8fafc; }
</style>
