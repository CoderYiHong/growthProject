<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="not-found-page">
    <div class="nf-content">
      <div class="nf-icon">
        <svg width="80" height="80" viewBox="0 0 48 48" fill="none">
          <rect width="48" height="48" rx="12" fill="#EAF5E8"/>
          <path d="M24 8C24 8 16 18 16 26C16 30.4 19.6 34 24 34C28.4 34 32 30.4 32 26C32 18 24 8 24 8Z" fill="#4F9D57" opacity="0.5"/>
          <circle cx="24" cy="25" r="4" fill="#24463A" opacity="0.5"/>
          <line x1="16" y1="16" x2="32" y2="32" stroke="#849089" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <h1 class="nf-title">404</h1>
      <p class="nf-desc">页面找不到了，就像迷路的种子还未找到土壤。</p>
      <div class="nf-actions">
        <button class="btn-primary" @click="router.push('/')">返回首页</button>
        <button class="btn-outline" @click="router.go(-1)">返回上一页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - var(--header-height));
  padding: var(--spacing-2xl);
}

.nf-content {
  text-align: center;
}

.nf-icon {
  margin-bottom: var(--spacing-lg);
}

.nf-title {
  font-size: 80px;
  font-weight: 800;
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: var(--spacing-sm);
}

.nf-desc {
  font-size: var(--font-size-md);
  color: var(--color-secondary);
  margin-bottom: var(--spacing-xl);
}

.nf-actions {
  display: flex;
  gap: var(--spacing-sm);
  justify-content: center;
}
</style>
