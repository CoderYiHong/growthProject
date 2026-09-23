<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getDashboardStats, getAdminProjects, getAdminArticles, getAdminMessages, getAdminLogs, getDashboardTrend, getDashboardCategories } from '@/api/admin'
import { transformProject, transformArticle, formatDate } from '@/utils'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 统计卡片
const statCards = ref([
  { label: '项目总数', value: 0, icon: 'projects', color: '#4F9D57', bg: '#EAF5E8' },
  { label: '文章总数', value: 0, icon: 'articles', color: '#6CB4EE', bg: '#E8F4FD' },
  { label: '访问总量', value: 0, icon: 'visits', color: '#F0B85A', bg: '#FEF6E8' },
  { label: '留言总数', value: 0, icon: 'messages', color: '#9B7EC4', bg: '#F0E8F5' }
])

const recentProjects = ref([])
const recentArticles = ref([])
const pendingMessages = ref([])
const recentLogs = ref([])
const visitTrendData = ref([])
const contentCategoryData = ref([])

// 图表
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

onMounted(async () => {
  // 获取仪表盘数据
  try {
    const res = await getDashboardStats()
    if (res.code === 200) {
      const d = res.data
      statCards.value[0].value = d.projectCount || 0
      statCards.value[1].value = d.articleCount || 0
      statCards.value[2].value = (d.totalVisits || 0).toLocaleString()
      statCards.value[3].value = d.messageCount || 0
    }
  } catch (e) {
    ElMessage.warning('仪表盘统计数据加载失败')
  }

  // 获取最近项目和文章 + 趋势/分类数据
  try {
    const [projRes, artRes, msgRes, logRes, trendRes, catRes] = await Promise.all([
      getAdminProjects({ page: 1, pageSize: 5 }),
      getAdminArticles({ page: 1, pageSize: 5 }),
      getAdminMessages({ page: 1, pageSize: 5 }),
      getAdminLogs({ page: 1, pageSize: 5 }),
      getDashboardTrend(),
      getDashboardCategories()
    ])
    if (projRes.code === 200 && projRes.data) {
      recentProjects.value = (projRes.data.records || []).map(transformProject)
    }
    if (artRes.code === 200 && artRes.data) {
      recentArticles.value = (artRes.data.records || []).map(transformArticle).filter(a => a.status === 'published')
    }
    if (msgRes.code === 200 && msgRes.data) {
      pendingMessages.value = (msgRes.data.records || []).filter(m => m.status === 'unread')
    }
    if (logRes.code === 200 && logRes.data) {
      recentLogs.value = (logRes.data.records || []).map(log => ({
        action: log.action,
        target: log.detail || log.module,
        time: log.createTime || log.time,
        user: log.userName || log.user || ''
      }))
    }
    if (trendRes.code === 200) {
      visitTrendData.value = trendRes.data || []
    }
    if (catRes.code === 200) {
      contentCategoryData.value = catRes.data || []
    }
  } catch (e) {
    ElMessage.warning('仪表盘列表数据加载失败')
  }

  // 数据就绪后初始化图表
  initTrendChart()
  initPieChart()
  window.addEventListener('resize', handleResize)
})

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['访问量', '文章发布'],
      bottom: 0
    },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: visitTrendData.value.map(d => d.date),
      axisLabel: { fontSize: 11 }
    },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#D8E5DB' } } },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        data: visitTrendData.value.map(d => d.visits),
        itemStyle: { color: '#4F9D57' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(79,157,87,0.2)' },
          { offset: 1, color: 'rgba(79,157,87,0)' }
        ])}
      },
      {
        name: '文章发布',
        type: 'line',
        smooth: true,
        data: visitTrendData.value.map(d => d.articles),
        itemStyle: { color: '#F0B85A' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(240,184,90,0.2)' },
          { offset: 1, color: 'rgba(240,184,90,0)' }
        ])}
      }
    ]
  })
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: contentCategoryData.value.map(d => ({ name: d.name, value: d.value }))
    }]
  })
}

function handleResize() {
  trendChart?.resize()
  pieChart?.resize()
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <template v-if="card.icon === 'projects'"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></template>
            <template v-else-if="card.icon === 'articles'"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></template>
            <template v-else-if="card.icon === 'visits'"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></template>
            <template v-else><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></template>
          </svg>
        </div>
        <div>
          <span class="stat-value">{{ card.value }}</span>
          <span class="stat-label">{{ card.label }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-grid">
      <div class="card chart-card">
        <h4 class="card-title">访问趋势</h4>
        <div ref="trendChartRef" class="chart-box"></div>
      </div>
      <div class="card chart-card">
        <h4 class="card-title">内容分类</h4>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 列表区 -->
    <div class="lists-grid">
      <!-- 最近项目 -->
      <div class="card">
        <div class="card-header">
          <h4 class="card-title">最近项目</h4>
          <router-link to="/admin/projects" class="link-sm">查看全部</router-link>
        </div>
        <div class="list-simple">
          <div v-for="p in recentProjects" :key="p.id" class="list-item">
            <span class="list-item-name">{{ p.name }}</span>
            <span class="list-item-tag">{{ p.categoryLabel }}</span>
          </div>
        </div>
      </div>

      <!-- 最近文章 -->
      <div class="card">
        <div class="card-header">
          <h4 class="card-title">最近文章</h4>
          <router-link to="/admin/articles" class="link-sm">查看全部</router-link>
        </div>
        <div class="list-simple">
          <div v-for="a in recentArticles" :key="a.id" class="list-item">
            <span class="list-item-name">{{ a.title }}</span>
            <span class="list-item-date">{{ formatDate(a.createdAt, 'MM-DD') }}</span>
          </div>
        </div>
      </div>

      <!-- 待处理留言 -->
      <div class="card">
        <div class="card-header">
          <h4 class="card-title">待处理留言</h4>
          <router-link to="/admin/messages" class="link-sm">查看全部</router-link>
        </div>
        <div class="list-simple">
          <div v-for="m in pendingMessages.slice(0, 5)" :key="m.id" class="list-item">
            <span class="list-item-name">{{ m.name }} - {{ m.subject }}</span>
            <el-tag size="small" type="warning">未读</el-tag>
          </div>
          <div v-if="pendingMessages.length === 0" class="list-empty">暂无待处理留言</div>
        </div>
      </div>
    </div>

    <!-- 操作日志 -->
    <div class="card">
      <div class="card-header">
        <h4 class="card-title">最近操作</h4>
        <router-link to="/admin/logs" class="link-sm">查看全部</router-link>
      </div>
      <el-table :data="recentLogs" style="width: 100%" size="small">
        <el-table-column prop="action" label="操作" width="120" />
        <el-table-column prop="target" label="目标" show-overflow-tooltip />
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column prop="user" label="用户" width="80" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-title);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--color-secondary);
}

/* 图表 */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.chart-card {
  padding: var(--spacing-lg);
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.chart-box {
  height: 320px;
}

/* 卡片通用 */
.card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
}

.card-title {
  font-size: var(--font-size-md);
}

.link-sm {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  text-decoration: none;
}

/* 列表区 */
.lists-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.list-simple {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.list-item:last-child {
  border-bottom: none;
}

.list-item-name {
  font-size: var(--font-size-sm);
  color: var(--color-body);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: var(--spacing-sm);
}

.list-item-tag,
.list-item-date {
  font-size: var(--font-size-xs);
  color: var(--color-muted);
  flex-shrink: 0;
}

.list-empty {
  text-align: center;
  padding: var(--spacing-lg);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
}

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
  .lists-grid { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
}
</style>
