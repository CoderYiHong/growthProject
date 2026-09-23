<script setup>
defineProps({
  weekly: { type: Object, required: true },
  monthly: { type: Object, default: null }
})

const activeTab = defineModel('tab', { default: 'week' })
</script>

<template>
  <section class="section">
    <div class="container-wide">
      <h2 class="section-title">阶段总结</h2>
      <p class="section-sub">定期回顾，明确目标</p>

      <div class="tab-row">
        <button class="tab-btn" :class="{ active: activeTab === 'week' }" @click="activeTab = 'week'">本周</button>
        <button class="tab-btn" :class="{ active: activeTab === 'month' }" @click="activeTab = 'month'">本月</button>
      </div>

      <div v-if="activeTab === 'week' && weekly" class="summary-card">
        <div class="summary-header">
          <h4>{{ weekly.week }}</h4>
        </div>

        <div class="stats-row">
          <div class="stat">
            <span class="stat-value">{{ weekly.totalLearningHours }}</span>
            <span class="stat-label">学习时长 (h)</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ weekly.projectCount }}</span>
            <span class="stat-label">推进项目</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ weekly.completedItems.length }}</span>
            <span class="stat-label">完成事项</span>
          </div>
        </div>

        <div class="summary-section">
          <h5 class="summary-label">本周完成</h5>
          <ul class="checklist">
            <li v-for="(item, i) in weekly.completedItems" :key="i" class="checklist-item">{{ item }}</li>
          </ul>
        </div>

        <div class="summary-section">
          <h5 class="summary-label">下周计划</h5>
          <ul class="checklist">
            <li v-for="(item, i) in weekly.nextWeekGoals" :key="i" class="checklist-item next">{{ item }}</li>
          </ul>
        </div>
      </div>

      <div v-if="activeTab === 'month' && monthly" class="summary-card">
        <div class="summary-header">
          <h4>{{ monthly.month }}</h4>
        </div>

        <div class="stats-row">
          <div class="stat">
            <span class="stat-value">{{ monthly.totalLearningHours }}</span>
            <span class="stat-label">学习时长 (h)</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ monthly.projectCount }}</span>
            <span class="stat-label">推进项目</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ monthly.articleCount }}</span>
            <span class="stat-label">文章产出</span>
          </div>
        </div>

        <div class="summary-section">
          <h5 class="summary-label">本月完成</h5>
          <ul class="checklist">
            <li v-for="(item, i) in monthly.completedItems" :key="i" class="checklist-item">{{ item }}</li>
          </ul>
        </div>

        <div class="summary-section">
          <h5 class="summary-label">下月目标</h5>
          <ul class="checklist">
            <li v-for="(item, i) in monthly.nextMonthGoals" :key="i" class="checklist-item next">{{ item }}</li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-title);
  margin-bottom: 4px;
}

.section-sub {
  font-size: 14px;
  color: var(--color-secondary);
  margin-bottom: 16px;
}

.tab-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tab-btn {
  padding: 6px 16px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 13px;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
  transition: all 0.15s ease;
}

.tab-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.tab-btn.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }

.summary-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: rgba(255,255,255,0.8);
  padding: 24px;
  backdrop-filter: blur(6px);
}

.summary-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-title);
  margin-bottom: 16px;
}

.stats-row {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-light);
}

.stat {
  text-align: center;
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--color-muted);
}

.summary-section {
  margin-bottom: 16px;
}

.summary-section:last-child { margin-bottom: 0; }

.summary-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-title);
  margin-bottom: 8px;
}

.checklist {
  list-style: none;
  padding: 0;
}

.checklist-item {
  position: relative;
  padding: 4px 0 4px 20px;
  font-size: 13px;
  color: var(--color-body);
  line-height: 1.6;
}

.checklist-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 11px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  opacity: 0.5;
}

.checklist-item.next::before {
  background: var(--color-accent);
  opacity: 0.7;
}

@media (max-width: 768px) {
  .summary-card { padding: 16px; }
  .stats-row { gap: 12px; }
  .stat-value { font-size: 22px; }
}
</style>
