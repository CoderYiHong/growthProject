<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 位置状态
const x = ref(window.innerWidth - 160)
const y = ref(window.innerHeight - 280)
const scale = ref(1)
const rotation = ref(0)

// 拖拽状态
const dragging = ref(false)
let dragStartX = 0
let dragStartY = 0
let petStartX = 0
let petStartY = 0

// 空闲漂浮
let floatTimer = null
let idleTimer = null

function startIdleFloat() {
  floatTimer = setInterval(() => {
    if (dragging.value) return
    // 随机微移
    const dx = (Math.random() - 0.5) * 60
    const dy = (Math.random() - 0.5) * 40
    const newX = Math.max(20, Math.min(window.innerWidth - 180, x.value + dx))
    const newY = Math.max(60, Math.min(window.innerHeight - 300, y.value + dy))
    x.value = newX
    y.value = newY
    rotation.value = (Math.random() - 0.5) * 8
    scale.value = 1 + (Math.random() - 0.5) * 0.06
  }, 4000)
}

// 拖拽处理
function onDragStart(e) {
  dragging.value = true
  dragStartX = e.clientX || e.touches?.[0]?.clientX || 0
  dragStartY = e.clientY || e.touches?.[0]?.clientY || 0
  petStartX = x.value
  petStartY = y.value
  scale.value = 1.08
  rotation.value = -5
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
  document.addEventListener('touchmove', onDragMove, { passive: false })
  document.addEventListener('touchend', onDragEnd)
}

function onDragMove(e) {
  if (!dragging.value) return
  const cx = e.clientX || e.touches?.[0]?.clientX || 0
  const cy = e.clientY || e.touches?.[0]?.clientY || 0
  x.value = Math.max(10, Math.min(window.innerWidth - 170, petStartX + (cx - dragStartX)))
  y.value = Math.max(10, Math.min(window.innerHeight - 290, petStartY + (cy - dragStartY)))
}

function onDragEnd() {
  dragging.value = false
  scale.value = 1
  rotation.value = 0
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.removeEventListener('touchmove', onDragMove)
  document.removeEventListener('touchend', onDragEnd)
}

// 双击回家
function onDoubleClick() {
  x.value = window.innerWidth - 160
  y.value = window.innerHeight - 280
  scale.value = 1.15
  setTimeout(() => { scale.value = 1 }, 300)
}

onMounted(() => {
  startIdleFloat()
  // 初始小动画：从右下角弹出
  x.value = window.innerWidth
  setTimeout(() => {
    x.value = window.innerWidth - 160
  }, 500)
})

onUnmounted(() => {
  clearInterval(floatTimer)
  clearTimeout(idleTimer)
})
</script>

<template>
  <div
    class="floating-pet"
    :class="{ 'is-dragging': dragging }"
    :style="{
      left: x + 'px',
      top: y + 'px',
      transform: `scale(${scale}) rotate(${rotation}deg)`,
    }"
    @mousedown.prevent="onDragStart"
    @touchstart.prevent="onDragStart"
    @dblclick="onDoubleClick"
    title="可以拖我哦~ 双击送我回家"
  >
    <img
      src="/images/people.png"
      alt="桌宠"
      class="pet-img"
      draggable="false"
    />
    <!-- 小气泡 -->
    <div class="pet-bubble">戳戳我~</div>
  </div>
</template>

<style scoped>
.floating-pet {
  position: fixed;
  z-index: 9997;
  cursor: grab;
  user-select: none;
  -webkit-user-drag: none;
  transition:
    left 2s ease-in-out,
    top 2s ease-in-out,
    transform 0.4s var(--ease-out-back, cubic-bezier(0.34, 1.56, 0.64, 1));
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
}

.floating-pet.is-dragging {
  transition: transform 0.15s ease;
  cursor: grabbing;
  filter: drop-shadow(0 8px 20px rgba(0, 0, 0, 0.25));
}

.pet-img {
  width: 140px;
  height: auto;
  pointer-events: none;
}

.pet-bubble {
  position: absolute;
  top: -36px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.92);
  color: var(--color-body, #4C5851);
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.floating-pet:hover .pet-bubble {
  opacity: 1;
}

/* 移动端缩小 */
@media (max-width: 768px) {
  .pet-img {
    width: 100px;
  }
  .pet-bubble {
    font-size: 11px;
    top: -28px;
  }
}
</style>
