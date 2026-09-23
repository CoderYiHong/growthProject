<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTimetable } from '@/api/public'
import TimetableBoard from '@/components/timetable/TimetableBoard.vue'

const loading = ref(true)
const semester = ref({ startDate: '', totalWeeks: 0, examStartWeek: 0 })
const courses = ref([])
const entries = ref([])

const subtitle = computed(() => {
  const s = semester.value
  if (!s.startDate) return '课表数据由后台管理，尚未配置学期'
  const parts = [`开学：${s.startDate}`, `共 ${s.totalWeeks} 周`]
  if (s.examStartWeek > 0) parts.push(`第 ${s.examStartWeek} 周起考试周`)
  return parts.join(' · ')
})

onMounted(async () => {
  try {
    const res = await getTimetable()
    if (res.code === 200 && res.data) {
      semester.value = res.data.semester || semester.value
      courses.value = res.data.courses || []
      entries.value = res.data.entries || []
    }
  } catch {
    // 后端不可用时保留空状态，页面不崩溃
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="timetable-page">
    <!-- 遮罩 -->
    <div class="tt-page-bg"></div>

    <div class="tt-page-container" v-loading="loading">
      <!-- 头部 -->
      <div class="tt-page-head">
        <h2>课程表</h2>
        <p class="tt-page-sub">{{ subtitle }}</p>
      </div>

      <!-- 课表主体 -->
      <TimetableBoard :courses="courses" :entries="entries" :semester="semester" />
    </div>
  </div>
</template>

<style scoped>
.timetable-page { position: relative; min-height: 100vh; }
.tt-page-bg { position: fixed; inset: 0; background: rgba(239,241,235,0.55); pointer-events: none; z-index: 0; }
.tt-page-container { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; padding: 24px 28px 64px; }

.tt-page-head { text-align: center; margin-bottom: 18px; }
.tt-page-head h2 { font-size: 22px; font-weight: 700; color: #17231b; margin: 0; }
.tt-page-sub { font-size: 13px; color: #65766b; margin: 6px 0 0; }

@media (max-width: 768px) {
  .tt-page-container { padding: 12px; }
}
</style>
