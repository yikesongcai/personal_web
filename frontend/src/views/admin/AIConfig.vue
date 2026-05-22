<template>
  <div class="manage-page">
    <div class="header">
      <h2>AI 模型配置</h2>
    </div>

    <div class="notice">
      API Key 通过环境变量配置，此处仅展示当前使用的模型参数。
    </div>

    <div class="config-grid">
      <div class="config-card">
        <div class="label">聊天模型 (Chat Model)</div>
        <div class="value">{{ config.chatModel }}</div>
      </div>
      <div class="config-card">
        <div class="label">API 端点 (Base URL)</div>
        <div class="value mono">{{ config.baseUrl }}</div>
      </div>
      <div class="config-card">
        <div class="label">嵌入模型 (Embedding Model)</div>
        <div class="value">{{ config.embeddingModel }}</div>
      </div>
      <div class="config-card">
        <div class="label">温度 (Temperature)</div>
        <div class="value">{{ config.temperature }}</div>
      </div>
      <div class="config-card">
        <div class="label">RAG Top-K</div>
        <div class="value">{{ config.ragTopK }}</div>
      </div>
      <div class="config-card">
        <div class="label">相似度阈值 (Similarity Threshold)</div>
        <div class="value">{{ config.ragSimilarityThreshold }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const config = ref({})

const token = () => sessionStorage.getItem('adminToken')

onMounted(async () => {
  const res = await fetch('/api/admin/ai-config', {
    headers: { 'X-Admin-Token': token() }
  })
  config.value = await res.json()
})
</script>

<style scoped>
.manage-page { max-width: 800px; }
.header { margin-bottom: 1rem; }
.header h2 { margin: 0; }
.notice { background: #dbeafe; color: #1e40af; padding: 12px 16px; border-radius: 6px; margin-bottom: 1.5rem; font-size: 0.9rem; }
.config-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.config-card { background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.25rem; }
.config-card .label { color: #64748b; font-size: 0.85rem; margin-bottom: 0.5rem; }
.config-card .value { font-size: 1.1rem; font-weight: 600; color: #1e293b; word-break: break-all; }
.config-card .mono { font-family: monospace; font-size: 0.9rem; }
</style>
