<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'

const props = defineProps({
  visible: Boolean, planId: String, planName: String, itemId: String, itemTitle: String
})
const emit = defineEmits(['update:visible', 'complete', 'idle-timeout'])

// ── 预设与设置 ──
const PRESETS = [15, 25, 45, 60]
const IDLE_OPTIONS = [
  { label: '10 分钟', value: 10 }, { label: '15 分钟', value: 15 },
  { label: '20 分钟', value: 20 }, { label: '30 分钟', value: 30 },
  { label: '关闭', value: 0 }
]
const SCENE_MODES = [
  { label: '普通学习', idleMin: 15 }, { label: '阅读模式', idleMin: 30 },
  { label: '观看课程', idleMin: 30 }, { label: '自定义', idleMin: 15 }
]

// ── 加载持久化设置 ──
const savedIdle = Number(localStorage.getItem('pomodoro_idle_min') || 15)
const savedScene = Number(localStorage.getItem('pomodoro_scene') || 0)
const idleMinutes = ref(savedIdle)
const focusScene = ref(savedScene)
const autoEndSeconds = 60 // 离席确认弹窗倒计时

function saveSettings() { localStorage.setItem('pomodoro_idle_min', String(idleMinutes.value)); localStorage.setItem('pomodoro_scene', String(focusScene.value)) }
function applyScene(idx) { focusScene.value = idx; idleMinutes.value = SCENE_MODES[idx].idleMin; saveSettings() }

// ── 计时状态 ──
const totalSeconds = ref(25 * 60)
const remaining = ref(25 * 60)
const isRunning = ref(false)
const isPaused = ref(false)
let timerInterval = null

// ── 会话追踪 ──
const sessionStartedAt = ref(null)    // 本次计时开始时间戳
const lastActivityAt = ref(null)      // 最后用户活动时间戳
const pausedDuration = ref(0)         // 累计暂停时长(ms)
const pausedAt = ref(null)            // 当前暂停开始时间戳
const idlePromptStartedAt = ref(null) // 离席弹窗触发时间戳
const autoEndTimer = ref(null)        // 自动结束倒计时 ID
const endReason = ref(null)           // 结束原因

// ── 离席弹窗 ──
const showIdlePrompt = ref(false)
const idleCountdown = ref(autoEndSeconds)
const idleCooldownUntil = ref(null)   // 冷却期，期间不再弹出

const currentIdleMinutes = computed(() => focusScene.value >= 0 && focusScene.value < SCENE_MODES.length ? SCENE_MODES[focusScene.value].idleMin : idleMinutes.value)

// ── 显示 ──
const display = () => { const m = Math.floor(remaining.value / 60); const s = remaining.value % 60; return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}` }
const progressPct = () => totalSeconds.value > 0 ? ((totalSeconds.value - remaining.value) / totalSeconds.value * 100) : 0

// ── 用户活动检测 ──
function onActivity() { if (isRunning.value && !showIdlePrompt.value) lastActivityAt.value = Date.now() }
function setupActivityListeners() {
  const events = ['keydown', 'pointerdown', 'touchstart', 'scroll']
  for (const e of events) document.addEventListener(e, onActivity, { passive: true })
  let mousemoveTimer = null
  document.addEventListener('mousemove', () => { if (mousemoveTimer) return; mousemoveTimer = setTimeout(() => { mousemoveTimer = null; onActivity() }, 2000) }, { passive: true })
  document.addEventListener('visibilitychange', () => { if (document.visibilityState === 'visible') { onActivity(); onVisibilityReturn() } })
  window.addEventListener('focus', onActivity)
}
function teardownActivityListeners() {
  ['keydown', 'pointerdown', 'touchstart', 'scroll', 'mousemove'].forEach(e => document.removeEventListener(e, onActivity))
  document.removeEventListener('visibilitychange', onActivity)
  window.removeEventListener('focus', onActivity)
}

// ── 页面可见性 ──
function onVisibilityReturn() {
  if (!isRunning.value || !sessionStartedAt.value) return
  const now = Date.now()
  const elapsed = now - sessionStartedAt.value - pausedDuration.value
  const plannedMs = totalSeconds.value * 1000
  remaining.value = Math.max(0, Math.round((plannedMs - elapsed) / 1000))
  // 检查是否需要弹出离席确认
  checkIdle()
}

// ── 离席检测 ──
let idleCheckInterval = null
function checkIdle() {
  const idleMs = currentIdleMinutes.value * 60 * 1000
  if (idleMs <= 0) return // 关闭检测
  if (!isRunning.value || showIdlePrompt.value) return
  if (idleCooldownUntil.value && Date.now() < idleCooldownUntil.value) return
  const lastAct = lastActivityAt.value || sessionStartedAt.value
  if (Date.now() - lastAct >= idleMs) triggerIdlePrompt()
}

function triggerIdlePrompt() {
  showIdlePrompt.value = true
  idlePromptStartedAt.value = Date.now()
  idleCountdown.value = autoEndSeconds
  // 提示音
  try { const ctx = new (window.AudioContext || window.webkitAudioContext)(); const osc = ctx.createOscillator(); const gain = ctx.createGain(); osc.connect(gain); gain.connect(ctx.destination); gain.gain.value = 0.1; osc.frequency.value = 880; osc.start(); osc.stop(ctx.currentTime + 0.15) } catch(e){}
  // 标题
  document.title = '你还在吗？即将结束专注'
  // 倒计时
  autoEndTimer.value = setInterval(() => {
    idleCountdown.value--
    if (idleCountdown.value <= 0) autoEndSession()
  }, 1000)
}

function autoEndSession() {
  clearInterval(autoEndTimer.value)
  showIdlePrompt.value = false
  const validEnd = idlePromptStartedAt.value || Date.now()
  const elapsed = validEnd - sessionStartedAt.value - pausedDuration.value
  const actualSec = Math.floor(elapsed / 1000)
  endReason.value = 'idle_timeout'
  isRunning.value = false; isPaused.value = false; clearInterval(timerInterval)
  const pending = {
    planId: props.planId, itemId: props.itemId,
    duration: actualSec,
    sessionStartedAt: sessionStartedAt.value, lastActivityAt: lastActivityAt.value,
    idlePromptStartedAt: idlePromptStartedAt.value, autoEndedAt: Date.now(),
    endReason: 'idle_timeout', status: 'pending_confirmation',
    confirmedDuration: null, confirmedAt: null
  }
  try { const arr = JSON.parse(localStorage.getItem('pomodoro_pending') || '[]'); arr.push(pending); localStorage.setItem('pomodoro_pending', JSON.stringify(arr)) } catch(e){}
  emit('idle-timeout', pending)
  resetState()
  emit('update:visible', false)
  document.title = 'Orange. - 个人成长平台'
}

function respondStillHere() {
  clearInterval(autoEndTimer.value)
  showIdlePrompt.value = false
  lastActivityAt.value = Date.now()
  autoEndTimer.value = null
  idleCooldownUntil.value = Date.now() + (currentIdleMinutes.value * 60 * 1000)
  endReason.value = null
  document.title = 'Orange. - 个人成长平台'
}

function respondEndAndRecord() {
  clearInterval(autoEndTimer.value)
  showIdlePrompt.value = false
  endReason.value = 'manual_end_during_idle_prompt'
  const elapsed = Date.now() - sessionStartedAt.value - pausedDuration.value
  const actualSec = Math.max(0, Math.floor(elapsed / 1000))
  isRunning.value = false; isPaused.value = false; clearInterval(timerInterval)
  document.title = 'Orange. - 个人成长平台'
  if (actualSec >= 60) emit('complete', actualSec)
  emit('update:visible', false)
}

function respondAbandon() {
  if (!confirm('确定放弃本次专注？已产生的学习时长将不计入统计。')) return
  clearInterval(autoEndTimer.value)
  showIdlePrompt.value = false
  endReason.value = 'abandoned_during_idle_prompt'
  isRunning.value = false; isPaused.value = false; clearInterval(timerInterval)
  document.title = 'Orange. - 个人成长平台'
  emit('update:visible', false)
}

// ── 计时控制 ──
function setPreset(mins) { if (isRunning.value) return; totalSeconds.value = mins * 60; remaining.value = mins * 60 }
function start() {
  if (isRunning.value) return
  if (!isPaused.value) { sessionStartedAt.value = Date.now(); lastActivityAt.value = Date.now(); pausedDuration.value = 0; endReason.value = null }
  else if (pausedAt.value) { pausedDuration.value += Date.now() - pausedAt.value; pausedAt.value = null }
  isRunning.value = true; isPaused.value = false
  timerInterval = setInterval(() => { if (remaining.value <= 0) complete(); else remaining.value-- }, 1000)
  if (idleCheckInterval) clearInterval(idleCheckInterval)
  idleCheckInterval = setInterval(checkIdle, 5000)
}
function pause() {
  isRunning.value = false; isPaused.value = true; pausedAt.value = Date.now()
  clearInterval(timerInterval)
}
function resetSession() {
  isRunning.value = false; isPaused.value = false
  remaining.value = totalSeconds.value; clearInterval(timerInterval)
}
function complete() {
  clearInterval(timerInterval); clearInterval(idleCheckInterval)
  isRunning.value = false; isPaused.value = false
  const actualSeconds = totalSeconds.value - remaining.value
  endReason.value = 'completed'
  if (actualSeconds >= 60) emit('complete', actualSeconds)
  remaining.value = totalSeconds.value; emit('update:visible', false)
  document.title = 'Orange. - 个人成长平台'
}
function cancel() { clearInterval(timerInterval); clearInterval(idleCheckInterval); resetState(); emit('update:visible', false); document.title = 'Orange. - 个人成长平台' }
function resetState() {
  isRunning.value = false; isPaused.value = false; showIdlePrompt.value = false
  clearInterval(autoEndTimer.value); clearInterval(idleCheckInterval)
  remaining.value = totalSeconds.value; endReason.value = null
}

// ── 生命周期 ──
watch(() => props.visible, (v) => {
  if (v) { remaining.value = totalSeconds.value; isRunning.value = false; isPaused.value = false; setupActivityListeners(); if (!idleCheckInterval) idleCheckInterval = setInterval(checkIdle, 5000) }
  else { clearInterval(timerInterval); clearInterval(idleCheckInterval); teardownActivityListeners() }
})
onUnmounted(() => { clearInterval(timerInterval); clearInterval(idleCheckInterval); clearInterval(autoEndTimer.value); teardownActivityListeners() })

// ── 待确认记录 ──
const pendingRecords = ref([])
function loadPending() { try { pendingRecords.value = JSON.parse(localStorage.getItem('pomodoro_pending') || '[]') } catch(e) { pendingRecords.value = [] } }
function confirmPending(rec, adjustedMins) {
  rec.status = 'confirmed'; rec.confirmedDuration = (adjustedMins || Math.round(rec.duration / 60)) * 60; rec.confirmedAt = new Date().toISOString()
  if (rec.confirmedDuration >= 60) emit('complete', rec.confirmedDuration)
  savePending()
}
function discardPending(rec) { pendingRecords.value = pendingRecords.value.filter(r => r !== rec); savePending() }
function savePending() { localStorage.setItem('pomodoro_pending', JSON.stringify(pendingRecords.value)) }
onMounted(loadPending)
</script>

<template>
  <Teleport to="body">
    <!-- 待确认记录弹窗 -->
    <Transition name="fade">
      <div v-if="pendingRecords.length > 0 && !visible" class="timer-overlay" style="z-index:3001">
        <div class="timer-card" style="width:420px">
          <h3 style="margin-bottom:12px">待确认的专注记录</h3>
          <p style="font-size:13px;color:var(--color-secondary);margin-bottom:12px">上次专注因长时间未响应而自动结束，请确认有效学习时间。</p>
          <div v-for="(rec, i) in pendingRecords" :key="i" style="padding:10px;background:#FDF6EC;border-radius:6px;margin-bottom:8px;font-size:13px">
            <div style="display:flex;justify-content:space-between;margin-bottom:6px">
              <span>有效时长 <strong>{{ Math.round((rec.duration||0)/60) }}min</strong></span>
              <span style="color:var(--color-muted)">{{ new Date(rec.autoEndedAt).toLocaleTimeString() }}</span>
            </div>
            <div style="display:flex;gap:6px;align-items:center">
              <input v-model.number="rec.confirmedMins" type="number" :min="1" :placeholder="String(Math.round((rec.duration||0)/60))" style="width:60px;padding:4px 6px;border:1px solid var(--color-border);border-radius:4px;font-size:12px" /> min
              <el-button size="small" type="primary" @click="confirmPending(rec, rec.confirmedMins)">确认</el-button>
              <el-button size="small" @click="discardPending(rec)">放弃</el-button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 番茄钟主弹窗 -->
    <Transition name="fade">
      <div v-if="visible" class="timer-overlay" @click.self="cancel">
        <div class="timer-card">
          <button class="tclose" @click="cancel" v-if="!isRunning && !isPaused">✕</button>

          <div class="timer-header">
            <span class="timer-plan-icon">🍅</span>
            <div>
              <h3 class="timer-plan-name">{{ planName || '专注学习' }}</h3>
              <p v-if="itemTitle" class="timer-item-name">{{ itemTitle }}</p>
            </div>
          </div>

          <!-- 设置 -->
          <div v-if="!isRunning && !isPaused" style="margin-bottom:12px;font-size:12px;color:var(--color-secondary)">
            <div style="display:flex;gap:6px;align-items:center;justify-content:center;flex-wrap:wrap">
              <span>专注场景:</span>
              <button v-for="(sm, i) in SCENE_MODES" :key="i" class="sbtn" :class="{ sel: focusScene === i }" @click="applyScene(i)">{{ sm.label }}</button>
            </div>
            <div v-if="focusScene === 3" style="margin-top:6px;display:flex;gap:6px;align-items:center;justify-content:center">
              <span>离席检测:</span>
              <select v-model="idleMinutes" @change="saveSettings()" style="padding:3px 6px;border:1px solid var(--color-border);border-radius:4px;font-size:12px">
                <option v-for="o in IDLE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
              </select>
            </div>
          </div>

          <!-- 圆圈计时器 -->
          <div class="timer-ring-wrap">
            <svg class="timer-ring" viewBox="0 0 200 200">
              <circle cx="100" cy="100" r="90" fill="none" stroke="var(--color-border-light)" stroke-width="8" />
              <circle cx="100" cy="100" r="90" fill="none" stroke="var(--color-primary)" stroke-width="8" stroke-linecap="round" :stroke-dasharray="2 * Math.PI * 90" :stroke-dashoffset="2 * Math.PI * 90 * (1 - progressPct() / 100)" transform="rotate(-90 100 100)" style="transition: stroke-dashoffset 1s linear" />
            </svg>
            <div class="timer-display">{{ display() }}</div>
          </div>

          <div class="presets" v-if="!isRunning">
            <button v-for="m in PRESETS" :key="m" class="pbtn" :class="{ sel: totalSeconds === m * 60 }" @click="setPreset(m)">{{ m }}min</button>
          </div>

          <div class="timer-controls">
            <button v-if="!isRunning && !isPaused" class="tbtn tbtn-start" @click="start">开始专注</button>
            <button v-if="isRunning" class="tbtn tbtn-pause" @click="pause">暂停</button>
            <button v-if="isPaused" class="tbtn tbtn-start" @click="start">继续</button>
            <button v-if="isRunning || isPaused" class="tbtn tbtn-reset" @click="resetSession">重置</button>
          </div>
        </div>

        <!-- 离席确认弹窗 -->
        <Transition name="fade">
          <div v-if="showIdlePrompt" class="idle-overlay">
            <div class="idle-card">
              <h3>🍅 你还在学习吗？</h3>
              <p style="color:var(--color-secondary);margin:8px 0">已有 {{ currentIdleMinutes }} 分钟未检测到操作，将在 <strong style="color:#F56C6C">{{ idleCountdown }}</strong> 秒后结束本次专注。</p>
              <div style="display:flex;flex-direction:column;gap:8px;margin-top:16px">
                <button class="tbtn tbtn-start" @click="respondStillHere">✅ 我还在，继续专注</button>
                <button class="tbtn tbtn-pause" @click="respondEndAndRecord">⏹ 结束并记录</button>
                <button style="padding:8px;border:1px solid var(--color-border);border-radius:8px;background:transparent;color:var(--color-muted);cursor:pointer;font-size:13px" @click="respondAbandon">🗑 放弃本次专注</button>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.timer-overlay { position: fixed; inset: 0; z-index: 3000; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; }
.timer-card { background: rgba(255,255,255,0.97); backdrop-filter: blur(12px); border-radius: 16px; padding: 32px; width: 380px; max-width: 90vw; text-align: center; position: relative; box-shadow: var(--shadow-xl); }
.tclose { position: absolute; top: 12px; right: 16px; border: none; background: transparent; font-size: 18px; color: var(--color-muted); cursor: pointer; }
.timer-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; text-align: left; }
.timer-plan-icon { font-size: 28px; }
.timer-plan-name { font-size: 16px; font-weight: 700; color: var(--color-title); }
.timer-item-name { font-size: 13px; color: var(--color-secondary); }
.timer-ring-wrap { position: relative; width: 180px; height: 180px; margin: 0 auto 12px; }
.timer-ring { width: 100%; height: 100%; }
.timer-display { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 40px; font-weight: 800; color: var(--color-title); font-variant-numeric: tabular-nums; }
.presets { display: flex; gap: 6px; justify-content: center; margin-bottom: 12px; }
.pbtn, .sbtn { padding: 5px 12px; border: 1px solid var(--color-border); border-radius: 20px; font-size: 12px; background: transparent; cursor: pointer; color: var(--color-secondary); }
.pbtn:hover, .sbtn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.pbtn.sel, .sbtn.sel { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.timer-controls { display: flex; gap: 8px; justify-content: center; }
.tbtn { padding: 10px 24px; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; }
.tbtn-start { background: var(--color-primary); color: #fff; }
.tbtn-start:hover { background: #3D8B47; }
.tbtn-pause { background: var(--color-accent); color: #fff; }
.tbtn-reset { background: transparent; color: var(--color-secondary); border: 1px solid var(--color-border); }

/* 离席确认 */
.idle-overlay { position: fixed; inset: 0; z-index: 3100; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; }
.idle-card { background: #fff; border-radius: 14px; padding: 28px; width: 380px; max-width: 90vw; text-align: center; box-shadow: var(--shadow-xl); }
.idle-card h3 { font-size: 18px; margin-bottom: 4px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.25s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
