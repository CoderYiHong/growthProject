/**
 * 课表领域工具 — 从独立课表页（课表.html）移植的纯函数
 */

export const DAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

export const SLOTS = [
  { label: '上午', sub: '第1-2节' },
  { label: '上午', sub: '第3-4节' },
  { label: '下午', sub: '第5-6节' },
  { label: '下午', sub: '第7-8节' }
]

export const SLOT_TEXT = ['第1-2节', '第3-4节', '第5-6节', '第7-8节']

/** 课程调色板（亮色）— 与课表.html 的 :root 一致 */
export const PALETTE = [
  { bg: '#2a78d6', ink: '#000000' },
  { bg: '#1baf7a', ink: '#000000' },
  { bg: '#eda100', ink: '#000000' },
  { bg: '#008300', ink: '#ffffff' },
  { bg: '#4a3aa7', ink: '#ffffff' },
  { bg: '#e34948', ink: '#000000' },
  { bg: '#e87ba4', ink: '#000000' },
  { bg: '#eb6834', ink: '#000000' }
]

/** 课程调色板（暗色） */
export const PALETTE_DARK = [
  { bg: '#3987e5', ink: '#000000' },
  { bg: '#199e70', ink: '#000000' },
  { bg: '#c98500', ink: '#000000' },
  { bg: '#008300', ink: '#ffffff' },
  { bg: '#9085e9', ink: '#000000' },
  { bg: '#e66767', ink: '#000000' },
  { bg: '#d55181', ink: '#000000' },
  { bg: '#d95926', ink: '#000000' }
]

/** 按颜色编号取调色板项（1-8，越界取 1） */
export function paletteOf(color, dark = false) {
  const list = dark ? PALETTE_DARK : PALETTE
  const idx = Math.min(Math.max((color || 1) - 1, 0), list.length - 1)
  return list[idx]
}

const DAY_MS = 86400000

/** 'yyyy-MM-dd' → 本地 Date（无时区偏移问题） */
export function parseDateStr(s) {
  if (!s) return null
  const parts = String(s).split('-').map(Number)
  if (parts.length !== 3 || parts.some(isNaN)) return null
  return new Date(parts[0], parts[1] - 1, parts[2])
}

export function addDays(d, n) {
  const x = new Date(d)
  x.setDate(x.getDate() + n)
  return x
}

export function fmtMonthDay(d) {
  return (d.getMonth() + 1) + '月' + d.getDate() + '日'
}

export function fmtFull(d) {
  return d.getFullYear() + '年' + fmtMonthDay(d)
}

/** 开学日所在日历周的周一（周次按自然周 周一~周日 锚定，开学日可为任意星期） */
export function weekMondayOf(startDate) {
  const offset = (startDate.getDay() + 6) % 7 // 周一=0 ... 周日=6
  return addDays(startDate, -offset)
}

/** 第 week 周的周一 */
export function mondayOf(startDate, week) {
  return addDays(weekMondayOf(startDate), (week - 1) * 7)
}

/** 未开学=0，开学后按自然周计 */
export function rawWeekOf(startDate, date) {
  const week1Mon = weekMondayOf(startDate)
  const days = Math.floor((new Date(date.getFullYear(), date.getMonth(), date.getDate()) - week1Mon) / DAY_MS)
  return days < 0 ? 0 : Math.floor(days / 7) + 1
}

export function clampWeek(w, totalWeeks) {
  return Math.max(1, Math.min(totalWeeks, w))
}

export function currentWeek(startDate, totalWeeks) {
  return clampWeek(rawWeekOf(startDate, new Date()), totalWeeks)
}

/** 排课是否落在第 w 周 */
export function inWeek(entry, w) {
  return entry.startWeek <= w && w <= entry.endWeek
}
