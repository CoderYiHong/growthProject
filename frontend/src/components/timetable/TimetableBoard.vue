<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useAppStore } from '@/stores/app'
import {
  DAYS, SLOTS, paletteOf,
  parseDateStr, mondayOf, rawWeekOf, clampWeek, currentWeek,
  fmtMonthDay, fmtFull, inWeek
} from '@/utils/timetable'

const props = defineProps({
  courses: { type: Array, default: () => [] },
  entries: { type: Array, default: () => [] },
  semester: { type: Object, default: () => ({ startDate: '', totalWeeks: 0, examStartWeek: 0 }) },
  title: { type: String, default: '' }
})

const appStore = useAppStore()
const isDark = computed(() => appStore.theme === 'dark')

// ── 学期 ──
const startDate = computed(() => parseDateStr(props.semester?.startDate))
const totalWeeks = computed(() => Number(props.semester?.totalWeeks) || 0)
const examStartWeek = computed(() => Number(props.semester?.examStartWeek) || 0)
const configured = computed(() => !!startDate.value && totalWeeks.value > 0)

const week = ref(1)

watch([startDate, totalWeeks], () => {
  week.value = configured.value ? currentWeek(startDate.value, totalWeeks.value) : 1
}, { immediate: true })

// ── 本周信息 ──
const weekMon = computed(() => configured.value ? mondayOf(startDate.value, week.value) : null)
const weekSun = computed(() => configured.value ? mondayOf(startDate.value, week.value + 1 - 1) : null)
const rawWeek = computed(() => configured.value ? rawWeekOf(startDate.value, new Date()) : 0)
const weekPill = computed(() => {
  if (!configured.value) return ''
  if (rawWeek.value === week.value) return '本周'
  if (week.value >= examStartWeek.value) return '考试周'
  return ''
})

const weekOptions = computed(() => {
  const opts = []
  for (let w = 1; w <= totalWeeks.value; w++) {
    const mon = mondayOf(startDate.value, w)
    const sun = mondayOf(startDate.value, w + 1 - 1)
    opts.push({
      value: w,
      label: `第${w}周 (${fmtMonthDay(mon)}-${fmtMonthDay(sun)})${w >= examStartWeek.value ? ' · 考试周' : ''}`
    })
  }
  return opts
})

// ── 本周课程 ──
const courseById = computed(() => {
  const m = new Map()
  props.courses.forEach(c => { if (c.id != null) m.set(c.id, c) })
  return m
})
const courseByIndex = computed(() => {
  const m = new Map()
  props.courses.forEach((c, i) => m.set(i, c))
  return m
})

const weekEntries = computed(() => {
  if (!configured.value) return []
  return props.entries
    .filter(e => inWeek(e, week.value))
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
})

function resolveCourse(e) {
  if (e.courseId != null) return courseById.value.get(e.courseId)
  return courseByIndex.value.get(e.courseIndex)
}

const weekStat = computed(() => {
  if (!configured.value) return ''
  const count = weekEntries.value.length
  return week.value <= (examStartWeek.value - 1) || examStartWeek.value <= 0
    ? `本周共 ${count * 2} 节课 · ${count} 个课次`
    : '教学周已结束，进入考试周'
})

// ── 表头日期与今天 ──
function dayDate(i) {
  return weekMon.value ? mondayOf(weekMon.value, i) : null
}
function isToday(i) {
  if (!weekMon.value || rawWeek.value !== week.value) return false
  const d = dayDate(i)
  const now = new Date()
  return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()
}

// ── 图例联动 ──
const hoverKey = ref(null)
function courseKey(e) {
  const c = resolveCourse(e)
  if (c?.id != null) return 'id:' + c.id
  return 'idx:' + (e.courseIndex ?? '')
}

function cellStyle(e) {
  const c = resolveCourse(e)
  const p = paletteOf(c?.color || 1, isDark.value)
  return { background: p.bg, color: p.ink }
}

// ── 悬浮提示 ──
const tip = ref({ show: false, x: 0, y: 0, html: '' })
function showTip(ev, e) {
  const c = resolveCourse(e)
  if (!c) return
  const parts = [
    `<b>${c.name}</b>`,
    `周次：第${e.startWeek}-${e.endWeek}周`,
    `地点：${e.room || '—'}`
  ]
  const meta = [`教师：${c.teacher || '—'}`]
  if (c.title) meta.push(`职称：${c.title}`)
  meta.push(`学分：${c.credit ?? '—'}`)
  parts.push(meta.join(' · '))
  tip.value.html = parts.join('<br>')
  tip.value.show = true
  moveTip(ev)
}
function moveTip(ev) {
  const pad = 12
  let x = ev.clientX + pad
  let y = ev.clientY + pad
  tip.value.x = x
  tip.value.y = y
}
function hideTip() { tip.value.show = false }

// ── 周切换 ──
function gotoWeek(w) {
  week.value = clampWeek(w, totalWeeks.value)
}
function prevWeek() { gotoWeek(week.value - 1) }
function nextWeek() { gotoWeek(week.value + 1) }
function gotoToday() { week.value = currentWeek(startDate.value, totalWeeks.value) }
const jumpDate = ref('')
function onDateJump(val) {
  if (!val || !configured.value) return
  const d = parseDateStr(val)
  const raw = rawWeekOf(startDate.value, d)
  week.value = clampWeek(raw || 1, totalWeeks.value)
}

function onKeydown(e) {
  const tag = e.target?.tagName
  if (tag === 'SELECT' || tag === 'INPUT' || tag === 'TEXTAREA') return
  if (e.key === 'ArrowLeft') prevWeek()
  if (e.key === 'ArrowRight') nextWeek()
}
onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))

// ── 主题 ──
function toggleTheme() { appStore.toggleTheme() }
</script>

<template>
  <div class="tt-board">
    <!-- 标题 -->
    <h1 v-if="title" class="tt-title">{{ title }}</h1>

    <!-- 学期未配置 -->
    <div v-if="!configured" class="tt-empty">
      <p>学期尚未配置</p>
      <p class="tt-empty-hint">请先在后台「课表管理 → 学期设置」中填写开学日期和总周数</p>
    </div>

    <template v-else>
      <!-- 控制栏 -->
      <div class="tt-bar">
        <button class="tt-btn" @click="prevWeek" title="上一周">◀ 上一周</button>
        <select class="tt-select" :value="week" @change="e => gotoWeek(Number(e.target.value))" aria-label="选择教学周">
          <option v-for="opt in weekOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
        <button class="tt-btn" @click="nextWeek" title="下一周">下一周 ▶</button>
        <el-date-picker
          v-model="jumpDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="按日期跳转"
          size="small"
          style="width: 140px"
          @change="onDateJump"
        />
        <button class="tt-btn tt-ghost" @click="gotoToday">回到本周</button>
        <button class="tt-btn tt-ghost" @click="toggleTheme" title="切换日间/夜间模式">{{ isDark ? '☀️' : '🌙' }}</button>
      </div>

      <!-- 周标题 -->
      <div class="tt-weektitle">
        第{{ week }}周
        <span v-if="weekPill" class="tt-pill">{{ weekPill }}</span>
      </div>
      <div v-if="weekMon" class="tt-weekrange">{{ fmtFull(weekMon) }} – {{ fmtFull(weekSun) }}</div>
      <div class="tt-stat">{{ weekStat }}</div>

      <!-- 图例 -->
      <div class="tt-legend">
        <div
          v-for="c in courses"
          :key="'legend-' + (c.id ?? courses.indexOf(c))"
          class="tt-li"
          @mouseenter="hoverKey = (c.id != null ? 'id:' + c.id : 'idx:' + courses.indexOf(c))"
          @mouseleave="hoverKey = null"
        >
          <span class="tt-sw" :style="{ background: paletteOf(c.color || 1, isDark).bg }"></span>
          {{ c.name }}<small>{{ c.teacher }} · {{ c.credit }}学分</small>
        </div>
      </div>

      <!-- 网格 -->
      <div class="tt-tablewrap">
        <table class="tt-grid" :class="{ dim: hoverKey !== null }">
          <thead>
            <tr>
              <th style="width: 62px">节次</th>
              <th v-for="(d, i) in DAYS" :key="d" :style="isToday(i) ? { background: 'var(--tt-gridline)' } : {}">
                {{ d }}
                <span class="tt-d">{{ weekMon ? fmtMonthDay(dayDate(i)) : '' }}</span>
                <span v-if="isToday(i)" class="tt-tag">今天</span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(s, si) in SLOTS" :key="si">
              <td class="tt-time">{{ s.label }}<br>{{ s.sub }}</td>
              <td v-for="d in 7" :key="d">
                <div
                  v-for="e in weekEntries.filter(x => x.dayOfWeek === d && x.slot === si)"
                  :key="(e.id ?? '') + '-' + d + '-' + si"
                  class="scell"
                  :class="{ match: hoverKey === null || hoverKey === courseKey(e) }"
                  :style="cellStyle(e)"
                  tabindex="0"
                  @mouseenter="ev => showTip(ev, e)"
                  @mousemove="moveTip"
                  @mouseleave="hideTip"
                  @focus="ev => showTip(ev, e)"
                  @blur="hideTip"
                >
                  <span class="n">{{ resolveCourse(e)?.name }}</span>
                  <span class="m">{{ e.room }}<br>{{ resolveCourse(e)?.teacher }}</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 考试周提示 -->
      <div v-if="examStartWeek > 0 && week >= examStartWeek" class="tt-exam">
        第{{ week }}周为考试周，课表无课程安排（第1-{{ examStartWeek - 1 }}周为教学周）。
      </div>

      <!-- 空课表提示 -->
      <div v-if="!entries.length" class="tt-exam">
        课表为空，可前往后台「课表管理」导入或添加课程。
      </div>

      <div class="tt-notes">上午第1-4节 · 下午第5-8节 · 晚上(9-12节)无课<br>悬停课程卡片可查看详情 · 按 ← → 方向键可切换周次</div>
    </template>

    <!-- 悬浮提示 -->
    <div v-if="tip.show" class="tt-tip" :style="{ left: tip.x + 'px', top: tip.y + 'px' }" v-html="tip.html"></div>
  </div>
</template>

<style scoped>
/* 调色板（亮色） */
.tt-board {
  --tt-c1: #2a78d6; --tt-c2: #1baf7a; --tt-c3: #eda100; --tt-c4: #008300;
  --tt-c5: #4a3aa7; --tt-c6: #e34948; --tt-c7: #e87ba4; --tt-c8: #eb6834;
  --tt-plane: #f9f9f7; --tt-surface: #fcfcfb; --tt-ink: #0b0b0b;
  --tt-ink2: #52514e; --tt-muted: #898781; --tt-gridline: #e1e0d9;
  --tt-hairline: rgba(11, 11, 11, 0.10);
}
/* 调色板（暗色）— 站点 data-theme 挂在 html 上 */
html[data-theme='dark'] .tt-board {
  --tt-c1: #3987e5; --tt-c2: #199e70; --tt-c3: #c98500; --tt-c4: #008300;
  --tt-c5: #9085e9; --tt-c6: #e66767; --tt-c7: #d55181; --tt-c8: #d95926;
  --tt-plane: #0d0d0d; --tt-surface: #1a1a19; --tt-ink: #ffffff;
  --tt-ink2: #c3c2b7; --tt-muted: #898781; --tt-gridline: #2c2c2a;
  --tt-hairline: rgba(255, 255, 255, 0.10);
}

.tt-title { font-size: 19px; text-align: center; color: var(--tt-ink); }

.tt-empty { text-align: center; padding: 48px 0; color: var(--tt-ink); font-size: 15px; }
.tt-empty-hint { color: var(--tt-muted); font-size: 13px; margin-top: 8px; }

/* 控制栏 */
.tt-bar { display: flex; gap: 8px; align-items: center; justify-content: center; flex-wrap: wrap; margin-bottom: 6px; }
.tt-btn, .tt-select {
  font: inherit; font-size: 13px; color: var(--tt-ink); background: var(--tt-surface);
  border: 1px solid var(--tt-hairline); border-radius: 6px; padding: 6px 10px; cursor: pointer;
}
.tt-btn:hover, .tt-select:hover { border-color: var(--tt-ink2); }
.tt-ghost { opacity: 0.85; }

/* 周标题 */
.tt-weektitle { text-align: center; font-size: 16px; font-weight: 700; margin-top: 10px; color: var(--tt-ink); }
.tt-pill {
  display: inline-block; font-size: 11px; font-weight: 600; border-radius: 999px;
  padding: 2px 8px; margin-left: 6px; vertical-align: 2px;
  background: var(--tt-ink); color: var(--tt-surface);
}
.tt-weekrange { text-align: center; color: var(--tt-ink2); font-size: 13px; margin: 4px 0 2px; }
.tt-stat { text-align: center; color: var(--tt-muted); font-size: 12.5px; margin-bottom: 12px; }

/* 图例 */
.tt-legend { display: flex; flex-wrap: wrap; gap: 6px; justify-content: center; margin: 10px 0 12px; }
.tt-li {
  display: flex; align-items: center; gap: 6px; background: var(--tt-surface);
  border: 1px solid var(--tt-hairline); border-radius: 6px; padding: 4px 9px;
  font-size: 12px; cursor: default; color: var(--tt-ink);
}
.tt-li small { color: var(--tt-muted); }
.tt-sw { width: 12px; height: 12px; border-radius: 3px; flex: none; }

/* 表格 */
.tt-tablewrap { background: var(--tt-gridline); border-radius: 10px; padding: 2px; overflow-x: auto; }
.tt-grid { border-collapse: separate; border-spacing: 2px; width: 100%; min-width: 800px; }
.tt-grid th, .tt-grid td { background: var(--tt-surface); border-radius: 5px; padding: 6px 5px; text-align: center; vertical-align: top; }
.tt-grid th { font-size: 12.5px; font-weight: 600; color: var(--tt-ink2); padding: 8px 4px; }
.tt-grid th .tt-d { display: block; font-weight: 400; color: var(--tt-muted); font-size: 11.5px; margin-top: 2px; font-variant-numeric: tabular-nums; }
.tt-grid th .tt-tag {
  display: inline-block; background: var(--tt-ink); color: var(--tt-surface);
  font-size: 10px; font-weight: 600; border-radius: 999px; padding: 1px 7px; margin-top: 3px;
}
.tt-time { width: 62px; font-size: 11.5px; color: var(--tt-muted); vertical-align: middle; line-height: 1.6; }

/* 课程格子（不可用 -card 后缀，避免全局毛玻璃规则） */
.scell { border-radius: 6px; padding: 7px 6px; font-size: 12.5px; line-height: 1.35; text-align: left; min-height: 54px; }
.scell:hover { filter: brightness(1.07); box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18); }
.scell .n { font-weight: 700; font-size: 13px; }
.scell .m { display: block; font-size: 11.5px; opacity: 0.93; margin-top: 3px; }
.tt-grid.dim .scell:not(.match) { opacity: 0.32; }

/* 考试周 / 提示 */
.tt-exam {
  text-align: center; margin-top: 14px; padding: 14px; border-radius: 8px;
  border: 1px dashed var(--tt-gridline); color: var(--tt-ink2); font-size: 13px;
}
.tt-notes { font-size: 12px; color: var(--tt-muted); text-align: center; margin-top: 14px; line-height: 1.9; }

/* 悬浮提示 */
.tt-tip {
  position: fixed; z-index: 9999; background: var(--tt-ink); color: var(--tt-surface);
  font-size: 12px; padding: 8px 11px; border-radius: 6px; max-width: 280px;
  line-height: 1.6; pointer-events: none; box-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);
}
.tt-tip b { font-size: 12.5px; }
</style>
