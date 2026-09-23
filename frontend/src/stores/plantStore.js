import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useStudyPlanStore } from './studyPlan'

const LS_KEY = 'growth_plant_v1'

const PLANTS = {
  orange: { name: '橙子树', desc: '持续积累，最终开花结果', stages: ['种子','萌芽','幼苗','成长','枝繁叶茂','开花','成熟'] },
  sunflower: { name: '向日葵', desc: '保持规律，向着目标生长', stages: ['种子','萌芽','幼苗','成长','枝繁叶茂','开花','成熟'] },
  sakura: { name: '樱花', desc: '记录每一次专注与绽放', stages: ['种子','萌芽','幼苗','成长','枝繁叶茂','开花','成熟'] },
  bamboo: { name: '竹子', desc: '前期扎根，后期快速成长', stages: ['种子','萌芽','幼苗','成长','枝繁叶茂','开花','成熟'] },
  succulent: { name: '多肉', desc: '轻松陪伴，适合稳定积累', stages: ['种子','萌芽','幼苗','成长','枝繁叶茂','开花','成熟'] }
}

const STAGE_RULES = [
  { stage: 0, name: '种子', mins: 0, days: 0 },
  { stage: 1, name: '萌芽', mins: 60, days: 1 },
  { stage: 2, name: '幼苗', mins: 300, days: 3 },
  { stage: 3, name: '成长', mins: 900, days: 7 },
  { stage: 4, name: '枝繁叶茂', mins: 1800, days: 14 },
  { stage: 5, name: '开花', mins: 3000, days: 21 },
  { stage: 6, name: '成熟', mins: 4800, days: 30 }
]

function load() { try { const r = localStorage.getItem(LS_KEY); if (r) return JSON.parse(r) } catch(e){} return null }
function save(data) { localStorage.setItem(LS_KEY, JSON.stringify(data)) }

export const usePlantStore = defineStore('plant', () => {
  const planStore = useStudyPlanStore()
  const plant = ref(load())

  const currentStage = computed(() => {
    if (!plant.value) return null
    const { totalMins, activeDays } = getStats()
    for (let i = STAGE_RULES.length - 1; i >= 0; i--) {
      if (totalMins >= STAGE_RULES[i].mins && activeDays >= STAGE_RULES[i].days) {
        // 幂等：阶段事件只记录一次
        if (plant.value.currentStage < i) {
          plant.value.currentStage = i
          plant.value.stageEvents = plant.value.stageEvents || []
          const evKey = `stage_${i}`
          if (!plant.value.stageEvents.find(e => e.key === evKey)) {
            plant.value.stageEvents.push({ key: evKey, stage: i, name: STAGE_RULES[i].name, focusMins: totalMins, activeDays, reachedAt: new Date().toISOString() })
          }
          if (i === STAGE_RULES.length - 1) plant.value.maturedAt = new Date().toISOString()
          save(plant.value)
        }
        return i
      }
    }
    return 0
  })

  function getStats() {
    if (!plant.value) return { totalMins: 0, activeDays: 0, checkins: [] }
    let checkins = [...planStore.checkins]
    // 按成长范围筛选
    if (plant.value.scopeType === 'category') checkins = checkins.filter(c => {
      const p = planStore.getPlanById(c.planId); return p?.category === plant.value.scopeId
    })
    else if (plant.value.scopeType === 'plan') checkins = checkins.filter(c => c.planId === plant.value.scopeId)

    const dayMap = {}
    let totalMins = 0
    for (const c of checkins) {
      if (!c.duration || c.duration <= 0) continue
      const d = c.date?.slice(0, 10)
      if (!d) continue
      dayMap[d] = (dayMap[d] || 0) + c.duration
      totalMins += c.duration
    }
    const activeDays = Object.values(dayMap).filter(m => m >= 25).length
    return { totalMins, activeDays, checkins }
  }

  function initPlant(type, scopeType, scopeId) {
    plant.value = {
      id: 'p_' + Date.now(), type, scopeType: scopeType || 'all', scopeId: scopeId || null,
      currentStage: 0, startedAt: new Date().toISOString(), status: 'growing',
      stageEvents: [{ key: 'stage_0', stage: 0, name: '种子', focusMins: 0, activeDays: 0, reachedAt: new Date().toISOString() }],
      garden: []
    }
    save(plant.value)
  }

  function changePlantAppearance(newType) {
    if (!plant.value) return
    plant.value.type = newType
    save(plant.value)
  }

  function resetPlant(type, scopeType, scopeId) {
    // 保存旧植物到花园
    if (plant.value && plant.value.currentStage >= STAGE_RULES.length - 1) {
      plant.value.garden = plant.value.garden || []
      plant.value.garden.push({
        type: plant.value.type, scopeType: plant.value.scopeType, scopeId: plant.value.scopeId,
        startedAt: plant.value.startedAt, maturedAt: plant.value.maturedAt || new Date().toISOString(),
        totalMins: getStats().totalMins, activeDays: getStats().activeDays
      })
    }
    initPlant(type, scopeType, scopeId)
  }

  const todayMins = computed(() => {
    const today = new Date().toISOString().slice(0, 10)
    return planStore.checkins.filter(c => c.date?.startsWith(today)).reduce((s, c) => s + (c.duration || 0), 0)
  })
  const wateredToday = computed(() => todayMins.value >= 25)
  const isDormant = computed(() => {
    if (!plant.value) return false
    const stats = getStats()
    // 连续7天无有效专注 = 休眠
    let streak = 0
    const d = new Date()
    for (let i = 0; i < 7; i++) {
      const key = d.toISOString().slice(0, 10)
      const mins = planStore.checkins.filter(c => c.date?.startsWith(key)).reduce((s, c) => s + (c.duration || 0), 0)
      if (mins >= 25) break
      streak++
      d.setDate(d.getDate() - 1)
    }
    return streak >= 7
  })

  function nextStageInfo() {
    const s = currentStage.value ?? 0
    if (s >= STAGE_RULES.length - 1) return null
    const next = STAGE_RULES[s + 1]
    const stats = getStats()
    return {
      name: next.name, needMins: Math.max(0, next.mins - stats.totalMins),
      needDays: Math.max(0, next.days - stats.activeDays),
      timePct: Math.min(100, Math.round(stats.totalMins / next.mins * 100)),
      dayPct: Math.min(100, Math.round(stats.activeDays / next.days * 100))
    }
  }

  function plantStatus() {
    if (!plant.value) return null
    if (currentStage.value >= STAGE_RULES.length - 1) return 'matured'
    if (isDormant.value) return 'dormant'
    if (wateredToday.value) return 'watered'
    return 'growing'
  }

  return {
    plant, PLANTS, STAGE_RULES, currentStage, todayMins, wateredToday, isDormant,
    initPlant, changePlantAppearance, resetPlant, getStats, nextStageInfo, plantStatus
  }
})
