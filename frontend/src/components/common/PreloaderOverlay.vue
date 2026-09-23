<script setup>
import { ref, onMounted } from 'vue'

const emit = defineEmits(['complete'])
const exiting = ref(false)
const reducedMotion = ref(false)

onMounted(() => {
  reducedMotion.value = window.matchMedia('(prefers-reduced-motion: reduce)').matches

  if (reducedMotion.value) {
    setTimeout(() => emit('complete'), 800)
    return
  }

  // 动画时间线
  setTimeout(() => { exiting.value = true }, 2200)
  setTimeout(() => emit('complete'), 2800)
})
</script>

<template>
  <div class="preloader-overlay" :class="{ 'is-exiting': exiting, 'reduced-motion': reducedMotion }">
    <div class="preloader-center">
      <!-- SVG 植物生长 -->
      <div class="preloader-icon-wrapper">
        <svg class="preloader-svg" viewBox="0 0 80 120" width="80" height="120" aria-hidden="true">
          <!-- 花盆 -->
          <path class="preloader-pot"
                d="M25 100 L55 100 L60 115 L20 115 Z"
                fill="rgba(255,255,255,0.08)"
                stroke="rgba(255,255,255,0.15)"
                stroke-width="1" />
          <!-- 土壤 -->
          <ellipse class="preloader-soil"
                   cx="40" cy="98" rx="14" ry="4"
                   fill="rgba(255,255,255,0.06)" />
          <!-- 茎干 -->
          <path class="preloader-stem"
                d="M40 95 Q38 70 42 50 Q44 35 40 20"
                fill="none"
                stroke="rgba(255,255,255,0.6)"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-dasharray="160"
                stroke-dashoffset="160" />
          <!-- 左叶 -->
          <path class="preloader-leaf preloader-leaf--left"
                d="M42 55 Q25 48 20 35 Q30 40 42 45Z"
                fill="rgba(255,255,255,0.5)" />
          <!-- 右叶 -->
          <path class="preloader-leaf preloader-leaf--right"
                d="M42 40 Q55 30 58 22 Q52 32 42 38Z"
                fill="rgba(255,255,255,0.5)" />
          <!-- 顶芽 -->
          <circle class="preloader-bud"
                  cx="40" cy="18" r="3"
                  fill="rgba(255,255,255,0.7)" />
        </svg>
      </div>

      <!-- 品牌文字 -->
      <h1 class="preloader-title">Orange<span class="preloader-dot">.</span></h1>
      <p class="preloader-tagline">在成长中前行</p>
    </div>

    <!-- 底部进度条 -->
    <div class="preloader-progress">
      <div class="preloader-progress-track">
        <div class="preloader-progress-fill"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ---- 遮罩容器 ---- */
.preloader-overlay {
  position: fixed;
  inset: 0;
  z-index: var(--preloader-z, 9999);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg,
    var(--color-primary-dark, #24463A) 0%,
    #1a3022 40%,
    var(--color-primary, #4F9D57) 100%
  );
  overflow: hidden;
  transition: opacity 0.6s var(--ease-out-expo, cubic-bezier(0.16, 1, 0.3, 1)),
              transform 0.6s var(--ease-out-expo, cubic-bezier(0.16, 1, 0.3, 1));
}

.preloader-overlay.is-exiting {
  opacity: 0;
  transform: scale(1.05);
  pointer-events: none;
}

/* ---- 中心内容 ---- */
.preloader-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  margin-top: -30px;
}

.preloader-svg {
  will-change: transform;
}

/* ---- SVG 动画 ---- */

/* 茎干描边 */
.preloader-stem {
  animation: drawPath 1s var(--ease-out-expo, cubic-bezier(0.16, 1, 0.3, 1)) 0.2s forwards;
}

/* 叶片 */
.preloader-leaf {
  transform: scale(0);
  transform-origin: center;
  opacity: 0;
}

.preloader-leaf--left {
  animation: leafGrow 0.5s var(--ease-out-back, cubic-bezier(0.34, 1.56, 0.64, 1)) 0.5s forwards;
}

.preloader-leaf--right {
  animation: leafGrow 0.5s var(--ease-out-back, cubic-bezier(0.34, 1.56, 0.64, 1)) 0.7s forwards;
}

/* 顶芽 */
.preloader-bud {
  opacity: 0;
  animation: budAppear 0.3s ease 1.1s forwards;
}

@keyframes budAppear {
  from { opacity: 0; transform: scale(0.5); }
  to   { opacity: 1; transform: scale(1); }
}

/* ---- 文字 ---- */
.preloader-title {
  font-size: 48px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 8px;
  opacity: 0;
  filter: blur(4px);
  animation: textReveal 0.7s var(--ease-out-expo, cubic-bezier(0.16, 1, 0.3, 1)) 1s forwards;
}

.preloader-dot {
  color: rgba(255, 255, 255, 0.5);
}

.preloader-tagline {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 400;
  letter-spacing: 4px;
  opacity: 0;
  transform: translateY(8px);
  animation: taglineFade 0.6s ease 1.4s forwards;
}

/* ---- 进度条 ---- */
.preloader-progress {
  position: absolute;
  bottom: 48px;
  left: 50%;
  transform: translateX(-50%);
  width: 240px;
}

.preloader-progress-track {
  height: 2px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.preloader-progress-fill {
  height: 100%;
  width: 100%;
  background: rgba(255, 255, 255, 0.45);
  transform: scaleX(0);
  transform-origin: left;
  animation: progressFill 2.2s var(--ease-out-expo, cubic-bezier(0.16, 1, 0.3, 1)) forwards;
}

/* ---- 移动端 ---- */
@media (max-width: 768px) {
  .preloader-title {
    font-size: 36px;
  }

  .preloader-svg {
    transform: scale(0.85);
  }

  .preloader-progress {
    width: 160px;
    bottom: 36px;
  }
}

/* ---- 无障碍 — 减少动画 ---- */
@media (prefers-reduced-motion: reduce) {
  .preloader-overlay {
    transition: opacity 0.3s ease;
  }

  .preloader-stem,
  .preloader-leaf,
  .preloader-bud,
  .preloader-title,
  .preloader-tagline,
  .preloader-progress-fill {
    animation: none;
    opacity: 1;
    transform: none;
    filter: none;
    stroke-dashoffset: 0;
  }

  .preloader-title {
    letter-spacing: normal;
  }

  .preloader-overlay.is-exiting {
    opacity: 0;
    transform: none;
  }
}
</style>
