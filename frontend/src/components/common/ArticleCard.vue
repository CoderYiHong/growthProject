<script setup>
import { ref, watch } from 'vue'
import { formatDate, formatViews } from '@/utils'

const props = defineProps({
  article: { type: Object, required: true }
})

const coverLoadFailed = ref(false)

watch(
  () => props.article.effectiveCover,
  () => { coverLoadFailed.value = false }
)

function handleCoverError() {
  coverLoadFailed.value = true
}
</script>

<template>
  <router-link :to="`/articles/${article.id}`" class="article-card">
    <!-- 封面 -->
    <div class="article-cover">
      <img
        v-if="article.effectiveCover && !coverLoadFailed"
        :src="article.effectiveCover"
        :alt="`${article.title}封面`"
        class="article-cover-image"
        loading="lazy"
        @error="handleCoverError"
      />
      <div
        v-else
        class="article-cover-bg"
        :style="{ background: `linear-gradient(135deg, hsl(${article.id * 40 % 360}, 40%, 92%), hsl(${article.id * 40 % 360}, 30%, 96%))` }"
      >
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="1.5" opacity="0.3">
          <path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>
        </svg>
      </div>
      <span class="article-category-tag">{{ article.categoryLabel }}</span>
    </div>

    <!-- 信息 -->
    <div class="article-info">
      <h3 class="article-title">{{ article.title }}</h3>
      <p class="article-summary">{{ article.summary }}</p>

      <!-- 标签 -->
      <div class="article-tags">
        <span v-for="tag in article.tags.slice(0, 3)" :key="tag" class="tag tag-sm">{{ tag }}</span>
      </div>

      <!-- 元信息 -->
      <div class="article-meta">
        <span class="article-date">{{ formatDate(article.createdAt, 'YYYY-MM-DD') }}</span>
        <span class="article-divider">·</span>
        <span class="article-views">{{ formatViews(article.views) }} 阅读</span>
        <span class="article-divider">·</span>
        <span class="article-readtime">{{ article.readTime }}</span>
      </div>
    </div>
  </router-link>
</template>

<style scoped>
.article-card {
  display: flex;
  flex-direction: column;
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  text-decoration: none;
}

.article-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

/* 封面 */
.article-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.article-cover-bg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.article-cover-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal);
}

.article-card:hover .article-cover-image {
  transform: scale(1.03);
}

.article-category-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 3px 10px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  font-size: 11px;
  color: var(--color-primary);
  font-weight: 500;
}

/* 信息 */
.article-info {
  padding: var(--spacing-md);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-title {
  font-size: var(--font-size-lg);
  line-height: 1.4;
  margin-bottom: var(--spacing-xs);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
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

.article-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: var(--spacing-sm);
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-xs);
  color: var(--color-muted);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
}

.article-divider {
  color: var(--color-border);
}
</style>
