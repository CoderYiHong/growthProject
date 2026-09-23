<script setup>
defineProps({
  skillGroups: { type: Array, required: true }
})

// 技能等级对应的颜色
function levelStyle(level) {
  const map = {
    '掌握': { bg: '#EAF5E8', color: '#4F9D57' },
    '熟悉': { bg: '#E8F4FD', color: '#6CB4EE' },
    '了解': { bg: '#DCE5DE', color: '#5B7262' }
  }
  return map[level] || map['了解']
}
</script>

<template>
  <section class="resume-section" v-if="skillGroups && skillGroups.length > 0">
    <h2 class="section-title">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="4 17 10 11 4 17"/><line x1="12" y1="17" x2="20" y2="17"/></svg>
      专业技能
    </h2>

    <div class="skills-grid">
      <div v-for="group in skillGroups" :key="group.category" class="skill-group">
        <h4 class="skill-group-title">{{ group.category }}</h4>
        <div class="skill-items">
          <div v-for="skill in group.items" :key="skill.id" class="skill-item">
            <div class="skill-info-row">
              <span class="skill-name">{{ skill.name }}</span>
              <span
                class="skill-level-tag"
                :style="{ background: levelStyle(skill.level).bg, color: levelStyle(skill.level).color }"
              >{{ skill.level }}</span>
            </div>
            <div class="skill-bar-track">
              <div
                class="skill-bar-fill"
                :style="{ width: skill.percentage + '%', background: levelStyle(skill.level).color }"
              ></div>
            </div>
          </div>
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

.skills-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.skill-group {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.skill-group-title {
  font-size: var(--font-size-md);
  color: var(--color-title);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--color-primary-light);
}

.skill-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skill-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.skill-name {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-body);
}

.skill-level-tag {
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.skill-bar-track {
  height: 5px;
  background: var(--color-border-light);
  border-radius: 3px;
  overflow: hidden;
}

.skill-bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

@media (max-width: 768px) {
  .skills-grid {
    grid-template-columns: 1fr;
  }
}
</style>
