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
          <h3>{{ stats.tokenCont }} W</h3>
        </div>
      </div>
      <div class="card stat-card">
        <div class="icon">👀</div>
        <div class="info">
          <span>全站访问人次</span>
          <h3>{{ stats.visitsCount }} +</h3>
        </div>
      </div>
      <div class="card stat-card">
        <div class="icon">📦</div>
        <div class="info">
          <span>项目总数</span>
          <h3>{{ projectsCount }}</h3>
        </div>
      </div>
      <div class="card stat-card">
        <div class="icon">📝</div>
        <div class="info">
          <span>文章总数</span>
          <h3>{{ articlesCount }}</h3>
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

const filterToken = ref('month')
const filterVisits = ref('day')

const projectsCount = ref(0)
const articlesCount = ref(0)
const stats = ref({ tokenCont: 1.25, visitsCount: '3.6k' })

const tokenChartRef = ref(null)
const visitsChartRef = ref(null)

let tokenChart = null
let visitsChart = null

const fetchCounts = async () => {
  try {
    const pRes = await fetch('/api/projects')
    if(pRes.ok) { const p = await pRes.json(); projectsCount.value = p.length }
    const aRes = await fetch('/api/articles')
    if(aRes.ok) { const a = await aRes.json(); articlesCount.value = a.length }
  } catch(e) {
    console.error(e)
  }
}

const mockTokenData = {
  day: { x: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00'], y: [120, 300, 150, 400, 210, 800, 500] },
  month: { x: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'], y: [12000, 8000, 15000, 22000, 19000, 25000, 31000] },
  year: { x: ['2023', '2024', '2025', '2026'], y: [150000, 230000, 380000, 410000] }
}

const mockVisitsData = {
  day: { x: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00'], y: [35, 80, 45, 120, 60, 210, 180] },
  month: { x: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'], y: [3200, 2100, 4500, 3900, 5100, 4800, 6200] },
  year: { x: ['2023', '2024', '2025', '2026'], y: [21000, 34000, 52000, 68000] }
}

const renderTokenChart = () => {
  const data = mockTokenData[filterToken.value]
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.x, axisLine: { lineStyle: { color: '#94a3b8' } } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
    series: [{ data: data.y, type: 'bar', itemStyle: { color: '#0ea5e9', borderRadius: [4,4,0,0] } }]
  }
  tokenChart.setOption(option)
}

const renderVisitsChart = () => {
  const data = mockVisitsData[filterVisits.value]
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.x, boundaryGap: false, axisLine: { lineStyle: { color: '#94a3b8' } } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
    series: [{ data: data.y, type: 'line', smooth: true, areaStyle: { color: 'rgba(16, 185, 129, 0.2)' }, itemStyle: { color: '#10b981' } }]
  }
  visitsChart.setOption(option)
}

const changeTokenFilter = (val) => { filterToken.value = val; renderTokenChart() }
const changeVisitsFilter = (val) => { filterVisits.value = val; renderVisitsChart() }

onMounted(async () => {
  await fetchCounts()
  await nextTick()
  tokenChart = echarts.init(tokenChartRef.value)
  visitsChart = echarts.init(visitsChartRef.value)
  renderTokenChart()
  renderVisitsChart()

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
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
