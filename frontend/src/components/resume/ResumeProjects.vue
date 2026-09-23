<script setup>
defineProps({
  projects: { type: Array, required: true }
})

function handleImgError(e) {
  e.target.style.display = 'none'
}
</script>

<template>
  <section class="resume-section" v-if="projects && projects.length > 0">
    <h2 class="section-title">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
      代表项目
    </h2>

    <div class="projects-list">
      <div v-for="project in projects" :key="project.id" class="project-card">
        <div class="project-cover-col">
          <div class="project-cover">
            <img
              v-if="project.cover"
              :src="project.cover"
              :alt="project.name"
              @error="handleImgError"
            />
            <svg v-else width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="1.5" opacity="0.2">
              <rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
            </svg>
          </div>
        </div>

        <div class="project-info-col">
          <h4 class="project-name">{{ project.name }}</h4>
          <p class="project-summary">{{ project.summary }}</p>
          <p class="project-responsibility">
            <span class="resp-label">职责：</span>{{ project.responsibility }}
          </p>
          <div class="project-techs">
            <span v-for="tech in project.technologies" :key="tech" class="project-tech-tag">{{ tech }}</span>
          </div>
          <div class="project-links" v-if="project.repositoryUrl && project.repositoryUrl !== '#'">
            <a :href="project.repositoryUrl" target="_blank" class="project-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 00-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0020 4.77 5.07 5.07 0 0019.91 1S18.73.65 16 2.48a13.38 13.38 0 00-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 005 4.77a5.44 5.44 0 00-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 009 18.13V22"/></svg>
              查看源码
            </a>
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

.projects-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.project-card {
  display: flex;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
  background: var(--color-bg-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.project-card:hover {
  box-shadow: var(--shadow-md);
}

.project-cover-col {
  flex-shrink: 0;
}

.project-cover {
  width: 140px;
  height: 100px;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-bg));
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.project-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.project-info-col {
  flex: 1;
  min-width: 0;
}

.project-name {
  font-size: var(--font-size-lg);
  margin-bottom: 6px;
}

.project-summary {
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.project-responsibility {
  font-size: var(--font-size-sm);
  color: var(--color-body);
  margin-bottom: 8px;
}

.resp-label {
  font-weight: 500;
  color: var(--color-secondary);
}

.project-techs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.project-tech-tag {
  padding: 2px 8px;
  background: var(--color-bg);
  border-radius: 4px;
  font-size: 11px;
  color: var(--color-secondary);
  border: 1px solid var(--color-border-light);
}

.project-links {
  display: flex;
  gap: var(--spacing-sm);
}

.project-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.project-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .project-card {
    flex-direction: column;
  }

  .project-cover {
    width: 100%;
    height: 140px;
  }
}
</style>
