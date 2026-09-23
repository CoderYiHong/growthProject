<script setup>
defineProps({
  abilities: { type: Array, required: true }
})
</script>

<template>
  <section class="resume-section" v-if="abilities && abilities.length > 0">
    <h2 class="section-title">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
      核心能力
    </h2>

    <div class="abilities-grid">
      <div v-for="ability in abilities" :key="ability.id" class="ability-card">
        <div class="ability-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="2">
            <template v-if="ability.icon === 'code'">
              <polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/>
            </template>
            <template v-else-if="ability.icon === 'mobile'">
              <rect x="5" y="2" width="14" height="20" rx="2"/><line x1="12" y1="18" x2="12" y2="18.01"/>
            </template>
            <template v-else-if="ability.icon === 'data'">
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/>
            </template>
            <template v-else-if="ability.icon === 'ai'">
              <path d="M12 2a4 4 0 014 4c0 2-4 6-4 6s-4-4-4-6a4 4 0 014-4z"/><path d="M12 12v4M10 18h4"/>
            </template>
            <template v-else>
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/>
            </template>
          </svg>
        </div>
        <h4 class="ability-title">{{ ability.title }}</h4>
        <p class="ability-desc">{{ ability.description }}</p>
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

.abilities-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md);
}

.ability-card {
  padding: var(--spacing-lg);
  background: var(--color-bg-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.ability-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.ability-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-sm);
}

.ability-title {
  font-size: var(--font-size-md);
  margin-bottom: var(--spacing-xs);
  color: var(--color-title);
}

.ability-desc {
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
  line-height: 1.7;
}

@media (max-width: 1024px) {
  .abilities-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .abilities-grid {
    grid-template-columns: 1fr;
  }
}
</style>
