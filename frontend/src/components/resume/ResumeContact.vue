<script setup>
defineProps({
  contact: { type: Object, required: true }
})
</script>

<template>
  <section class="resume-section" v-if="contact">
    <h2 class="section-title">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
      联系方式
    </h2>

    <div class="contact-grid">
      <div class="contact-item">
        <div class="contact-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
        </div>
        <div v-if="contact.email">
          <span class="contact-label">邮箱</span>
          <a :href="'mailto:' + contact.email" class="contact-value">{{ contact.email }}</a>
        </div>
      </div>

      <div class="contact-item" v-if="contact.github">
        <div class="contact-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 00-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0020 4.77 5.07 5.07 0 0019.91 1S18.73.65 16 2.48a13.38 13.38 0 00-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 005 4.77a5.44 5.44 0 00-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 009 18.13V22"/></svg>
        </div>
        <div>
          <span class="contact-label">GitHub</span>
          <a :href="contact.github" target="_blank" class="contact-value">{{ contact.github }}</a>
        </div>
      </div>

      <div class="contact-item" v-if="contact.csdn">
        <div class="contact-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>
        </div>
        <div>
          <span class="contact-label">CSDN 博客</span>
          <a :href="contact.csdn" target="_blank" class="contact-value">访问博客</a>
        </div>
      </div>

      <div class="contact-item" v-if="contact.location">
        <div class="contact-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
        </div>
        <div>
          <span class="contact-label">所在地</span>
          <span class="contact-value">{{ contact.location }}</span>
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

.contact-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.contact-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
}

.contact-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-light);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.contact-label {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--color-muted);
  margin-bottom: 2px;
}

.contact-value {
  font-size: var(--font-size-sm);
  color: var(--color-title);
  font-weight: 500;
  text-decoration: none;
  word-break: break-all;
}

a.contact-value:hover {
  color: var(--color-primary);
  text-decoration: underline;
}

@media (max-width: 768px) {
  .contact-grid {
    grid-template-columns: 1fr;
  }
}
</style>
