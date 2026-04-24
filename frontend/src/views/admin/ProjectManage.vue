<template>
  <div class="manage-page">
    <div class="header">
      <h2>项目管理 (Projects)</h2>
      <button class="btn primary" @click="openForm()">+ 新增项目</button>
    </div>

    <!-- Table -->
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th style="width:50px">ID</th>
            <th>项目标题</th>
            <th style="width:130px">使用框架</th>
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
            <td>{{ item.frameworks }}</td>
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
        <h3>{{ formData.id ? '编辑项目' : '新增项目' }}</h3>
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label>标题 (Title)</label>
            <input v-model="formData.title" required />
          </div>
          <div class="form-group">
          <label>使用框架 (Frameworks)</label>
          <input v-model="formData.frameworks" placeholder="如: Spring Boot / Vue" />
        </div>
        <div class="form-group">
          <label>项目简介 (Summary) <small style="color:#94a3b8">如填写，将作为展示用摘要显示在卡片和列表中</small></label>
          <textarea v-model="formData.summary" rows="2" placeholder="一句话描述项目的核心功能和特色..."></textarea>
        </div>
        <div class="form-group">
          <label>在线地址 (Live URL)</label>
            <input v-model="formData.onlineUrl" />
          </div>
          <div class="form-group">
          <label>GitHub 地址 (GitHub URL)</label>
          <div class="checkbox-row">
            <input type="checkbox" id="closedSource" v-model="isClosedSource" @change="onClosedSourceChange" />
            <label for="closedSource" class="checkbox-label">🔒 非开源项目（不显示 GitHub 链接）</label>
          </div>
          <input v-if="!isClosedSource" v-model="formData.githubUrl" placeholder="https://github.com/user/repo" style="margin-top:8px" />
        </div>
          <div class="form-row">
            <div class="form-group" style="flex:1">
              <label>显示排序 (Sort Order) <small style="color:#94a3b8">数字越小迆靠前</small></label>
              <input type="number" v-model.number="formData.sortOrder" min="0" style="max-width:120px" />
            </div>
            <div class="form-group" style="flex:1">
              <label>设为首页精选</label>
              <div class="checkbox-row">
                <input type="checkbox" id="isFeatured" v-model="formData.isFeatured" />
                <label for="isFeatured" class="checkbox-label">⭐ 设为首页精选展示</label>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>封面图片 (Cover)</label>
            <input type="file" @change="uploadCover" accept="image/*" />
            <div v-if="formData.coverImage" style="margin-top: 8px">
              <img :src="formData.coverImage" alt="cover" style="max-height: 100px; border-radius: 4px; border: 1px solid #eee;" />
            </div>
          </div>
          <div class="form-group">
            <label>内容/README (Markdown) - <i>将自动同步至AI知识库</i></label>
            <textarea v-model="formData.content" rows="6"></textarea>
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
const isClosedSource = ref(false)

const fetchItems = async () => {
  const res = await fetch('/api/admin/projects', {
    headers: { 'X-Admin-Token': sessionStorage.getItem('adminToken') }
  })
  if(res.ok) items.value = await res.json()
}

const openForm = (item = null) => {
  if (item) {
    formData.value = { ...item }
    isClosedSource.value = item.githubUrl === null || item.githubUrl === ''
  } else {
    formData.value = { title: '', frameworks: '', onlineUrl: '', githubUrl: '', coverImage: '', content: '', sortOrder: 0, isFeatured: false }
    isClosedSource.value = false
  }
  showForm.value = true
}

const onClosedSourceChange = () => {
  if (isClosedSource.value) {
    formData.value.githubUrl = ''
  }
}

const uploadCover = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  const uploadData = new FormData()
  uploadData.append('file', file)
  
  try {
    const res = await fetch('/api/admin/upload', {
      method: 'POST',
      headers: { 'X-Admin-Token': sessionStorage.getItem('adminToken') },
      body: uploadData
    })
    const data = await res.json()
    if (data.url) {
      formData.value.coverImage = data.url
    } else {
      alert('上传失败：' + (data.error || '未知错误'))
    }
  } catch (err) {
    alert('上传异常')
  }
}

const saveItem = async () => {
  const isEdit = !!formData.value.id
  const url = isEdit ? `/api/admin/projects/${formData.value.id}` : '/api/admin/projects'
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
  if(!confirm('确定要删除该项目吗？这是一项不可逆的操作。')) return
  const res = await fetch(`/api/admin/projects/${id}`, { 
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

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.4); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal { background: white; padding: 2rem; border-radius: 12px; width: 600px; max-width: 90vw; max-height: 90vh; overflow-y: auto; }
.modal h3 { margin-top: 0; margin-bottom: 1.5rem; color: #0f172a; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }
.form-group { margin-bottom: 1.2rem; }
.form-group label { display: block; margin-bottom: 6px; font-size: 14px; font-weight: 500; color: #334155; }
.form-group input, .form-group textarea { width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-family: inherit; font-size: 14px; box-sizing: border-box; }
.form-group input[type="file"] { padding: 6px; }
.form-group input:focus, .form-group textarea:focus { outline: none; border-color: #0ea5e9; box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.1); }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 2rem; }
.checkbox-row { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
.checkbox-row input[type="checkbox"] { width: 16px; height: 16px; cursor: pointer; accent-color: #0ea5e9; }
.checkbox-label { font-size: 13px; color: #475569; cursor: pointer; }
.form-row { display: flex; gap: 1rem; align-items: flex-start; margin-bottom: 1.2rem; }
.badge { display: inline-block; padding: 2px 8px; border-radius: 999px; font-size: 12px; font-weight: 500; }
.badge.featured { background: #fef9c3; color: #92400e; }
.badge.normal { background: #f1f5f9; color: #64748b; }
</style>
