<script setup>
defineProps({
  plans: { type: Array, required: true }
})

const iconMap = {
  book: 'M4 19.5A2.5 2.5 0 016.5 17H20M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z',
  code: 'M16 18l6-6-6-6M8 6l-6 6 6 6',
  lang: 'M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z M8 10h8M8 14h4',
  media: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8'
}

function statusClass(s) {
  return s === 'done' ? 'done' : s === 'in_progress' ? 'in-progress' : 'pending'
}

function statusLabel(s) {
  return s === 'done' ? '已完成' : s === 'in_progress' ? '进行中' : '待开始'
}
</script>

<template>
  <section class="section">
    <div class="container-wide">
      <h2 class="section-title">当前成长计划</h2>
      <p class="section-sub">四个方向，持续精进</p>

      <div class="plan-grid">
        <div v-for="plan in plans" :key="plan.id" class="plan-item">
          <div class="plan-header">
            <span class="plan-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path :d="iconMap[plan.icon] || iconMap.code" />
              </svg>
            </span>
            <div>
              <h4 class="plan-direction">{{ plan.direction }}</h4>
              <p class="plan-phase">{{ plan.currentPhase }}</p>
            </div>
          </div>

          <div class="plan-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: plan.progress + '%' }"></div>
            </div>
            <span class="progress-text">{{ plan.progress }}%</span>
          </div>

          <p class="plan-hours">本周投入：{{ plan.weeklyHours }} 小时</p>

          <div class="plan-tasks">
            <div v-for="task in plan.recentTasks" :key="task.name" class="task-row">
              <span class="task-dot" :class="statusClass(task.status)"></span>
              <span class="task-name">{{ task.name }}</span>
              <span class="task-tag" :class="statusClass(task.status)">{{ statusLabel(task.status) }}</span>
            </div>
          </div>

          <div class="plan-goals">
            <span v-for="goal in plan.goals" :key="goal" class="plan-goal-tag">{{ goal }}</span>
          </div>
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
  margin-bottom: 24px;
}

.plan-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.plan-item {
  background: rgba(255,255,255,0.8);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 20px;
  backdrop-filter: blur(6px);
}

.plan-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.plan-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.plan-direction {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-title);
  margin-bottom: 2px;
}

.plan-phase {
  font-size: 12px;
  color: var(--color-secondary);
}

.plan-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: var(--color-border-light);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 3px;
  transition: width 0.6s ease;
}

.progress-text {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
}

.plan-hours {
  font-size: 12px;
  color: var(--color-muted);
  margin-bottom: 12px;
}

.plan-tasks {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
  padding: 10px;
  background: var(--color-bg);
  border-radius: 4px;
}

.task-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.task-dot.done { background: var(--color-primary); }
.task-dot.in-progress { background: var(--color-accent); }
.task-dot.pending { background: var(--color-muted); }

.task-name {
  font-size: 12px;
  color: var(--color-body);
  flex: 1;
}

.task-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 500;
}

.task-tag.done { background: var(--color-primary-light); color: var(--color-primary); }
.task-tag.in-progress { background: var(--color-accent-light); color: #B8860B; }
.task-tag.pending { background: var(--color-bg); color: var(--color-muted); }

.plan-goals {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.plan-goal-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  background: var(--color-bg);
  color: var(--color-secondary);
  border-radius: 3px;
  border: 1px solid var(--color-border-light);
}

@media (max-width: 1200px) { .plan-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .plan-grid { grid-template-columns: 1fr; } }
</style>
