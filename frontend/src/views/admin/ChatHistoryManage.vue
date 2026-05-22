<template>
  <div class="manage-page">
    <div class="header">
      <h2>会话记录</h2>
    </div>

    <div class="stats-bar">
      <span>共 <strong>{{ total }}</strong> 个会话</span>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th style="width:200px">会话 ID</th>
            <th>首个提问</th>
            <th style="width:80px">轮次</th>
            <th style="width:160px">首次时间</th>
            <th style="width:160px">最近时间</th>
            <th style="width:100px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="sessions.length === 0">
            <td colspan="6" style="text-align:center;color:#94a3b8;padding:2rem">暂无会话记录</td>
          </tr>
          <template v-for="s in sessions" :key="s.session_id">
            <tr @click="toggleExpand(s.session_id)" class="clickable">
              <td><code>{{ s.session_id }}</code></td>
              <td>{{ truncate(s.first_question, 60) }}</td>
              <td>{{ s.turn_count }}</td>
              <td>{{ formatTime(s.first_created_at) }}</td>
              <td>{{ formatTime(s.last_created_at) }}</td>
              <td>
                <button class="btn-sm danger" @click.stop="confirmDelete(s.session_id)">删除</button>
              </td>
            </tr>
            <tr v-if="expandedId === s.session_id">
              <td colspan="6" style="padding:1rem;background:#f8fafc;">
                <div v-if="loadingDetail" style="text-align:center;padding:1rem;">加载中...</div>
                <div v-else-if="detail.length === 0" style="color:#94a3b8;">无对话记录</div>
                <div v-else class="conversation">
                  <div v-for="turn in detail" :key="turn.id || turn.createdAt" class="turn">
                    <div class="turn-q"><strong>Q:</strong> {{ turn.question }}</div>
                    <div class="turn-a"><strong>A:</strong> {{ turn.answer || '(空回答)' }}</div>
                    <div class="turn-meta">
                      <span v-if="turn.sources && turn.sources.length">
                        参考来源:
                        <a v-for="src in turn.sources" :key="src.url" :href="src.url" target="_blank" class="source-link">{{ src.title }}</a>
                      </span>
                      <span class="turn-time">{{ formatTime(turn.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="total > size">
      <button class="btn-sm" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ Math.ceil(total / size) }} 页（{{ total }} 条）</span>
      <button class="btn-sm" :disabled="page >= Math.ceil(total / size)" @click="changePage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const sessions = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const expandedId = ref(null)
const detail = ref([])
const loadingDetail = ref(false)

const token = () => sessionStorage.getItem('adminToken')
const headers = () => ({ 'Content-Type': 'application/json', 'X-Admin-Token': token() })

const fetchSessions = async () => {
  const res = await fetch(`/api/admin/chat-history/sessions?page=${page.value}&size=${size}`, { headers: headers() })
  const data = await res.json()
  sessions.value = data.data
  total.value = data.total
}

const changePage = (p) => { page.value = p; fetchSessions() }

const toggleExpand = async (sessionId) => {
  if (expandedId.value === sessionId) {
    expandedId.value = null
    return
  }
  expandedId.value = sessionId
  loadingDetail.value = true
  const res = await fetch(`/api/admin/chat-history/sessions/${encodeURIComponent(sessionId)}`, { headers: headers() })
  detail.value = await res.json()
  loadingDetail.value = false
}

const confirmDelete = async (sessionId) => {
  if (!confirm(`确认删除会话 [${sessionId}] 的全部记录？此操作不可恢复。`)) return
  await fetch(`/api/admin/chat-history/sessions/${encodeURIComponent(sessionId)}`, {
    method: 'DELETE', headers: headers()
  })
  expandedId.value = null
  fetchSessions()
}

const truncate = (text, len) => text && text.length > len ? text.substring(0, len) + '...' : (text || '')

const formatTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchSessions)
</script>

<style scoped>
.manage-page { max-width: 1200px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.header h2 { margin: 0; }
.stats-bar { margin-bottom: 1rem; color: #64748b; }
.table-container { background: white; border-radius: 8px; border: 1px solid #e2e8f0; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 12px 14px; background: #f8fafc; color: #475569; font-weight: 600; font-size: 0.85rem; border-bottom: 1px solid #e2e8f0; }
td { padding: 10px 14px; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem; }
tr.clickable { cursor: pointer; }
tr.clickable:hover td { background: #f1f5f9; }
code { background: #f1f5f9; padding: 2px 6px; border-radius: 3px; font-size: 0.85rem; }
.conversation { max-height: 400px; overflow-y: auto; }
.turn { margin-bottom: 1rem; padding-bottom: 1rem; border-bottom: 1px solid #e2e8f0; }
.turn:last-child { border-bottom: none; }
.turn-q { margin-bottom: 0.3rem; color: #1e40af; }
.turn-a { margin-bottom: 0.3rem; color: #334155; }
.turn-meta { display: flex; justify-content: space-between; font-size: 0.8rem; color: #94a3b8; }
.source-link { margin-left: 4px; color: #3b82f6; text-decoration: none; }
.source-link:hover { text-decoration: underline; }
.turn-time { margin-left: auto; }
.btn-sm { padding: 4px 10px; font-size: 0.8rem; border-radius: 4px; cursor: pointer; }
.btn-sm.danger { background: #fee2e2; color: #dc2626; border: 1px solid #fecaca; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1rem; }
</style>
