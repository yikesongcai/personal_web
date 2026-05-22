<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <h2>
        <DashboardOutlined class="brand-icon" />
        <span>控制中心</span>
      </h2>
      <nav>
        <div class="nav-group">
          <div class="nav-group-label">数据概览</div>
          <router-link to="/admin/dashboard">
            <DashboardOutlined /><span>仪表盘概览</span>
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-label">内容管理</div>
          <router-link to="/admin/homepage">
            <HomeOutlined /><span>首页内容管理</span>
          </router-link>
          <router-link to="/admin/projects">
            <FolderOutlined /><span>项目管理</span>
          </router-link>
          <router-link to="/admin/articles">
            <FileTextOutlined /><span>文章管理</span>
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-label">AI 与知识库</div>
          <router-link to="/admin/knowledge">
            <DatabaseOutlined /><span>RAG 知识库</span>
          </router-link>
          <router-link to="/admin/chat-history">
            <MessageOutlined /><span>会话记录</span>
          </router-link>
          <router-link to="/admin/ai-config">
            <SettingOutlined /><span>AI 配置</span>
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-label">系统管理</div>
          <router-link to="/admin/rate-limit">
            <SafetyCertificateOutlined /><span>速率白名单</span>
          </router-link>
          <router-link to="/admin/system-log">
            <FileSearchOutlined /><span>系统日志</span>
          </router-link>
        </div>
      </nav>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">
          <LogoutOutlined /><span>退出登录</span>
        </button>
      </div>
    </aside>
    <main class="admin-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  DashboardOutlined,
  HomeOutlined,
  FolderOutlined,
  FileTextOutlined,
  DatabaseOutlined,
  MessageOutlined,
  SettingOutlined,
  SafetyCertificateOutlined,
  FileSearchOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'

const router = useRouter()

const handleLogout = () => {
  sessionStorage.removeItem('adminToken')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: calc(100vh - 66px); background: #f8fafc; }
.sidebar { width: 260px; background: #1e293b; color: white; padding: 1.5rem 1rem; box-shadow: 2px 0 10px rgba(0,0,0,0.1); display: flex; flex-direction: column; }
.sidebar h2 { display: flex; align-items: center; gap: 8px; margin: 0 0 1.5rem 0.5rem; color: #f8fafc; font-size: 1.1rem; letter-spacing: 0.5px; }
.brand-icon { font-size: 1.3rem; color: #60a5fa; }
.sidebar nav { display: flex; flex-direction: column; gap: 1.25rem; flex: 1; overflow-y: auto; }
.nav-group { display: flex; flex-direction: column; gap: 2px; }
.nav-group-label { font-size: 0.7rem; text-transform: uppercase; letter-spacing: 1.5px; color: #64748b; padding: 0 0.75rem 6px; font-weight: 600; }
.nav-group a { display: flex; align-items: center; gap: 10px; color: #94a3b8; text-decoration: none; padding: 9px 12px; border-radius: 6px; transition: all 0.15s; font-size: 0.9rem; }
.nav-group a .anticon { font-size: 1rem; width: 18px; text-align: center; flex-shrink: 0; }
.nav-group a.router-link-active, .nav-group a:hover { color: white; background: #334155; }
.nav-group a.router-link-active { font-weight: 500; border-left: 3px solid #60a5fa; padding-left: 9px; }
.sidebar-footer { margin-top: auto; padding-top: 0.75rem; border-top: 1px solid #334155; }
.logout-btn { display: flex; align-items: center; justify-content: center; gap: 6px; width: 100%; padding: 9px; background: transparent; border: 1px solid #475569; color: #cbd5e1; border-radius: 6px; cursor: pointer; font-size: 0.9rem; transition: all 0.2s; }
.logout-btn:hover { background: #ef4444; color: white; border-color: #ef4444; }
.admin-content { flex: 1; padding: 2.5rem; overflow-y: auto; height: calc(100vh - 66px); box-sizing: border-box; }
</style>
