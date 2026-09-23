<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStudyPlanStore } from '@/stores/studyPlan'
import { useUserSkillStore } from '@/stores/userSkill'
import { planStatuses, itemStatuses, techCategories, examSubjects } from '@/data/studyPlans'

const props = defineProps({ visible: Boolean, plan: Object })
const emit = defineEmits(['update:visible', 'focus'])
const store = useStudyPlanStore()
const skillStore = useUserSkillStore()

const checkins = computed(() => props.plan ? store.getCheckinsByPlan(props.plan.id) : [])
const groups = computed(() => props.plan?.groups || [])
const totalItems = computed(() => { let n = 0; for (const g of groups.value) n += (g.items || []).length; return n })
const doneItems = computed(() => { let n = 0; for (const g of groups.value) n += (g.items || []).filter(it => it.status === 'done').length; return n })

// 打卡表单
const showCheckinForm = ref(false)
const checkinContent = ref('')
const checkinNote = ref('')
const checkinDuration = ref(25)
const selectedItemId = ref(null)
const selectedGroupId = ref(null)

function openCheckin(groupId, itemId) {
  selectedGroupId.value = groupId; selectedItemId.value = itemId
  checkinContent.value = ''; checkinNote.value = ''; checkinDuration.value = 25
  showCheckinForm.value = true
}

function submitCheckin() {
  if (!props.plan) return
  store.addCheckin({ planId: props.plan.id, groupId: selectedGroupId.value, itemId: selectedItemId.value, content: checkinContent.value || '学习打卡', note: checkinNote.value, duration: checkinDuration.value, markDone: true })
  showCheckinForm.value = false
  skillStore.syncFromPlans()
}

function close() { emit('update:visible', false) }

// 监听番茄钟完成
function onPomodoroCheckin(e) {
  if (e.detail?.planId === props.plan?.id) {
    selectedItemId.value = e.detail.itemId || null
    selectedGroupId.value = e.detail.groupId || null
    checkinDuration.value = Math.round((e.detail.duration || 1500) / 60)
    showCheckinForm.value = true
  }
}
onMounted(() => window.addEventListener('pomodoro-checkin', onPomodoroCheckin))
onUnmounted(() => window.removeEventListener('pomodoro-checkin', onPomodoroCheckin))
</script>

<template>
  <Teleport to="body">
    <Transition name="drawer">
      <div v-if="visible" class="drawer-overlay" @click.self="close">
        <div class="drawer-panel">
          <button class="drawer-close" @click="close">✕</button>
          <template v-if="plan">
            <div class="plan-info">
              <span class="tag">{{ plan.category }}</span>
              <h3>{{ plan.name }}</h3>
              <span class="tag" :style="{ background: (planStatuses.find(s=>s.value===plan.status)||{}).bg, color: (planStatuses.find(s=>s.value===plan.status)||{}).color }">{{ (planStatuses.find(s=>s.value===plan.status)||{}).label }}</span>
            </div>
            <div class="stats-row">
              <div><span class="sl">日期</span><span class="sv">{{ plan.startDate }} ~ {{ plan.endDate }}</span></div>
              <div><span class="sl">计划/实际</span><span class="sv">{{ plan.plannedHours }}h / {{ (plan.actualHours||0).toFixed(1) }}h</span></div>
              <div><span class="sl">子任务</span><span class="sv" style="color:var(--color-primary);font-weight:600">{{ doneItems }}/{{ totalItems }}</span></div>
            </div>
            <div class="prog-section"><div class="prog-hd"><span>总体进度</span><strong>{{ plan.progress }}%</strong></div><div class="prog-bar"><div class="prog-fill" :style="{ width: plan.progress + '%' }"></div></div></div>

            <!-- 按系列分组 -->
            <div v-for="g in groups" :key="g.id" class="group-block">
              <div class="group-hd">
                <span class="group-name">{{ g.name }}</span>
                <span class="group-prog">{{ store.calcGroupProgress(g) }}%</span>
              </div>
              <div v-if="!g.items?.length" style="font-size:12px;color:var(--color-muted);padding:4px 0">暂无子任务</div>
              <div v-for="it in g.items" :key="it.id" class="item-row" :class="{ done: it.status === 'done' }">
                <label class="item-check"><input type="checkbox" :checked="it.status === 'done'" @change="store.updateItem(plan.id, it.id, { status: it.status === 'done' ? 'pending' : 'done' })" /></label>
                <span class="item-title">{{ it.title }}</span>
                <span class="item-pomo">🍅 {{ it.actualPomodoros || 0 }}/{{ it.estPomodoros || 0 }}</span>
                <button class="mini-btn" @click="emit('focus', plan, it)">专注</button>
                <button class="mini-btn" @click="openCheckin(g.id, it.id)">打卡</button>
              </div>
            </div>

            <!-- 打卡历史 -->
            <div class="group-block">
              <h4>最近打卡</h4>
              <div v-if="!checkins.length" style="font-size:12px;color:var(--color-muted)">暂无打卡</div>
              <div v-for="chk in checkins.slice(0,8)" :key="chk.id" class="chk-row"><span class="chk-date">{{ chk.date?.slice(0,10) }}</span><span class="chk-content">{{ chk.content }}</span><span class="chk-dur">{{ chk.duration }}min</span></div>
            </div>

            <div class="action-row"><button class="btn-main" @click="emit('focus', plan, null)">🍅 开始专注</button><button class="btn-second" @click="openCheckin(null, null)">📝 记录学习</button></div>

            <!-- 打卡表单 -->
            <div v-if="showCheckinForm" class="checkin-form">
              <h4>学习打卡</h4>
              <textarea v-model="checkinContent" placeholder="学习内容..." rows="2"></textarea>
              <input v-model="checkinNote" placeholder="学习心得（选填）" />
              <label style="font-size:13px;display:flex;align-items:center;gap:8px;margin-bottom:8px">时长(分钟) <input v-model.number="checkinDuration" type="number" min="1" max="480" style="width:70px" /></label>
              <div style="display:flex;gap:8px"><button class="btn-main" @click="submitCheckin">保存打卡</button><button class="btn-second" @click="showCheckinForm = false">取消</button></div>
            </div>
          </template>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.drawer-overlay { position: fixed; inset: 0; z-index: 2000; background: rgba(0,0,0,0.25); display: flex; justify-content: flex-end; }
.drawer-panel { width: 480px; max-width: 100vw; height: 100vh; overflow-y: auto; background: rgba(255,255,255,0.97); backdrop-filter: blur(12px); padding: 28px 24px; box-shadow: var(--shadow-xl); }
.drawer-close { position: absolute; top: 16px; right: 20px; width: 36px; height: 36px; border: none; background: rgba(0,0,0,0.05); border-radius: 50%; font-size: 16px; cursor: pointer; }
.plan-info { margin-bottom: 12px; }
.plan-info h3 { font-size: 20px; font-weight: 700; margin: 6px 0 4px; }
.tag { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 500; background: var(--color-primary-light); color: var(--color-primary); margin-right: 6px; }
.stats-row { display: flex; gap: 16px; margin-bottom: 12px; flex-wrap: wrap; }
.sl { font-size: 11px; color: var(--color-muted); display: block; }
.sv { font-size: 13px; color: var(--color-body); font-weight: 500; }
.prog-section { margin-bottom: 16px; }
.prog-hd { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px; }
.prog-hd strong { color: var(--color-primary); }
.prog-bar { height: 8px; background: var(--color-border-light); border-radius: 4px; overflow: hidden; }
.prog-fill { height: 100%; background: var(--color-primary); border-radius: 4px; transition: width 0.3s; }

.group-block { margin-bottom: 18px; }
.group-block h4 { font-size: 14px; font-weight: 600; margin-bottom: 6px; }
.group-hd { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; border-bottom: 1px solid var(--color-border-light); margin-bottom: 6px; }
.group-name { font-weight: 600; font-size: 14px; color: var(--color-title); }
.group-prog { font-size: 12px; color: var(--color-primary); font-weight: 600; }

.item-row { display: flex; align-items: center; gap: 6px; padding: 6px 0; border-bottom: 1px solid #F5F7FA; font-size: 12px; }
.item-row.done .item-title { text-decoration: line-through; color: var(--color-muted); }
.item-check input { accent-color: var(--color-primary); }
.item-title { flex: 1; color: var(--color-body); min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-pomo { font-size: 11px; color: var(--color-muted); white-space: nowrap; }

.chk-row { display: flex; gap: 8px; padding: 5px 0; border-bottom: 1px solid #F5F7FA; font-size: 12px; align-items: center; }
.chk-date { color: var(--color-muted); white-space: nowrap; }
.chk-content { flex: 1; color: var(--color-body); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chk-dur { color: var(--color-primary); font-weight: 600; white-space: nowrap; }

.mini-btn { padding: 2px 6px; border: 1px solid var(--color-border); border-radius: 3px; font-size: 10px; background: transparent; cursor: pointer; white-space: nowrap; }
.mini-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.action-row { display: flex; gap: 8px; margin: 16px 0; }
.btn-main { flex: 1; padding: 10px 16px; background: var(--color-primary); color: #fff; border: none; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-second { flex: 1; padding: 10px 16px; background: transparent; color: var(--color-primary); border: 1.5px solid var(--color-primary); border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; }
.checkin-form { background: var(--color-bg); padding: 12px; border-radius: 8px; }
.checkin-form h4 { font-size: 14px; margin-bottom: 8px; }
.checkin-form textarea, .checkin-form input { width: 100%; padding: 6px 8px; border: 1px solid var(--color-border); border-radius: 4px; font-size: 13px; margin-bottom: 6px; resize: vertical; }

.drawer-enter-active, .drawer-leave-active { transition: all 0.3s; }
.drawer-enter-from, .drawer-leave-to { opacity: 0; }
.drawer-enter-from .drawer-panel, .drawer-leave-to .drawer-panel { transform: translateX(100%); }
.drawer-enter-active .drawer-panel, .drawer-leave-active .drawer-panel { transition: transform 0.3s; }
@media (max-width: 768px) { .drawer-panel { width: 100vw; padding: 16px; } }
</style>
