<template>
  <div>
    <nav class="navbar">
      <div class="nav-brand"><router-link to="/">SONG'S LAB</router-link></div>
      <div class="nav-links">
        <router-link to="/">Home</router-link>
        <router-link to="/projects">Projects</router-link>
        <router-link to="/articles">Articles</router-link>
        <router-link to="/admin" class="admin-link">Admin</router-link>
      </div>
    </nav>
    <router-view></router-view>
    <AiChatWidget api-url="/api/chat" history-url="/api/chat/history" />
  </div>
</template>

<script setup>
import AiChatWidget from './components/AiChatWidget.vue';
</script>

<style>
body { 
  margin: 0; 
  color: #0f172a; 
  font-family: 'IBM Plex Sans', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  background:
    radial-gradient(circle at 15% -5%, rgba(20, 184, 166, 0.22), transparent 24%),
    radial-gradient(circle at 92% 5%, rgba(249, 115, 22, 0.22), transparent 24%),
    linear-gradient(180deg, #fffbeb 0%, #f0fdfa 100%);
  background-attachment: fixed;
}
.navbar { 
  display: flex; 
  justify-content: space-between; 
  align-items: center;
  padding: 1rem 2rem; 
  background: rgba(255, 255, 255, 0.85); 
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(238, 238, 238, 0.5); 
  position: sticky;
  top: 0;
  z-index: 100;
}
.nav-brand a { font-weight: bold; text-decoration: none; color: #0f766e; letter-spacing: 1px; }
.nav-links a { margin-left: 1.5rem; text-decoration: none; color: #334155; font-weight: 500; font-size: 15px;}
.nav-links a:hover, .nav-links a.router-link-active { color: #0ea5e9; }
.admin-link {
  font-size: 0.85em;
  padding: 4px 8px;
  background: #f1f5f9;
  border-radius: 4px;
  transition: background 0.2s;
}
.admin-link:hover {
  background: #e2e8f0;
}
</style>
