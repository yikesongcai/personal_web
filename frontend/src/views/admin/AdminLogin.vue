<template>
  <div class="login-container">
    <div class="login-box">
      <h2>后台管理登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>Admin API Key</label>
          <input type="password" v-model="apiKey" required placeholder="Enter your secret key" />
        </div>
        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
        <button type="submit" class="btn primary">进入管理后台</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const apiKey = ref('')
const errorMsg = ref('')

const handleLogin = async () => {
  errorMsg.value = ''
  try {
    const res = await fetch('/api/admin/verify', {
      headers: {
        'X-Admin-Token': apiKey.value
      }
    })
    
    if (res.ok) {
      sessionStorage.setItem('adminToken', apiKey.value)
      router.push('/admin/dashboard')
    } else {
      errorMsg.value = '验证失败：API Key 错误'
    }
  } catch (e) {
    errorMsg.value = '请求失败，请检查网络或后端服务'
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f8fafc;
}
.login-box {
  background: white;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  width: 100%;
  max-width: 400px;
}
.login-box h2 {
  margin-top: 0;
  margin-bottom: 2rem;
  color: #0f172a;
  text-align: center;
  font-size: 1.5rem;
}
.form-group {
  margin-bottom: 1.5rem;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}
.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}
.form-group input:focus {
  outline: none;
  border-color: #0ea5e9;
  box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.1);
}
.error-msg {
  color: #ef4444;
  font-size: 13px;
  margin-bottom: 1rem;
  text-align: center;
}
.btn {
  width: 100%;
  padding: 12px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  font-size: 15px;
}
.btn.primary {
  background: #0ea5e9;
  color: white;
  transition: background 0.2s;
}
.btn.primary:hover {
  background: #0284c7;
}
</style>
