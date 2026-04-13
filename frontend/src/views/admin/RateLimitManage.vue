<template>
  <div class="manage-page">
    <div class="header">
      <h2>Chat 接口速率白名单管理</h2>
      <button class="btn primary" @click="openForm()">+ 添加 IP</button>
    </div>
    
    <div class="alert info">
      <p>⚠️ 注意：Chat 接口默认限制每个 IP 频率为 <strong>10次/分钟</strong>。白名单中的 IP 不受此限制。添加后最多等一分钟生效。</p>
    </div>

    <!-- Table -->
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th><th>IP 地址</th><th>备注</th><th>添加时间</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>{{ item.id }}</td>
            <td><code>{{ item.ip }}</code></td>
            <td>{{ item.remark }}</td>
            <td>{{ new Date(item.created_at).toLocaleString() }}</td>
            <td class="actions">
              <button class="btn-sm danger" @click="deleteItem(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal Form -->
    <div class="modal-overlay" v-if="showForm" @click.self="showForm = false">
      <div class="modal">
        <h3>添加白名单 IP</h3>
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label>IP 地址</label>
            <input v-model="formData.ip" required placeholder="例如: 192.168.1.100" />
          </div>
          <div class="form-group">
            <label>备注说明</label>
            <input v-model="formData.remark" placeholder="如: 办公室电脑" />
          </div>
          <div class="form-actions">
            <button type="button" class="btn ghost" @click="showForm = false">取消</button>
            <button type="submit" class="btn primary">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const items = ref([])
const showForm = ref(false)
const formData = ref({})

const fetchItems = async () => {
  const res = await fetch('/api/admin/rate-limit/whitelist', {
    headers: { 'X-Admin-Token': sessionStorage.getItem('adminToken') }
  })
  if (res.ok) items.value = await res.json()
}

const openForm = () => {
  formData.value = { ip: '', remark: '' }
  showForm.value = true
}

const saveItem = async () => {
  const res = await fetch('/api/admin/rate-limit/whitelist', {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'X-Admin-Token': sessionStorage.getItem('adminToken')
    },
    body: JSON.stringify(formData.value)
  })
  if (res.ok) {
    showForm.value = false
    fetchItems()
  } else {
    alert('保存失败，可能 IP 已存在')
  }
}

const deleteItem = async (id) => {
  if (!confirm('确定要删除该 IP 吗？')) return
  const res = await fetch(`/api/admin/rate-limit/whitelist/${id}`, { 
    method: 'DELETE',
    headers: { 'X-Admin-Token': sessionStorage.getItem('adminToken') }
  })
  if (res.ok) fetchItems()
}

onMounted(fetchItems)
</script>

<style scoped>
.manage-page { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.header h2 { margin: 0; color: #0f172a; }
.alert.info { background: #e0f2fe; color: #0369a1; padding: 12px 16px; border-radius: 6px; margin-bottom: 2rem; font-size: 14px; }
.alert p { margin: 0; }
.btn { padding: 8px 16px; border-radius: 6px; font-weight: 500; cursor: pointer; border: none; font-size: 14px; }
.btn.primary { background: #0ea5e9; color: white; transition: background 0.2s; }
.btn.primary:hover { background: #0284c7; }
.btn.ghost { background: transparent; border: 1px solid #cbd5e1; color: #475569; }
.btn-sm { padding: 4px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; border: 1px solid #cbd5e1; background: white; margin-right: 6px; }
.btn-sm.danger { color: #ef4444; border-color: #fca5a5; background: #fef2f2; }
.table-container { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f1f5f9; }
th { background: #f8fafc; color: #475569; font-weight: 600; font-size: 14px; }
td { color: #334155; font-size: 15px; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.4); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal { background: white; padding: 2rem; border-radius: 12px; width: 600px; max-width: 90vw; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin-top: 0; margin-bottom: 1.5rem; color: #0f172a; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }
.form-group { margin-bottom: 1.2rem; }
.form-group label { display: block; margin-bottom: 6px; font-size: 14px; font-weight: 500; color: #334155; }
.form-group input { width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
.form-group input:focus { outline: none; border-color: #0ea5e9; box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.1); }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 2rem; }
</style>
