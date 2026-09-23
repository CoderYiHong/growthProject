<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import * as echarts from 'echarts'
import { useStudyPlanStore } from '@/stores/studyPlan'

const props = defineProps({ period: String, filterCategory: String, filterPlanId: String })
const emit = defineEmits(['slice-click'])

const store = useStudyPlanStore()
const chartRef = ref(null)
let chart = null

const CAT_COLORS = { '技术学习': '#4fae61', '考研学习': '#e6a23c', '英语学习': '#4b91d1', '自媒体运营': '#9b72cf' }

const slices = computed(() => {
  const now = new Date()
  let cutoff = new Date()
  if (props.period === 'today') cutoff.setHours(0,0,0,0)
  else if (props.period === 'week') cutoff.setDate(now.getDate() - 7)
  else if (props.period === 'month') cutoff.setMonth(now.getMonth() - 1)
  else cutoff.setFullYear(now.getFullYear() - 1)

  const map = {}
  for (const c of store.checkins) {
    if (!c.duration || new Date(c.date) < cutoff) continue
    const plan = store.getPlanById(c.planId)
    let key = plan?.category || '其他'
    if (props.filterCategory && props.filterCategory !== 'all' && key !== props.filterCategory) continue
    if (props.filterPlanId && props.filterPlanId !== 'all') {
      if (c.planId === props.filterPlanId) {
        const group = plan?.groups?.find(g => g.id === c.groupId)
        key = group?.name || plan?.name || key
      } else continue
    }
    map[key] = (map[key] || 0) + c.duration
  }
  const total = Object.values(map).reduce((s, v) => s + v, 0)
  return Object.entries(map).map(([name, mins]) => ({ name, value: mins, pct: total > 0 ? Math.round(mins / total * 100) : 0 }))
})

const totalMins = computed(() => slices.value.reduce((s, v) => s + v.value, 0))

function fmtMins(m) {
  if (m < 60) return m + '分钟'
  const h = Math.floor(m / 60); const min = m % 60
  return min > 0 ? `${h}h${min}min` : `${h}h`
}

function render() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const data = slices.value
  chart.setOption({
    tooltip: { trigger: 'item', formatter: p => `${p.name}: ${fmtMins(p.value)} (${p.percent}%)` },
    series: [{
      type: 'pie', radius: ['60%', '82%'], center: ['50%', '50%'],
      itemStyle: { borderColor: '#fff', borderWidth: 3, borderRadius: 2 },
      label: { show: false },
      data: data.length ? data.map(d => ({ ...d, itemStyle: { color: CAT_COLORS[d.name] || '#aab5ad' } })) : [{ name: '暂无记录', value: 1, itemStyle: { color: '#e6ebe6' } }],
      silent: data.length === 0,
      emphasis: { scaleSize: 6 }
    }]
  })
  chart.off('click')
  chart.on('click', params => { if (data.length) emit('slice-click', params.name) })
}

watch(slices, render, { deep: true })
onMounted(() => { render(); window.addEventListener('resize', () => chart?.resize()) })
</script>

<template>
  <div class="donut-card">
    <h4>专注时间分布</h4>
    <div class="donut-period">
      <button v-for="p in [{k:'today',l:'今日'},{k:'week',l:'本周'},{k:'month',l:'本月'},{k:'year',l:'本年'}]" :key="p.k"
        :class="{ active: period === p.k }" @click="$emit('update:period', p.k)">{{ p.l }}</button>
    </div>
    <div ref="chartRef" style="width:240px;height:240px;margin:0 auto"></div>
    <div class="donut-center">{{ period === 'today' ? '今日' : period === 'week' ? '本周' : period === 'month' ? '本月' : '本年' }}专注</div>
    <div class="donut-total">{{ fmtMins(totalMins) }}</div>
    <div class="donut-legend" v-if="slices.length">
      <div v-for="s in slices" :key="s.name" class="dl-item">
        <span class="dl-dot" :style="{ background: CAT_COLORS[s.name] || '#aab5ad' }"></span>
        <span class="dl-name">{{ s.name }}</span>
        <span class="dl-val">{{ fmtMins(s.value) }}</span>
        <span class="dl-pct">{{ s.pct }}%</span>
      </div>
    </div>
    <div v-else class="donut-empty">暂无记录</div>
  </div>
</template>

<style scoped>
.donut-card { background: rgba(255,255,255,0.94); border-radius: 12px; padding: 18px; border: 1px solid #d9e3da; position: relative; }
.donut-card h4 { font-size: 15px; font-weight: 700; color: #17231b; margin-bottom: 8px; }
.donut-period { display: flex; gap: 4px; margin-bottom: 4px; }
.donut-period button { padding: 3px 10px; border: 1px solid #d9e3da; border-radius: 14px; font-size: 11px; background: #fff; cursor: pointer; color: #53675a; }
.donut-period button.active { background: #2f9142; color: #fff; border-color: #2f9142; }
.donut-center { text-align: center; font-size: 12px; color: #65766b; margin-top: -8px; }
.donut-total { text-align: center; font-size: 22px; font-weight: 800; color: #17231b; }
.donut-empty { text-align: center; color: #b0b8b2; font-size: 13px; padding: 16px; }
.donut-legend { margin-top: 8px; }
.dl-item { display: flex; align-items: center; gap: 6px; font-size: 11px; padding: 2px 0; }
.dl-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dl-name { flex: 1; color: #53675a; }
.dl-val { color: #34463a; font-weight: 600; }
.dl-pct { color: #65766b; min-width: 28px; text-align: right; }
</style>
