<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStudyPlanStore } from '@/stores/studyPlan'
import { getSiteSettings } from '@/api/public'

const store = useStudyPlanStore()
const goalMins = ref(120)

onMounted(async () => {
  try {
    const res = await getSiteSettings()
    if (res.code === 200 && res.data?.dailyGoalMins) {
      const val = parseInt(res.data.dailyGoalMins, 10)
      if (val > 0) goalMins.value = val
    }
  } catch { /* 使用默认值 120 */ }
})

const todayMins = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return store.checkins.filter(c => c.date?.startsWith(today)).reduce((s, c) => s + (c.duration || 0), 0)
})
const pct = computed(() => Math.min(100, Math.round(todayMins.value / goalMins.value * 100)))
</script>

<template>
  <div class="goal-card">
    <h4>今日专注目标</h4>
    <div class="goal-nums">{{ todayMins }} <span>/ {{ goalMins }} 分钟</span></div>
    <div class="goal-bar"><div class="goal-fill" :style="{ width: pct + '%' }"></div></div>
    <div class="goal-pct">{{ pct }}%</div>
    <div v-if="pct >= 100" class="goal-done">🎉 今日目标已完成</div>
    <div v-else class="goal-hint">每日专注目标 {{ goalMins }} 分钟</div>
  </div>
</template>

<style scoped>
.goal-card { background: rgba(255,255,255,0.94); border-radius: 12px; padding: 18px; border: 1px solid #d9e3da; }
.goal-card h4 { font-size: 15px; font-weight: 700; color: #17231b; margin-bottom: 8px; }
.goal-nums { font-size: 24px; font-weight: 800; color: #17231b; }
.goal-nums span { font-size: 14px; font-weight: 400; color: #65766b; }
.goal-bar { height: 8px; background: #e6ebe6; border-radius: 4px; margin-top: 6px; overflow: hidden; }
.goal-fill { height: 100%; background: #2f9142; border-radius: 4px; transition: width 0.5s; }
.goal-pct { font-size: 12px; color: #2f9142; font-weight: 600; margin-top: 2px; }
.goal-done { font-size: 13px; color: #2f9142; font-weight: 600; margin-top: 4px; }
.goal-hint { font-size: 11px; color: #b0b8b2; margin-top: 4px; }
</style>
