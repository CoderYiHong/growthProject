<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const visible = ref(false)

onMounted(() => {
  requestAnimationFrame(() => {
    requestAnimationFrame(() => { visible.value = true })
  })
})
</script>

<template>
  <section class="hero" :class="{ entered: visible }">
    <div class="hero-inner container-wide">
      <div class="hero-text">
        <p class="hero-greeting fade-up" style="--d:0s">你好，我是</p>
        <h1 class="hero-name fade-up" style="--d:0.1s">Orange</h1>
        <p class="hero-desc fade-up" style="--d:0.2s">
          记录学习、项目与长期成长的数据科学学习者
        </p>
        <p class="hero-detail fade-up" style="--d:0.3s">
          专注于全栈开发、数据科学与 AI 自动化，<br class="hero-br" />用代码创造价值，用学习驱动成长。
        </p>
        <div class="hero-actions fade-up" style="--d:0.4s">
          <button class="btn-hero btn-hero-primary" @click="router.push('/growth')">
            探索我的成长
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </button>
          <button class="btn-hero btn-hero-outline" @click="router.push('/projects')">
            查看项目作品
          </button>
        </div>
        <div class="hero-links fade-up" style="--d:0.5s">
          <a href="https://github.com/CoderYiHong" target="_blank" class="hero-link">GitHub</a>
          <a href="https://blog.csdn.net/Yihong1833100198?type=blog" target="_blank" class="hero-link">CSDN</a>
          <a href="https://zhihu.com/people/yihong" target="_blank" class="hero-link">知乎</a>
        </div>
      </div>
    </div>

    <!-- 底部暗示 -->
    <div class="hero-scroll-hint">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><polyline points="6 9 12 15 18 9"/></svg>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  min-height: 460px;
  display: flex;
  align-items: center;
  padding: 48px 0 32px;
  overflow: hidden;
}

.hero-inner {
  width: 100%;
  position: relative;
  z-index: 1;
}

.hero-text {
  max-width: 640px;
}

/* 入场动画 */
.fade-up {
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.5s ease, transform 0.5s ease;
  transition-delay: var(--d, 0s);
}

.hero.entered .fade-up {
  opacity: 1;
  transform: translateY(0);
}

.hero-greeting {
  font-size: 16px;
  color: var(--color-secondary);
  margin-bottom: 4px;
}

.hero-name {
  font-size: 52px;
  font-weight: 800;
  color: var(--color-primary);
  line-height: 1.1;
  margin-bottom: 12px;
  letter-spacing: -1px;
}

.hero-desc {
  font-size: 18px;
  color: var(--color-body);
  font-weight: 500;
  margin-bottom: 8px;
}

.hero-detail {
  font-size: 15px;
  color: var(--color-secondary);
  line-height: 1.8;
  margin-bottom: 24px;
}

.hero-br {
  display: none;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.btn-hero {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 11px 22px;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  text-decoration: none;
}

.btn-hero-primary {
  background: var(--color-primary);
  color: #fff;
}

.btn-hero-primary:hover {
  background: #3D8B47;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 157, 87, 0.3);
}

.btn-hero-outline {
  background: transparent;
  color: var(--color-primary);
  border: 1.5px solid var(--color-primary);
}

.btn-hero-outline:hover {
  background: var(--color-primary-light);
  transform: translateY(-1px);
}

.hero-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-link {
  padding: 5px 14px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 13px;
  color: var(--color-secondary);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.15s ease;
}

.hero-link:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

/* 底部滚动提示 */
.hero-scroll-hint {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  color: var(--color-muted);
  animation: bounceDown 2s ease infinite;
  z-index: 1;
}

@keyframes bounceDown {
  0%, 100% { transform: translateX(-50%) translateY(0); opacity: 0.5; }
  50% { transform: translateX(-50%) translateY(6px); opacity: 1; }
}

@media (max-width: 768px) {
  .hero {
    min-height: 380px;
    padding: 32px 0 24px;
  }

  .hero-name {
    font-size: 38px;
  }

  .hero-desc {
    font-size: 16px;
  }

  .hero-detail {
    font-size: 14px;
  }

  .hero-br {
    display: inline;
  }

  .hero-actions {
    flex-direction: column;
  }

  .btn-hero {
    justify-content: center;
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .fade-up { opacity: 1; transform: none; transition: none; }
  .hero.entered .fade-up { transform: none; }
  .hero-scroll-hint { animation: none; display: none; }
}
</style>
