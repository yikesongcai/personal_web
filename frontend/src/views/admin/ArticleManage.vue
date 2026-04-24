<template>
  <div class="manage-page">
    <div class="header">
      <h2>文章管理 (Articles)</h2>
      <button class="btn primary" @click="openForm()">+ 新增文章</button>
    </div>

    <!-- Table -->
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th style="width:50px">ID</th>
            <th>文章标题</th>
            <th style="width:100px">标签</th>
            <th style="width:60px">排序</th>
            <th style="width:60px">精选</th>
            <th style="width:110px">创建时间</th>
            <th style="width:110px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.title }}</td>
            <td>{{ item.tags }}</td>
            <td>{{ item.sortOrder }}</td>
            <td><span :class="['badge', item.isFeatured ? 'featured' : 'normal']">{{ item.isFeatured ? '⭐精选' : '普通' }}</span></td>
            <td>{{ new Date(item.createdAt).toLocaleDateString() }}</td>
            <td class="actions">
              <button class="btn-sm" @click="openForm(item)">编辑</button>
              <button class="btn-sm danger" @click="deleteItem(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal Form -->
    <div class="modal-overlay" v-if="showForm" @click.self="showForm = false">
      <div class="modal">
        <h3>{{ formData.id ? '编辑文章' : '新增文章' }}</h3>
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label>标题 (Title)</label>
            <input v-model="formData.title" required />
          </div>
          <div class="form-group">
            <label>标签 (Tags)</label>
            <input v-model="formData.tags" placeholder="如: 前端, Vue, 教程" />
          </div>
          <div class="form-group">
            <label>文章摘要 (Summary) <small style="color:#94a3b8">展示在卡片上的简短描述</small></label>
            <textarea v-model="formData.summary" rows="2" placeholder="一句话概括文章内容..."></textarea>
          </div>
          <div class="form-row">
            <div class="form-group" style="flex:1">
              <label>显示排序 (Sort Order) <small style="color:#94a3b8">数字越小越靠前</small></label>
              <input type="number" v-model.number="formData.sortOrder" min="0" style="max-width:120px" />
            </div>
            <div class="form-group" style="flex:1">
              <label>设为首页精选</label>
              <div class="checkbox-row">
                <input type="checkbox" id="articleFeatured" v-model="formData.isFeatured" />
                <label for="articleFeatured" class="checkbox-label">⭐ 设为首页精选展示</label>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>内容 (Markdown) - <i>将自动同步至AI知识库</i></label>
            <textarea v-model="formData.content" rows="12" required></textarea>
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
  const res = await fetch('/api/admin/articles', {
    headers: { 'X-Admin-Token': sessionStorage.getItem('adminToken') }
  })
  if(res.ok) items.value = await res.json()
}

const openForm = (item = null) => {
  if (item) {
    formData.value = { ...item }
  } else {
    formData.value = { title: '', tags: '', summary: '', content: '', sortOrder: 0, isFeatured: false }
  }
  showForm.value = true
}

const saveItem = async () => {
  const isEdit = !!formData.value.id
  const url = isEdit ? `/api/admin/articles/${formData.value.id}` : '/api/admin/articles'
  const method = isEdit ? 'PUT' : 'POST'
  
  const res = await fetch(url, {
    method,
    headers: { 
      'Content-Type': 'application/json',
      'X-Admin-Token': sessionStorage.getItem('adminToken')
    },
    body: JSON.stringify(formData.value)
  })
  
  if(res.ok) {
    showForm.value = false
    fetchItems()
  } else {
    alert('保存失败')
  }
}

const deleteItem = async (id) => {
  if(!confirm('确定要删除该文章吗？（逻辑删除，数据可恢复）')) return
  const res = await fetch(`/api/admin/articles/${id}`, { 
    method: 'DELETE',
    headers: { 'X-Admin-Token': sessionStorage.getItem('adminToken') }
  })
  if(res.ok) fetchItems()
}

onMounted(fetchItems)
</script>

<style scoped>
.manage-page { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.header h2 { margin: 0; color: #0f172a; }
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
.badge { display: inline-block; padding: 2px 8px; border-radius: 999px; font-size: 12px; font-weight: 500; }
.badge.featured { background: #fef9c3; color: #92400e; }
.badge.normal { background: #f1f5f9; color: #64748b; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.4); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal { background: white; padding: 2rem; border-radius: 12px; width: 600px; max-width: 90vw; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin-top: 0; margin-bottom: 1.5rem; color: #0f172a; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }
.form-group { margin-bottom: 1.2rem; }
.form-group label { display: block; margin-bottom: 6px; font-size: 14px; font-weight: 500; color: #334155; }
.form-group input, .form-group textarea { width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-family: inherit; font-size: 14px; box-sizing: border-box; }
.form-group input:focus, .form-group textarea:focus { outline: none; border-color: #0ea5e9; box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.1); }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 2rem; }
.form-row { display: flex; gap: 1rem; align-items: flex-start; margin-bottom: 1.2rem; }
.checkbox-row { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
.checkbox-row input[type="checkbox"] { width: 16px; height: 16px; cursor: pointer; accent-color: #0ea5e9; }
.checkbox-label { font-size: 13px; color: #475569; cursor: pointer; }
</style>
