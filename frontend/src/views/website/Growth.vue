<script setup>
import { ref, computed } from 'vue'
import { useStudyPlanStore } from '@/stores/studyPlan'
import { planSheets } from '@/data/studyPlans'
import LearningHeatmap from '@/components/growth/LearningHeatmap.vue'
import DayDetailDrawer from '@/components/growth/DayDetailDrawer.vue'
import FocusDonut from '@/components/growth/FocusDonut.vue'
import DailyGoal from '@/components/growth/DailyGoal.vue'
import PlantCard from '@/components/growth/PlantCard.vue'

const store = useStudyPlanStore()
const today = new Date().toISOString().slice(0, 10)
const donutPeriod = ref('week')

// ── 筛选 ──
const viewYear = ref(new Date().getFullYear())
const filterCategory = ref('all')
const filterPlanId = ref('all')
const metric = ref('minutes')

const filterPlans = computed(() => {
  if (filterCategory.value === 'all') return store.plans
  return store.plans.filter(p => p.category === filterCategory.value)
})

// ── 每日数据计算 ──
const dailyData = computed(() => {
  const map = {}
  for (const chk of store.checkins) {
    if (!chk.date) continue
    const plan = store.getPlanById(chk.planId)
    const cat = plan?.category || ''
    if (filterCategory.value !== 'all' && cat !== filterCategory.value) continue
    if (filterPlanId.value !== 'all' && chk.planId !== filterPlanId.value) continue
    const d = chk.date.slice(0, 10)
    if (!map[d]) map[d] = { minutes: 0, pomodoros: 0, items: 0, records: [] }
    map[d].minutes += Math.round((chk.duration || 0))
    if (chk.itemId && chk.duration >= 25) map[d].pomodoros++
    if (chk.itemId && store.getItemById(chk.planId, chk.itemId)?.status === 'done') map[d].items++
    map[d].records.push(chk)
  }
  return map
})

// ── 年度统计 ──
const yearStats = computed(() => {
  let totalMins = 0, pomodoros = 0, itemsDone = 0, daysWithStudy = 0
  const yearStr = String(viewYear.value)
  const days = new Set()
  for (const [d, data] of Object.entries(dailyData.value)) {
    if (!d.startsWith(yearStr)) continue
    totalMins += data.minutes
    pomodoros += data.pomodoros
    if (data.minutes > 0) { daysWithStudy++; days.add(d) }
  }
  // 连续学习天数
  let streak = 0, maxStreak = 0
  const sorted = [...days].sort()
  let prev = null
  for (const d of sorted) {
    if (prev) {
      const prevDate = new Date(prev); prevDate.setDate(prevDate.getDate() + 1)
      if (prevDate.toISOString().slice(0, 10) === d) streak++
      else { maxStreak = Math.max(maxStreak, streak); streak = 1 }
    } else streak = 1
    prev = d
  }
  maxStreak = Math.max(maxStreak, streak)

  // 当前连续（从今天往前）
  let currentStreak = 0
  let check = new Date()
  while (true) {
    const key = check.toISOString().slice(0, 10)
    if (dailyData.value[key]?.minutes > 0) { currentStreak++; check.setDate(check.getDate() - 1) }
    else break
  }

  return { totalMins, pomodoros, daysWithStudy, currentStreak, maxStreak }
})

function fmtDuration(mins) {
  if (mins < 60) return mins + ' 分钟'
  const h = Math.floor(mins / 60); const m = mins % 60
  if (h < 10) return `${h}小时${m > 0 ? m + '分钟' : ''}`
  return (mins / 60).toFixed(1) + ' 小时'
}

// ── 活跃计划（进行中） ──
const activePlans = computed(() => store.plans.filter(p => p.status === 'active'))

function planWeekMins(planId) {
  const weekAgo = new Date(); weekAgo.setDate(weekAgo.getDate() - 7)
  const from = weekAgo.toISOString().slice(0, 10)
  return store.checkins.filter(c => c.planId === planId && c.date >= from).reduce((s, c) => s + (c.duration || 0), 0)
}

// ── 近期时间线 ──
const recentTimeline = computed(() => {
  return [...store.checkins].sort((a, b) => new Date(b.date) - new Date(a.date)).slice(0, 15).map(c => {
    const plan = store.getPlanById(c.planId)
    const item = c.itemId ? store.getItemById(c.planId, c.itemId) : null
    return { ...c, planName: plan?.name || '', category: plan?.category || '', itemTitle: item?.title || '' }
  })
})

// ── 日期详情 ──
const detailVisible = ref(false)
const detailDay = ref(null)
function onDayClick(day) { detailDay.value = day; detailVisible.value = true }

// ── 年份列表 ──
const yearOptions = []
const currentYear = new Date().getFullYear()
for (let y = currentYear; y >= currentYear - 3; y--) yearOptions.push(y)
</script>

<template>
  <div class="growth-dashboard">
    <div class="growth-main">
    <!-- 顶部统计 -->
    <div class="gh-header">
      <h2>学习足迹</h2>
      <div class="gh-filters">
        <select v-model="viewYear" class="gsel"><option v-for="y in yearOptions" :key="y" :value="y">{{ y }} 年</option></select>
        <select v-model="filterCategory" class="gsel"><option value="all">全部分类</option><option v-for="sh in planSheets.filter(s=>s.key!=='all')" :key="sh.key" :value="sh.key">{{ sh.label }}</option></select>
        <select v-model="filterPlanId" class="gsel"><option value="all">全部计划</option><option v-for="p in filterPlans" :key="p.id" :value="p.id">{{ p.name }}</option></select>
        <select v-model="metric" class="gsel"><option value="minutes">专注时长</option><option value="pomodoros">番茄数</option><option value="items">完成子任务</option></select>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card"><span class="sn">{{ fmtDuration(yearStats.totalMins) }}</span><span class="sl">年度专注时长</span></div>
      <div class="stat-card"><span class="sn">{{ yearStats.pomodoros }}</span><span class="sl">完成番茄</span></div>
      <div class="stat-card"><span class="sn">{{ yearStats.daysWithStudy }}</span><span class="sl">学习天数</span></div>
      <div class="stat-card"><span class="sn">{{ yearStats.currentStreak }}</span><span class="sl">当前连续</span></div>
      <div class="stat-card"><span class="sn">{{ yearStats.maxStreak }}</span><span class="sl">最长连续</span></div>
    </div>

    <!-- 贡献图 -->
    <div class="section-card" style="background: rgba(255,255,255,0.94); backdrop-filter: blur(4px);">
      <LearningHeatmap :year="viewYear" :daily-data="dailyData" :metric="metric" @day-click="onDayClick" />
    </div>

    <!-- 活跃计划 -->
    <div v-if="activePlans.length" class="section-card">
      <h3 class="sh">正在成长的计划</h3>
      <div class="active-plans-grid">
        <div v-for="ap in activePlans" :key="ap.id" class="ap-card">
          <div class="ap-top">
            <span class="ap-cat">{{ ap.category }}</span>
            <span class="ap-progress">{{ ap.progress }}%</span>
          </div>
          <div class="ap-name">{{ ap.name }}</div>
          <div class="ap-meta">
            <span>本周 {{ planWeekMins(ap.id) }}min</span>
            <span>最近 {{ ap.lastStudyTime ? ap.lastStudyTime.slice(0,10) : '—' }}</span>
          </div>
          <div class="ap-prog-bar"><div class="ap-prog-fill" :style="{ width: ap.progress + '%' }"></div></div>
        </div>
      </div>
    </div>

    <!-- 近期时间线 -->
    <div class="section-card">
      <h3 class="sh">近期学习记录</h3>
      <div v-if="recentTimeline.length === 0" class="empty">还没有学习记录</div>
      <div v-for="rec in recentTimeline" :key="rec.id" class="tl-item">
        <span class="tl-date">{{ rec.date?.slice(0,10) }} {{ rec.date?.slice(11,16) }}</span>
        <span class="tl-content">{{ rec.content || (rec.itemTitle ? '完成「' + rec.itemTitle + '」' : '专注学习') }}</span>
        <span class="tl-meta">{{ rec.duration }}min · {{ rec.category }}</span>
      </div>
    </div>

    <!-- 日期详情抽屉 -->
    <DayDetailDrawer v-model:visible="detailVisible" :day="detailDay" />

    <!-- 空状态 -->
    <div v-if="!store.checkins.length" class="empty-hero">
      <p>还没有学习足迹，选择一个计划开始第一次专注吧。</p>
      <router-link to="/plans" class="btn-start">🍅 开始专注</router-link>
    </div>
    </div><!-- /growth-main -->

    <!-- 右侧栏 -->
    <div class="growth-sidebar">
      <FocusDonut v-model:period="donutPeriod" :filter-category="filterCategory" :filter-plan-id="filterPlanId" />
      <DailyGoal />
      <PlantCard />
    </div>
  </div><!-- /growth-dashboard -->
</template>

<style scoped>
.growth-dashboard { width: min(1500px, calc(100% - 48px)); margin: 0 auto; padding: 20px 0; display: grid; grid-template-columns: minmax(0, 1fr) 400px; gap: 24px; align-items: start; }
.growth-main { min-width: 0; }
.growth-sidebar { display: flex; flex-direction: column; gap: 24px; }
.gh-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 16px; }
.gh-header h2 { font-size: 22px; font-weight: 700; }
.gh-filters { display: flex; gap: 6px; flex-wrap: wrap; }
.gsel { padding: 6px 10px; border: 1px solid var(--color-border); border-radius: 6px; font-size: 12px; background: #fff; color: var(--color-body); }

.stats-row { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 100px; background: rgba(255,255,255,0.8); border-radius: 8px; padding: 14px; text-align: center; border: 1px solid var(--color-border-light); }
.sn { font-size: 22px; font-weight: 700; color: var(--color-primary); display: block; }
.sl { font-size: 11px; color: var(--color-muted); margin-top: 2px; display: block; }

.section-card { background: rgba(255,255,255,0.8); border-radius: 8px; padding: 18px; border: 1px solid var(--color-border-light); margin-bottom: 16px; }
.sh { font-size: 16px; font-weight: 600; margin-bottom: 12px; }

.active-plans-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 10px; }
.ap-card { padding: 12px; border: 1px solid var(--color-border-light); border-radius: 8px; background: #fff; }
.ap-top { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 4px; }
.ap-cat { color: var(--color-primary); font-weight: 500; }
.ap-progress { color: var(--color-primary); font-weight: 700; }
.ap-name { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.ap-meta { font-size: 11px; color: var(--color-muted); display: flex; gap: 12px; margin-bottom: 6px; }
.ap-prog-bar { height: 4px; background: var(--color-border-light); border-radius: 2px; overflow: hidden; }
.ap-prog-fill { height: 100%; background: var(--color-primary); border-radius: 2px; }

.tl-item { display: flex; gap: 10px; padding: 8px 0; border-bottom: 1px solid var(--color-border-light); font-size: 13px; align-items: center; }
.tl-date { color: var(--color-muted); font-size: 12px; white-space: nowrap; min-width: 100px; }
.tl-content { flex: 1; color: var(--color-body); }
.tl-meta { color: var(--color-primary); font-size: 12px; white-space: nowrap; }

.empty, .empty-hero { text-align: center; padding: 48px; color: var(--color-muted); font-size: 14px; }
.empty-hero { padding: 80px 24px; }
.btn-start { display: inline-block; margin-top: 16px; padding: 10px 24px; background: var(--color-primary); color: #fff; border-radius: 8px; text-decoration: none; font-weight: 600; }

@media (max-width: 900px) {
  .growth-dashboard { grid-template-columns: 1fr; width: 100%; padding: 12px; }
  .growth-sidebar { gap: 16px; }
}
@media (max-width: 768px) {
  .growth-dashboard { padding: 8px; }
  .stats-row { gap: 6px; }
  .stat-card { padding: 10px; }
  .sn { font-size: 18px; }
}
</style>
