<template>
  <div class="dashboard">
    <div class="header">
      <h2>控制台概览 (Dashboard)</h2>
    </div>

    <!-- Cards Section -->
    <div class="cards-wrapper">
      <div class="card stat-card">
        <div class="icon">🎟️</div>
        <div class="info">
          <span>总 Token 消耗</span>
          <h3>{{ formatTokens(summary.totalTokens) }}</h3>
        </div>
      </div>
      <div class="card stat-card">
        <div class="icon">👀</div>
        <div class="info">
          <span>全站访问人次</span>
          <h3>{{ summary.totalVisits }}</h3>
        </div>
      </div>
      <div class="card stat-card">
        <div class="icon">💬</div>
        <div class="info">
          <span>AI 对话总数</span>
          <h3>{{ summary.chatCount }}</h3>
        </div>
      </div>
      <div class="card stat-card">
        <div class="icon">📦</div>
        <div class="info">
          <span>项目总数</span>
          <h3>{{ summary.projectCount }}</h3>
        </div>
      </div>
      <div class="card stat-card">
        <div class="icon">📝</div>
        <div class="info">
          <span>文章总数</span>
          <h3>{{ summary.articleCount }}</h3>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-wrapper">
      <!-- Token Chart -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>Token 消耗统计</h3>
          <div class="filter-group">
            <button :class="{active: filterToken === 'day'}" @click="changeTokenFilter('day')">日</button>
            <button :class="{active: filterToken === 'month'}" @click="changeTokenFilter('month')">月</button>
            <button :class="{active: filterToken === 'year'}" @click="changeTokenFilter('year')">年</button>
          </div>
        </div>
        <div class="chart-body" ref="tokenChartRef"></div>
      </div>

      <!-- Visits Chart -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>访问流量统计</h3>
          <div class="filter-group">
            <button :class="{active: filterVisits === 'day'}" @click="changeVisitsFilter('day')">日</button>
            <button :class="{active: filterVisits === 'month'}" @click="changeVisitsFilter('month')">月</button>
            <button :class="{active: filterVisits === 'year'}" @click="changeVisitsFilter('year')">年</button>
          </div>
        </div>
        <div class="chart-body" ref="visitsChartRef"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const filterToken = ref('day')
const filterVisits = ref('day')
const summary = ref({ totalTokens: 0, totalVisits: 0, chatCount: 0, projectCount: 0, articleCount: 0 })

const tokenChartRef = ref(null)
const visitsChartRef = ref(null)

let tokenChart = null
let visitsChart = null

const adminToken = sessionStorage.getItem('adminToken')

const formatTokens = (v) => {
  if (!v) return '0'
  if (v >= 10000) return (v / 10000).toFixed(2) + ' 万'
  return String(v)
}

const fetchSummary = async () => {
  const res = await fetch('/api/admin/dashboard/summary', {
    headers: { 'X-Admin-Token': adminToken }
  })
  if (res.ok) summary.value = await res.json()
}

const fetchStatsAndRender = async (chartType) => {
  const period = chartType === 'token' ? filterToken.value : filterVisits.value
  const res = await fetch(`/api/admin/dashboard/stats?period=${period}`, {
    headers: { 'X-Admin-Token': adminToken }
  })
  if (!res.ok) return
  const json = await res.json()
  const rows = json.data || []
  const xData = rows.map(r => r.label)
  const yData = chartType === 'token' ? rows.map(r => Number(r.tokens)) : rows.map(r => Number(r.visits))

  if (chartType === 'token') {
    tokenChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: xData, axisLine: { lineStyle: { color: '#94a3b8' } } },
      yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
      series: [{ data: yData, type: 'bar', itemStyle: { color: '#0ea5e9', borderRadius: [4,4,0,0] } }]
    })
  } else {
    visitsChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: xData, boundaryGap: false, axisLine: { lineStyle: { color: '#94a3b8' } } },
      yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
      series: [{ data: yData, type: 'line', smooth: true, areaStyle: { color: 'rgba(16, 185, 129, 0.2)' }, itemStyle: { color: '#10b981' } }]
    })
  }
}

const changeTokenFilter = (val) => { filterToken.value = val; fetchStatsAndRender('token') }
const changeVisitsFilter = (val) => { filterVisits.value = val; fetchStatsAndRender('visits') }

onMounted(async () => {
  await fetchSummary()
  await nextTick()
  tokenChart = echarts.init(tokenChartRef.value)
  visitsChart = echarts.init(visitsChartRef.value)
  fetchStatsAndRender('token')
  fetchStatsAndRender('visits')

  window.addEventListener('resize', () => {
    tokenChart.resize()
    visitsChart.resize()
  })
})
</script>

<style scoped>
.dashboard { padding: 1rem; }
.header { margin-bottom: 2rem; }
.header h2 { margin: 0; color: #0f172a; }

.cards-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2.5rem;
}
.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  display: flex;
  align-items: center;
  gap: 1.2rem;
}
.stat-card .icon {
  font-size: 2.5rem;
  background: #f1f5f9;
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 12px;
  flex-shrink: 0;
}
.stat-card .info span { color: #64748b; font-size: 0.9rem; font-weight: 500; }
.stat-card .info h3 { margin: 6px 0 0; font-size: 1.8rem; color: #0f172a; }

.charts-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 1.5rem;
}
.chart-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.chart-header h3 { margin: 0; font-size: 1.1rem; color: #334155; }
.filter-group {
  display: flex;
  background: #f1f5f9;
  border-radius: 6px;
  padding: 2px;
}
.filter-group button {
  border: none;
  background: transparent;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  color: #64748b;
  font-size: 0.9rem;
  transition: all 0.2s;
}
.filter-group button.active {
  background: white;
  color: #0ea5e9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  font-weight: 500;
}
.chart-body {
  width: 100%;
  height: 300px;
}
</style>

