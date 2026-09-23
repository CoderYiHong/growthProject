<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  project: { type: Object, required: true },
  layout: { type: String, default: 'grid' } // grid | list
})

const coverLoadFailed = ref(false)

watch(
  () => props.project.effectiveCover,
  () => { coverLoadFailed.value = false }
)

const coverStyle = computed(() => {
  // 使用渐变色作为占位背景
  const colors = ['#EAF5E8', '#E8F4FD', '#FEF6E8', '#F0E8F5', '#E8F5F2', '#F5F0E8']
  const colorIndex = props.project.id % colors.length
  return { background: `linear-gradient(135deg, ${colors[colorIndex]}, #fff)` }
})

function handleCoverError() {
  coverLoadFailed.value = true
}
</script>

<template>
  <router-link
    :to="`/projects/${project.id}`"
    class="project-card"
    :class="[layout === 'list' ? 'project-card-list' : '']"
  >
    <!-- 封面 -->
    <div class="project-cover" :style="coverStyle">
      <img
        v-if="project.effectiveCover && !coverLoadFailed"
        :src="project.effectiveCover"
        :alt="`${project.name}封面`"
        class="project-cover-image"
        loading="lazy"
        @error="handleCoverError"
      />
      <div class="project-cover-overlay">
        <span class="project-category-tag">{{ project.categoryLabel }}</span>
      </div>
      <!-- 无图片时显示图标 -->
      <div v-if="!project.effectiveCover || coverLoadFailed" class="project-cover-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="1.5" opacity="0.3">
          <rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
        </svg>
      </div>
    </div>

    <!-- 信息 -->
    <div class="project-info">
      <h3 class="project-name">{{ project.name }}</h3>
      <p class="project-summary">{{ project.summary }}</p>

      <!-- 标签 -->
      <div class="project-tags">
        <span
          v-for="tech in project.techStack.slice(0, 4)"
          :key="tech"
          class="tag tag-sm"
        >{{ tech }}</span>
        <span v-if="project.techStack.length > 4" class="tag tag-sm tag-more">
          +{{ project.techStack.length - 4 }}
        </span>
      </div>

      <!-- 本人角色 & 项目链接 -->
      <div class="project-meta-row">
        <span v-if="project.role" class="project-role">{{ project.role }}</span>
        <a v-if="project.sourceUrl && project.sourceUrl !== '#'" :href="project.sourceUrl" target="_blank" class="project-source-link" @click.stop>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 00-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0020 4.77 5.07 5.07 0 0019.91 1S18.73.65 16 2.48a13.38 13.38 0 00-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 005 4.77a5.44 5.44 0 00-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 009 18.13V22"/></svg>
          GitHub
        </a>
      </div>

      <!-- 底部 -->
      <div class="project-footer-row">
        <span class="project-status" :class="project.status">
          <span class="status-dot"></span>
          {{ project.statusLabel }}
        </span>
        <span class="project-link-text">
          查看详情
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
        </span>
      </div>
    </div>
  </router-link>
</template>

<style scoped>
.project-card {
  display: flex;
  flex-direction: column;
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  text-decoration: none;
  cursor: pointer;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

/* 封面 */
.project-cover {
  position: relative;
  height: 200px;
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.project-cover-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal);
}

.project-card:hover .project-cover-image {
  transform: scale(1.03);
}

.project-cover-overlay {
  z-index: 1;
}

.project-cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-sm);
  display: flex;
  justify-content: flex-end;
}

.project-category-tag {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  color: var(--color-primary);
  backdrop-filter: blur(4px);
}

.project-cover-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* 信息 */
.project-info {
  padding: var(--spacing-md);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.project-name {
  font-size: var(--font-size-lg);
  margin-bottom: var(--spacing-xs);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-summary {
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: var(--spacing-sm);
  flex: 1;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: var(--spacing-sm);
}

.tag-more {
  background: var(--color-bg);
  color: var(--color-secondary);
}

.project-footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
  margin-top: auto;
}

.project-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
  margin-top: auto;
}

.project-role {
  font-size: 11px;
  color: var(--color-muted);
  padding: 2px 6px;
  background: var(--color-bg);
  border-radius: 3px;
}

.project-source-link {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--color-secondary);
  text-decoration: none;
  transition: color 0.15s;
}

.project-source-link:hover {
  color: var(--color-primary);
}

.project-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-xs);
  color: var(--color-secondary);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-secondary);
}

.project-status.in_progress .status-dot {
  background: #6CB4EE;
}

.project-status.completed .status-dot {
  background: var(--color-primary);
}

.project-link-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  font-weight: 500;
}

/* 列表模式 */
.project-card-list {
  flex-direction: row;
}

.project-card-list .project-cover {
  width: 280px;
  height: auto;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .project-card-list {
    flex-direction: column;
  }

  .project-card-list .project-cover {
    width: 100%;
    height: 180px;
  }
}
</style>
