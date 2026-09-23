<script setup>
import { ref, computed } from 'vue'
import { useUserSkillStore, SKILL_DOMAINS, SKILL_LEVELS } from '@/stores/userSkill'

const store = useUserSkillStore()

// 筛选
const filterDomain = ref('all')

const filteredSkills = computed(() => {
  let list = store.skills.filter(s => s.status !== '已归档')
  if (filterDomain.value !== 'all') list = list.filter(s => s.domain === filterDomain.value)
  return list.sort((a, b) => b.totalMinutes - a.totalMinutes)
})

function fmtMins(m) { return m >= 60 ? (m/60).toFixed(1)+'h' : m+'min' }
function levelIdx(l) { return SKILL_LEVELS.indexOf(l) }
function levelPct(l) { return Math.round(((levelIdx(l)+1) / SKILL_LEVELS.length) * 100) }
</script>

<template>
  <div class="skills-page">
    <div class="skills-panel">
      <!-- 标题 -->
      <div class="sk-header">
        <h2>技术能力</h2>
        <p>每一次学习完成，都会沉淀为可追溯的能力</p>
      </div>

      <!-- 统计卡片 -->
      <div class="sk-stats">
        <div class="sk-stat"><span class="sks-n">{{ store.totalSkills }}</span><span class="sks-l">技能总数</span></div>
        <div class="sk-stat"><span class="sks-n">{{ store.learningCount }}</span><span class="sks-l">学习中</span></div>
        <div class="sk-stat"><span class="sks-n">{{ store.unlockedCount }}</span><span class="sks-l">已解锁</span></div>
        <div class="sk-stat"><span class="sks-n">{{ store.proficientCount }}</span><span class="sks-l">已掌握</span></div>
        <div class="sk-stat"><span class="sks-n">{{ store.totalSkillMins >= 60 ? (store.totalSkillMins/60).toFixed(1)+'h' : store.totalSkillMins+'min' }}</span><span class="sks-l">累计学习</span></div>
      </div>

      <!-- 筛选 -->
      <div class="sk-filters">
        <button v-for="d in [{value:'all',label:'全部'}, ...SKILL_DOMAINS.map(d=>({value:d,label:d}))]" :key="d.value"
          class="sfbtn" :class="{ active: filterDomain === d.value }" @click="filterDomain = d.value">{{ d.label }}</button>
      </div>

      <!-- 技能卡片 -->
      <div v-if="filteredSkills.length" class="sk-grid">
        <div v-for="sk in filteredSkills" :key="sk.id" class="sk-card">
          <div class="skc-top">
            <span class="skc-icon">{{ sk.icon }}</span>
            <div class="skc-info">
              <span class="skc-name">{{ sk.name }}</span>
              <span class="skc-domain">{{ sk.domain }}</span>
            </div>
            <span class="skc-level" :class="'lv-' + sk.level">{{ sk.level }}</span>
          </div>
          <div class="skc-progress">
            <div class="skc-bar"><div class="skc-fill" :style="{ width: levelPct(sk.level) + '%' }"></div></div>
          </div>
          <div class="skc-meta">
            <span>{{ sk.evidence?.length || 0 }} 个模块</span>
            <span>{{ fmtMins(sk.totalMinutes) }}</span>
            <span v-if="sk.linkedPlans?.length">{{ sk.linkedPlans.length }} 个计划</span>
          </div>
          <div class="skc-detail" v-if="sk.evidence?.length">
            <div v-for="ev in sk.evidence.slice(0,3)" :key="ev.sourceId" class="skc-ev">
              <span class="skc-ev-plan">{{ ev.planName }}</span>
              <span class="skc-ev-group">→ {{ ev.groupName }}</span>
              <span class="skc-ev-mins">{{ fmtMins(ev.minutes) }}</span>
            </div>
          </div>
          <div class="skc-status">
            <span :class="sk.status==='学习中'?'learning':sk.status==='已解锁'?'unlocked':'archived'">{{ sk.status }}</span>
            <span class="skc-updated">{{ sk.updatedAt?.slice(0,10) }}</span>
          </div>
        </div>
      </div>

      <div v-else class="sk-empty">
        <p>还没有技能记录</p>
        <p class="sk-empty-hint">完成学习计划中的任务系列后，对应技能会自动出现在这里</p>
        <router-link to="/plans" class="sk-go-btn">去学习计划</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skills-page { position: relative; z-index: 1; }
.skills-panel {
  max-width: 1220px; margin: 24px auto 48px;
  padding: 32px 38px 48px;
  background: rgba(255,255,255,0.94); backdrop-filter: blur(10px);
  border: 1px solid #c8d6ca; border-radius: 18px;
  box-shadow: 0 2px 20px rgba(0,0,0,0.06);
}
.sk-header { margin-bottom: 20px; }
.sk-header h2 { font-size: 24px; font-weight: 700; color: #17231b; margin-bottom: 4px; }
.sk-header p { font-size: 14px; color: #65766b; }

.sk-stats { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
.sk-stat { flex: 1; min-width: 90px; text-align: center; padding: 14px 10px; border-radius: 10px; background: rgba(255,255,255,0.85); border: 1px solid #d9e3da; }
.sks-n { font-size: 22px; font-weight: 700; color: #2f9142; display: block; }
.sks-l { font-size: 11px; color: #718077; display: block; margin-top: 2px; }

.sk-filters { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 20px; }
.sfbtn { padding: 5px 14px; border: 1px solid #d9e3da; border-radius: 18px; font-size: 12px; background: #fff; color: #53675a; cursor: pointer; transition: all 0.15s; }
.sfbtn:hover { border-color: #2f9142; color: #2f9142; }
.sfbtn.active { background: #2f9142; color: #fff; border-color: #2f9142; }

.sk-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 14px; }
.sk-card { background: rgba(255,255,255,0.92); border: 1px solid #d9e3da; border-radius: 12px; padding: 18px; transition: all 0.2s; }
.sk-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.skc-top { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.skc-icon { font-size: 24px; }
.skc-info { flex: 1; min-width: 0; }
.skc-name { font-size: 15px; font-weight: 700; color: #17231b; display: block; }
.skc-domain { font-size: 11px; color: #718077; margin-top: 1px; }
.skc-level { padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; }
.lv-了解 { background: #F0F0F0; color: #909399; }
.lv-入门 { background: #E8F4FD; color: #409EFF; }
.lv-熟悉 { background: #FDF6EC; color: #E6A23C; }
.lv-掌握 { background: #EAF5E8; color: #2f9142; }
.lv-熟练 { background: #D4EDDA; color: #155724; }

.skc-progress { margin-bottom: 10px; }
.skc-bar { height: 4px; background: #e6ebe6; border-radius: 2px; overflow: hidden; }
.skc-fill { height: 100%; background: #2f9142; border-radius: 2px; transition: width 0.3s; }

.skc-meta { display: flex; gap: 12px; font-size: 12px; color: #65766b; margin-bottom: 8px; }

.skc-detail { margin-bottom: 8px; }
.skc-ev { display: flex; gap: 6px; align-items: center; font-size: 11px; color: #718077; padding: 3px 0; }
.skc-ev-plan { font-weight: 500; color: #53675a; }
.skc-ev-group { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.skc-ev-mins { color: #2f9142; font-weight: 600; white-space: nowrap; }

.skc-status { display: flex; justify-content: space-between; align-items: center; font-size: 11px; }
.skc-status .learning { color: #409EFF; font-weight: 500; }
.skc-status .unlocked { color: #2f9142; font-weight: 500; }
.skc-updated { color: #b0b8b2; }

.sk-empty { text-align: center; padding: 64px; color: #718077; }
.sk-empty-hint { font-size: 13px; color: #b0b8b2; margin: 8px 0 16px; }
.sk-go-btn { display: inline-block; padding: 10px 22px; background: #2f9142; color: #fff; border-radius: 8px; text-decoration: none; font-weight: 600; }

@media (max-width: 768px) {
  .skills-panel { padding: 18px 14px 28px; margin: 12px 8px 24px; }
  .sk-grid { grid-template-columns: 1fr; }
}
</style>
