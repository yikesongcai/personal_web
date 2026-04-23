<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <h2>控制中心 (Admin)</h2>
      <nav>
        <router-link to="/admin/dashboard">仪表盘概览</router-link>
        <router-link to="/admin/projects">项目管理</router-link>
        <router-link to="/admin/articles">文章管理</router-link>
        <router-link to="/admin/rate-limit">速率白名单</router-link>
        <router-link to="/admin/system-log">系统日志</router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </aside>
    <main class="admin-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleLogout = () => {
  sessionStorage.removeItem('adminToken')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: calc(100vh - 66px); background: #f8fafc; }
.sidebar { width: 250px; background: #1e293b; color: white; padding: 2rem; box-shadow: 2px 0 10px rgba(0,0,0,0.1); display: flex; flex-direction: column; }
.sidebar h2 { margin-top: 0; color: #f8fafc; font-size: 1.2rem; margin-bottom: 2rem; letter-spacing: 1px; }
.sidebar nav { display: flex; flex-direction: column; gap: 0.5rem; flex: 1; }
.sidebar nav a { color: #94a3b8; text-decoration: none; padding: 10px 14px; border-radius: 6px; transition: all 0.2s; }
.sidebar nav a.router-link-active, .sidebar nav a:hover { color: white; background: #334155; font-weight: 500; }
.sidebar-footer { margin-top: auto; padding-top: 1rem; border-top: 1px solid #334155; }
.logout-btn { width: 100%; padding: 10px; background: transparent; border: 1px solid #64748b; color: #cbd5e1; border-radius: 6px; cursor: pointer; transition: all 0.2s; }
.logout-btn:hover { background: #ef4444; color: white; border-color: #ef4444; }
.admin-content { flex: 1; padding: 2.5rem; overflow-y: auto; height: calc(100vh - 66px); box-sizing: border-box; }
</style>
