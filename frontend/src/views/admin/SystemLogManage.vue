<template>
  <div class="manage-page">
    <div class="header">
      <h2>系统日志 (System Logs)</h2>
      <div class="header-actions">
        <div class="filter-group">
          <button
            v-for="lv in levels"
            :key="lv"
            :class="['level-btn', { active: currentLevel === lv }]"
            @click="changeLevel(lv)"
          >{{ lv }}</button>
        </div>
        <button class="btn danger" @click="clearLogs">🗑 清空日志</button>
      </div>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th style="width:60px">ID</th>
            <th style="width:70px">级别</th>
            <th style="width:100px">模块</th>
            <th>消息</th>
            <th style="width:160px">时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="logs.length === 0">
            <td colspan="5" style="text-align:center;color:#94a3b8;padding:2rem">暂无日志</td>
          </tr>
          <tr v-for="log in logs" :key="log.id">
            <td>{{ log.id }}</td>
            <td><span :class="['badge', log.level.toLowerCase()]">{{ log.level }}</span></td>
            <td>{{ log.module }}</td>
            <td>{{ log.message }}</td>
            <td>{{ formatTime(log.created_at) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="total > size">
      <button class="btn-sm" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ Math.ceil(total / size) }} 页（{{ total }} 条）</span>
      <button class="btn-sm" :disabled="page >= Math.ceil(total / size)" @click="changePage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const levels = ['ALL', 'INFO', 'WARN', 'ERROR']
const currentLevel = ref('ALL')
const logs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(50)

const adminToken = sessionStorage.getItem('adminToken')

const fetchLogs = async () => {
  const res = await fetch(
    `/api/admin/logs?page=${page.value}&size=${size.value}&level=${currentLevel.value}`,
    { headers: { 'X-Admin-Token': adminToken } }
  )
  if (res.ok) {
    const data = await res.json()
    logs.value = data.data || []
    total.value = data.total || 0
  }
}

const changeLevel = (lv) => {
  currentLevel.value = lv
  page.value = 1
  fetchLogs()
}

const changePage = (p) => {
  page.value = p
  fetchLogs()
}

const clearLogs = async () => {
  if (!confirm('确定要清空所有系统日志吗？此操作不可逆。')) return
  const res = await fetch('/api/admin/logs', {
    method: 'DELETE',
    headers: { 'X-Admin-Token': adminToken }
  })
  if (res.ok) {
    page.value = 1
    fetchLogs()
  } else {
    alert('清空失败')
  }
}

const formatTime = (ts) => {
  if (!ts) return ''
  return new Date(ts).toLocaleString('zh-CN', { hour12: false })
}

onMounted(fetchLogs)
</script>

<style scoped>
.manage-page { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
.header h2 { margin: 0; color: #0f172a; }
.header-actions { display: flex; align-items: center; gap: 12px; }

.filter-group { display: flex; background: #f1f5f9; border-radius: 6px; padding: 2px; gap: 2px; }
.level-btn { border: none; background: transparent; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #64748b; font-size: 13px; font-weight: 500; transition: all 0.2s; }
.level-btn.active { background: white; color: #0f172a; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }

.btn { padding: 8px 16px; border-radius: 6px; font-weight: 500; cursor: pointer; border: none; font-size: 13px; }
.btn.danger { background: #fef2f2; color: #ef4444; border: 1px solid #fca5a5; transition: all 0.2s; }
.btn.danger:hover { background: #ef4444; color: white; }

.table-container { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th, td { padding: 10px 14px; text-align: left; border-bottom: 1px solid #f1f5f9; }
th { background: #f8fafc; color: #475569; font-weight: 600; font-size: 13px; }
td { color: #334155; vertical-align: top; word-break: break-all; }

.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
}
.badge.info { background: #e0f2fe; color: #0284c7; }
.badge.warn { background: #fef9c3; color: #a16207; }
.badge.error { background: #fef2f2; color: #dc2626; }

.pagination { display: flex; align-items: center; gap: 16px; justify-content: center; margin-top: 1.5rem; color: #64748b; font-size: 13px; }
.btn-sm { padding: 5px 14px; border-radius: 4px; font-size: 12px; cursor: pointer; border: 1px solid #cbd5e1; background: white; color: #475569; }
.btn-sm:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
