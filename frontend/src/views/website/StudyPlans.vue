<script setup>
import { ref, computed } from 'vue'
import { useStudyPlanStore } from '@/stores/studyPlan'
import { useUserSkillStore } from '@/stores/userSkill'
import { planSheets, planStatuses, techCategories, examSubjects } from '@/data/studyPlans'
import { ElMessage } from 'element-plus'
import PomodoroTimer from '@/components/study/PomodoroTimer.vue'
import StudyPlanDetail from '@/components/study/StudyPlanDetail.vue'

const store = useStudyPlanStore()
const skillStore = useUserSkillStore()

const activeSheet = ref('all')
const plansForSheet = computed(() => activeSheet.value === 'all' ? [...store.plans] : store.plans.filter(p => p.category === activeSheet.value))
const today = new Date().toISOString().slice(0, 10)
const displayPlans = computed(() => plansForSheet.value.map(p => ({ ...p, _effectiveStatus: (p.endDate < today && p.status !== 'done' && p.status !== 'paused') ? 'overdue' : p.status })))

const filterStatus = ref(''); const filterType = ref(''); const searchKeyword = ref('')
const filteredPlans = computed(() => {
  let list = [...displayPlans.value]
  if (filterStatus.value) list = list.filter(p => p._effectiveStatus === filterStatus.value)
  if (filterType.value) list = list.filter(p => p.type === filterType.value)
  if (searchKeyword.value) { const kw = searchKeyword.value.toLowerCase(); list = list.filter(p => p.name.toLowerCase().includes(kw) || (p.note||'').toLowerCase().includes(kw)) }
  return list
})
const typeOptions = computed(() => ['考研学习'].includes(activeSheet.value) ? examSubjects : ['技术学习'].includes(activeSheet.value) ? techCategories : [])

function isOverdue(p) { return p._effectiveStatus === 'overdue' }
function statusCfg(s) { return planStatuses.find(st => st.value === s) || { label: s, color: '#909399', bg: '#F4F4F5' } }

const detailVisible = ref(false); const detailPlan = ref(null)
function openDetail(plan) { detailPlan.value = plan; detailVisible.value = true }

const pomodoroVisible = ref(false); const pomodoroPlan = ref(null); const pomodoroItem = ref(null)
function startFocus(plan, item) { pomodoroPlan.value = plan; pomodoroItem.value = item || null; pomodoroVisible.value = true }
function onPomodoroComplete(duration) {
  pomodoroVisible.value = false
  if (pomodoroPlan.value) { detailPlan.value = store.getPlanById(pomodoroPlan.value.id); detailVisible.value = true
    setTimeout(() => { window.dispatchEvent(new CustomEvent('pomodoro-checkin', { detail: { planId: pomodoroPlan.value?.id, duration } })); skillStore.syncFromPlans() }, 300) }
}

function exportCSV() {
  const data = filteredPlans.value; if (!data.length) return ElMessage.warning('无数据')
  const rows = [['分类','类型','名称','状态','开始','截止','计划h','实际h','进度%','重要','备注']]
  for (const p of data) rows.push([p.category,p.type||'',p.name,p._effectiveStatus,p.startDate,p.endDate,p.plannedHours,(p.actualHours||0).toFixed(1),p.progress,p.importance,p.note||''])
  const csv = '﻿' + rows.map(r => r.map(c => '"'+String(c).replace(/"/g,'""')+'"').join(',')).join('\n')
  const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([csv],{type:'text/csv'})); a.download = `${activeSheet.value}_${today}.csv`; a.click(); ElMessage.success('导出成功')
}
</script>

<template>
  <div class="plans-page">
    <!-- 遮罩 -->
    <div class="plans-bg"></div>

    <div class="plans-container">
      <!-- 头部 -->
      <div class="plans-head">
        <h2>学习计划表</h2>
        <div class="plans-stats">
          <span class="ps-chip all">{{ displayPlans.length }} 项</span>
          <span class="ps-chip active">{{ displayPlans.filter(p=>p._effectiveStatus==='active').length }} 进行中</span>
          <span class="ps-chip done">{{ displayPlans.filter(p=>p._effectiveStatus==='done').length }} 已完成</span>
        </div>
      </div>

      <!-- 分类页签 -->
      <div class="plans-tabs">
        <button v-for="sh in planSheets" :key="sh.key" class="ptab" :class="{ active: activeSheet === sh.key }" @click="activeSheet = sh.key">
          <span class="ptab-icon">{{ sh.icon }}</span><span>{{ sh.label }}</span>
          <span class="ptab-n">{{ sh.key==='all' ? store.plans.length : store.plans.filter(p=>p.category===sh.key).length }}</span>
        </button>
      </div>

      <!-- 工具栏 -->
      <div class="plans-bar">
        <div class="pb-left">
          <el-input v-model="searchKeyword" placeholder="搜索学习内容..." size="small" clearable style="width:200px" />
          <el-select v-model="filterStatus" placeholder="全部状态" size="small" clearable style="width:110px">
            <el-option v-for="s in planStatuses" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <el-select v-if="typeOptions.length" v-model="filterType" placeholder="全部类型" size="small" clearable style="width:110px">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </div>
        <el-button size="small" @click="exportCSV">📥 导出</el-button>
      </div>

      <!-- 卡片列表 -->
      <div class="plans-list" v-if="filteredPlans.length">
        <div v-for="plan in filteredPlans" :key="plan.id" class="plan-card" :class="{ overdue: isOverdue(plan) }" @click="openDetail(plan)">
          <!-- 左侧 -->
          <div class="pc-left">
            <span class="pc-cat" :style="{ background: (techCategories.find(c=>c.value===plan.type)||examSubjects.find(s=>s.value===plan.type)||{}).color+'18', color: (techCategories.find(c=>c.value===plan.type)||examSubjects.find(s=>s.value===plan.type)||{}).color||'#909399' }">{{ plan.type || plan.category }}</span>
            <div class="pc-name">{{ plan.name }}</div>
            <div v-if="plan.chapters" class="pc-chapter">{{ plan.chapters }}</div>
            <div class="pc-status-row">
              <span class="pc-status" :style="{ background: statusCfg(plan._effectiveStatus).bg, color: statusCfg(plan._effectiveStatus).color }">{{ statusCfg(plan._effectiveStatus).label }}</span>
              <span class="pc-date">{{ plan.startDate }} ~ {{ plan.endDate }}</span>
            </div>
          </div>
          <!-- 中间 -->
          <div class="pc-mid">
            <div class="pc-progress-row">
              <span class="pc-progress-label">进度</span>
              <el-progress :percentage="plan.progress" :stroke-width="6" :show-text="false" style="flex:1" />
              <span class="pc-progress-num">{{ plan.progress }}%</span>
            </div>
            <div v-if="plan.questionsTotal" class="pc-exam-stats">
              <span>题目 {{ plan.questionsDone||0 }}/{{ plan.questionsTotal }}</span>
              <span :style="{ color: (plan.accuracy||0)>=80?'#67C23A':(plan.accuracy||0)>=60?'#E6A23C':'#F56C6C' }">正确率 {{ plan.accuracy||0 }}%</span>
              <span>第{{ plan.studyRound||1 }}轮</span>
            </div>
            <span class="pc-hours">实际 {{ (plan.actualHours||0).toFixed(1) }}h / 计划 {{ plan.plannedHours }}h</span>
          </div>
          <!-- 右侧 -->
          <div class="pc-right">
            <div class="pc-importance"><span v-for="i in 5" :key="i" :style="{ color: i <= plan.importance ? '#F0B85A' : '#d9e3da' }">★</span></div>
            <span class="pc-last">{{ plan.lastStudyTime ? plan.lastStudyTime.slice(0,10) : '—' }}</span>
            <div class="pc-actions" @click.stop>
              <button class="pca-focus" @click="startFocus(plan)">🍅 专注</button>
              <button class="pca-detail" @click="openDetail(plan)">详情</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="plans-empty">
        <p v-if="!store.plans.length">📋 还没有学习计划，去后台创建第一个计划吧</p>
        <p v-else>没有匹配的计划</p>
      </div>
    </div>

    <StudyPlanDetail v-model:visible="detailVisible" :plan="detailPlan" @focus="startFocus" />
    <PomodoroTimer v-model:visible="pomodoroVisible" :plan-id="pomodoroPlan?.id" :plan-name="pomodoroPlan?.name" :item-id="pomodoroItem?.id" :item-title="pomodoroItem?.title" @complete="onPomodoroComplete"
      @idle-timeout="(rec) => { if (rec.planId) { detailPlan = store.getPlanById(rec.planId); detailVisible = true } }" />
  </div>
</template>

<style scoped>
.plans-page { position: relative; min-height: 100vh; }
.plans-bg { position: fixed; inset: 0; background: rgba(239,245,236,0.55); pointer-events: none; z-index: 0; }
.plans-container { position: relative; z-index: 1; max-width: 1400px; margin: 0 auto; padding: 24px 28px 64px; }
.plans-head { display: flex; align-items: baseline; gap: 16px; margin-bottom: 14px; flex-wrap: wrap; }
.plans-head h2 { font-size: 22px; font-weight: 700; color: #17231b; }
.plans-stats { display: flex; gap: 6px; }
.ps-chip { font-size: 11px; padding: 2px 10px; border-radius: 10px; font-weight: 500; }
.ps-chip.all { background: #F4F4F5; color: #909399; }
.ps-chip.active { background: #FDF6EC; color: #E6A23C; }
.ps-chip.done { background: #F0F9EB; color: #67C23A; }

.plans-tabs { display: flex; gap: 2px; margin-bottom: 14px; border-bottom: 2px solid #d9e3da; }
.ptab { display: flex; align-items: center; gap: 6px; padding: 8px 14px; border: none; background: transparent; border-radius: 6px 6px 0 0; font-size: 13px; color: #53675a; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.15s; }
.ptab:hover { color: #2f9142; }
.ptab.active { color: #2f9142; font-weight: 600; border-bottom-color: #2f9142; }
.ptab-icon { font-size: 15px; }
.ptab-n { font-size: 11px; background: #e6ebe6; border-radius: 8px; padding: 1px 7px; }

.plans-bar { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; background: rgba(255,255,255,0.9); border: 1px solid #e2e9e3; border-radius: 10px; margin-bottom: 16px; gap: 8px; flex-wrap: wrap; }
.pb-left { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }

.plans-list { display: flex; flex-direction: column; gap: 14px; }
.plan-card { display: flex; gap: 20px; padding: 20px 24px; background: rgba(255,255,255,0.94); border: 1px solid rgba(52,120,72,0.12); border-radius: 14px; box-shadow: 0 1px 8px rgba(0,0,0,0.04); cursor: pointer; transition: all 0.15s; }
.plan-card:hover { border-color: #2f9142; box-shadow: 0 2px 16px rgba(0,0,0,0.08); }
.plan-card.overdue { border-left: 3px solid #E8A87C; padding-left: 21px; }
.pc-left { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.pc-cat { display: inline-block; padding: 2px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; width: fit-content; }
.pc-name { font-size: 16px; font-weight: 700; color: #17231b; margin-top: 2px; }
.pc-chapter { font-size: 12px; color: #65766b; }
.pc-status-row { display: flex; align-items: center; gap: 10px; margin-top: 2px; }
.pc-status { padding: 2px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.pc-date { font-size: 12px; color: #65766b; white-space: nowrap; }
.plan-card.overdue .pc-date { color: #F56C6C; }

.pc-mid { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; justify-content: center; }
.pc-progress-row { display: flex; align-items: center; gap: 10px; }
.pc-progress-label { font-size: 12px; color: #65766b; }
.pc-progress-num { font-size: 14px; font-weight: 700; color: #2f9142; min-width: 36px; text-align: right; }
.pc-exam-stats { display: flex; gap: 14px; font-size: 12px; color: #53675a; }
.pc-hours { font-size: 12px; color: #718077; }

.pc-right { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; flex-shrink: 0; justify-content: center; }
.pc-importance { font-size: 14px; letter-spacing: 1px; }
.pc-last { font-size: 12px; color: #909399; }
.pc-actions { display: flex; gap: 8px; margin-top: 4px; }
.pca-focus { padding: 8px 18px; background: #2f9142; color: #fff; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; white-space: nowrap; }
.pca-focus:hover { background: #247334; }
.pca-detail { padding: 8px 18px; background: transparent; color: #2f9142; border: 1.5px solid #2f9142; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; white-space: nowrap; }
.pca-detail:hover { background: #eaf5ec; }

.plans-empty { text-align: center; padding: 80px 24px; color: #909399; font-size: 15px; }
.plans-empty p { margin: 0; }

@media (max-width: 1200px) {
  .plan-card { flex-wrap: wrap; }
  .pc-right { flex-direction: row; align-items: center; gap: 12px; width: 100%; justify-content: flex-start; }
}
@media (max-width: 768px) {
  .plans-container { padding: 12px; }
  .plan-card { flex-direction: column; gap: 12px; padding: 16px; }
  .pc-right { flex-direction: column; align-items: flex-start; }
  .pca-focus, .pca-detail { width: 100%; text-align: center; }
}
</style>
