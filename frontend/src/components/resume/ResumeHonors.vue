<script setup>
defineProps({
  honors: { type: Array, required: true }
})

// 荣誉级别对应的颜色
function levelColor(level) {
  const map = {
    '国家级': '#D84040',
    '省级': '#F0B85A',
    '校级': '#6CB4EE',
    '厂商认证': '#4F9D57',
    '平台认证': '#9B7EC4'
  }
  return map[level] || '#849089'
}
</script>

<template>
  <section class="resume-section" v-if="honors && honors.length > 0">
    <h2 class="section-title">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="7"/><polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></svg>
      个人荣誉
    </h2>

    <div class="honors-grid">
      <div v-for="honor in honors" :key="honor.id" class="honor-card">
        <div class="honor-badge" :style="{ background: levelColor(honor.level) }">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
        </div>
        <div class="honor-content">
          <div class="honor-header-row">
            <h4 class="honor-name">{{ honor.name }}</h4>
            <span class="honor-level-tag" :style="{ color: levelColor(honor.level), background: levelColor(honor.level) + '18' }">
              {{ honor.level }}
            </span>
          </div>
          <div class="honor-meta">
            <span v-if="honor.issuingOrganization">{{ honor.issuingOrganization }}</span>
            <span>{{ honor.issueDate }}</span>
          </div>
          <p class="honor-desc" v-if="honor.description">{{ honor.description }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.resume-section {
  padding: var(--spacing-lg) 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-xl);
  margin-bottom: var(--spacing-md);
  color: var(--color-title);
}

.honors-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.honor-card {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--color-bg-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.honor-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.honor-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.honor-content {
  flex: 1;
  min-width: 0;
}

.honor-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.honor-name {
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.honor-level-tag {
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.honor-meta {
  font-size: 11px;
  color: var(--color-muted);
  margin-bottom: 4px;
  display: flex;
  gap: 8px;
}

.honor-desc {
  font-size: 12px;
  color: var(--color-secondary);
  line-height: 1.5;
}

@media (max-width: 768px) {
  .honors-grid {
    grid-template-columns: 1fr;
  }
}
</style>
