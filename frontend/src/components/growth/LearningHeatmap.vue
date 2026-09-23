<script setup>
import { computed } from 'vue'

const props = defineProps({
  year: Number,
  dailyData: Object,
  metric: { type: String, default: 'minutes' }
})
const emit = defineEmits(['day-click'])

// ── 颜色等级（opacity 始终为 1，不通过透明度制造深浅） ──
const COLORS = {
  empty: '#e6ebe6',
  l1: '#c6e6c9',
  l2: '#8fd195',
  l3: '#55b764',
  l4: '#2f9142',
  l5: '#176c2b'
}

function getColor(minutes) {
  if (!minutes || minutes <= 0) return COLORS.empty
  if (minutes <= 25) return COLORS.l1
  if (minutes <= 50) return COLORS.l2
  if (minutes <= 90) return COLORS.l3
  if (minutes <= 150) return COLORS.l4
  return COLORS.l5
}

// ── 网格构建 ──
const weeks = computed(() => {
  const result = []
  const year = props.year
  const start = new Date(year, 0, 1)
  const startDay = start.getDay()
  const firstDate = new Date(start)
  firstDate.setDate(firstDate.getDate() - startDay)

  const now = new Date()
  const todayKey = now.toISOString().slice(0, 10)
  const endOfYear = new Date(year, 11, 31)
  const endDate = new Date(Math.min(now.getTime(), endOfYear.getTime()))

  let current = new Date(firstDate)
  let week = []
  while (current <= endDate) {
    for (let d = 0; d < 7; d++) {
      const key = current.toISOString().slice(0, 10)
      const data = props.dailyData[key]
      const val = data ? (props.metric === 'pomodoros' ? data.pomodoros : props.metric === 'items' ? data.items : data.minutes) : 0
      week.push({ date: key, value: val, isFuture: key > todayKey, records: data?.records || [] })
      current.setDate(current.getDate() + 1)
    }
    result.push([...week])
    week = []
  }
  return result
})

// ── 月份标签（用 position absolute 对齐每列） ──
const monthLabels = computed(() => {
  const labels = []
  let colIndex = 0
  let prevMonth = null
  for (const week of weeks.value) {
    const firstDay = week[0]
    const m = firstDay.date.slice(5, 7)
    if (m !== prevMonth) {
      labels.push({ month: m, label: Number(m) + '月', col: colIndex })
      prevMonth = m
    }
    colIndex++
  }
  return labels
})

const weekdays = ['一', '', '三', '', '五', '', '']

function fmtDuration(mins) {
  if (mins < 60) return mins + '分钟'
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return m > 0 ? `${h}小时${m}分钟` : `${h}小时`
}
</script>

<template>
  <div class="heatmap-wrap">
    <!-- 月份标签 -->
    <div class="heatmap-months">
      <span v-for="m in monthLabels" :key="m.month" class="hm" :style="{ left: (m.col * 17 + 20) + 'px' }">{{ m.label }}</span>
    </div>
    <!-- 主体 -->
    <div class="heatmap-body">
      <div class="heatmap-labels">
        <span v-for="(d, i) in weekdays" :key="i" class="hday">{{ d }}</span>
      </div>
      <div class="heatmap-grid">
        <template v-for="(week, wi) in weeks" :key="wi">
          <div
            v-for="(day, di) in week" :key="day.date"
            class="hcell"
            :class="{ future: day.isFuture, empty: !day.isFuture && day.value <= 0 }"
            :style="day.isFuture ? {} : { background: getColor(props.metric === 'minutes' ? day.value : props.metric === 'pomodoros' ? (day.value * 15) : (day.value * 10)) }"
            :title="day.isFuture ? '' : (day.date + '\n' + (day.value > 0 ? '专注 ' + fmtDuration(day.records.reduce((s,c)=>s+(c.duration||0),0)) : '当天没有学习记录'))"
            @click="!day.isFuture && day.value > 0 && emit('day-click', day)"
          ></div>
        </template>
      </div>
    </div>
    <!-- 图例 -->
    <div class="heatmap-legend">
      <span class="lg-label">少</span>
      <span class="lg-cell" style="background:#e6ebe6"></span>
      <span class="lg-cell" style="background:#c6e6c9"></span>
      <span class="lg-cell" style="background:#8fd195"></span>
      <span class="lg-cell" style="background:#55b764"></span>
      <span class="lg-cell" style="background:#2f9142"></span>
      <span class="lg-cell" style="background:#176c2b"></span>
      <span class="lg-label">多</span>
    </div>
  </div>
</template>

<style scoped>
.heatmap-wrap { overflow-x: auto; padding: 4px 0 0; }
.heatmap-months { position: relative; height: 18px; margin-left: 24px; margin-bottom: 2px; }
.hm { position: absolute; font-size: 12px; color: #607564; font-weight: 500; white-space: nowrap; }
.heatmap-body { display: flex; gap: 3px; }
.heatmap-labels { display: flex; flex-direction: column; gap: 3px; padding-right: 6px; padding-top: 0; }
.hday { font-size: 12px; color: #607564; height: 14px; line-height: 14px; width: 18px; text-align: right; }
.hday:nth-child(2), .hday:nth-child(4), .hday:nth-child(6), .hday:nth-child(7) { color: transparent; }
.heatmap-grid { display: grid; grid-auto-flow: column; grid-template-rows: repeat(7, 14px); gap: 3px; }
.hcell { width: 14px; height: 14px; border-radius: 2px; cursor: pointer; flex-shrink: 0; opacity: 1 !important; }
.hcell.empty { background: #e6ebe6; border: 1px solid #dce3dc; }
.hcell.future { background: transparent; border: 1px dashed #dce3dc; }
.hcell:not(.future):hover { outline: 2px solid #2f9142; outline-offset: -1px; z-index: 1; }
.heatmap-legend { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #607564; margin-top: 10px; justify-content: flex-end; padding-right: 4px; }
.lg-label { font-size: 12px; color: #607564; }
.lg-cell { display: inline-block; width: 14px; height: 14px; border-radius: 2px; opacity: 1 !important; flex-shrink: 0; }
.lg-cell:first-of-type { border: 1px solid #dce3dc; }
@media (max-width: 768px) { .heatmap-wrap { -webkit-overflow-scrolling: touch; } }
</style>
