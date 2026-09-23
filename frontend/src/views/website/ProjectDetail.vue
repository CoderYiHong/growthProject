<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProjectDetail } from '@/api/public'
import { formatDate, transformProject } from '@/utils'

const route = useRoute()
const router = useRouter()

const project = ref(null)
const activeTab = ref('overview')
const coverLoadFailed = ref(false)

onMounted(async () => {
  try {
    const res = await getProjectDetail(route.params.id)
    if (res.code === 200 && res.data) {
      project.value = transformProject(res.data)
    } else {
      router.replace('/projects')
    }
  } catch (e) {
    console.error('获取项目详情失败', e)
    router.replace('/projects')
  }
})

const tabs = [
  { value: 'overview', label: '项目概述' },
  { value: 'features', label: '核心功能' },
  { value: 'architecture', label: '技术架构' },
  { value: 'process', label: '开发过程' },
  { value: 'review', label: '项目复盘' }
]
</script>

<template>
  <div class="project-detail-page" v-if="project">
    <!-- Banner -->
    <section class="detail-banner">
      <div class="container">
        <!-- 面包屑 -->
        <div class="breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="separator">/</span>
          <router-link to="/projects">项目作品</router-link>
          <span class="separator">/</span>
          <span>{{ project.name }}</span>
        </div>
      </div>
    </section>

    <!-- 项目头部 -->
    <section class="detail-header-section">
      <div class="container">
        <div class="detail-header-grid">
          <!-- 左侧图片 -->
          <div class="detail-cover">
            <img
              v-if="project.effectiveCover && !coverLoadFailed"
              :src="project.effectiveCover"
              :alt="`${project.name}封面`"
              class="detail-cover-image"
              @error="coverLoadFailed = true"
            />
            <div v-else class="detail-cover-placeholder">
              <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="1" opacity="0.2">
                <rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
              </svg>
              <p>{{ project.name }}</p>
            </div>
          </div>

          <!-- 右侧信息 -->
          <div class="detail-info">
            <span class="detail-category">{{ project.categoryLabel }}</span>
            <h1 class="detail-title">{{ project.name }}</h1>
            <p class="detail-summary">{{ project.summary }}</p>

            <div class="detail-tags">
              <span v-for="tech in project.techStack" :key="tech" class="tag">{{ tech }}</span>
            </div>

            <div class="detail-status-row">
              <span class="detail-status" :class="project.status">
                <span class="status-dot"></span>
                {{ project.statusLabel }}
              </span>
            </div>

            <div class="detail-meta">
              <div class="meta-item">
                <span class="meta-label">我的职责</span>
                <span class="meta-value">{{ project.role }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">开发时间</span>
                <span class="meta-value">{{ project.devPeriod }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">创建日期</span>
                <span class="meta-value">{{ formatDate(project.createdAt) }}</span>
              </div>
            </div>

            <div class="detail-actions">
              <a v-if="project.demoUrl" :href="project.demoUrl" class="btn-primary" target="_blank" rel="noopener noreferrer">在线演示</a>
              <a v-if="project.sourceUrl" :href="project.sourceUrl" class="btn-outline" target="_blank" rel="noopener noreferrer">查看 GitHub</a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 标签页内容 -->
    <section class="detail-content-section section">
      <div class="container">
        <div class="detail-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            class="tab-btn"
            :class="{ active: activeTab === tab.value }"
            @click="activeTab = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="tab-content">
          <!-- 项目概述 -->
          <div v-if="activeTab === 'overview'" class="tab-panel">
            <div class="content-block">
              <h3>项目背景</h3>
              <p>{{ project.background }}</p>
            </div>
            <div class="content-block">
              <h3>项目目标</h3>
              <ul class="goal-list">
                <li v-for="(g, i) in project.goals" :key="i">{{ g }}</li>
              </ul>
            </div>
            <div class="content-block">
              <h3>项目成果</h3>
              <ul class="result-list">
                <li v-for="(r, i) in project.results" :key="i">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  {{ r }}
                </li>
              </ul>
            </div>
          </div>

          <!-- 核心功能 -->
          <div v-if="activeTab === 'features'" class="tab-panel">
            <div class="content-block">
              <h3>核心功能</h3>
              <div class="features-grid">
                <div v-for="(f, i) in project.features" :key="i" class="feature-item">
                  <span class="feature-num">{{ i + 1 }}</span>
                  <span>{{ f }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 技术架构 -->
          <div v-if="activeTab === 'architecture'" class="tab-panel">
            <div class="content-block">
              <h3>技术架构</h3>
              <div class="arch-diagram">
                <p>{{ project.architecture }}</p>
              </div>
            </div>
            <div class="content-block">
              <h3>技术栈详情</h3>
              <div class="tech-stack-detail">
                <span v-for="tech in project.techStack" :key="tech" class="tech-item">{{ tech }}</span>
              </div>
            </div>
          </div>

          <!-- 开发过程 -->
          <div v-if="activeTab === 'process'" class="tab-panel">
            <div class="content-block">
              <h3>遇到的问题与解决思路</h3>
              <div class="challenge-list">
                <div v-for="(c, i) in project.challenges" :key="i" class="challenge-item">
                  <div class="challenge-problem">
                    <span class="challenge-label">问题</span>
                    <p>{{ c.problem }}</p>
                  </div>
                  <div class="challenge-solution">
                    <span class="challenge-label solution">解决</span>
                    <p>{{ c.solution }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 项目复盘 -->
          <div v-if="activeTab === 'review'" class="tab-panel">
            <div class="content-block">
              <h3>项目复盘</h3>
              <p class="review-text">{{ project.review }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.detail-banner {
  padding: var(--spacing-md) 0;
  background: var(--color-bg);
}

/* 头部 */
.detail-header-section {
  padding: var(--spacing-xl) 0;
  background: var(--color-bg-white);
  border-bottom: 1px solid var(--color-border-light);
}

.detail-header-grid {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: var(--spacing-2xl);
}

.detail-cover {
  border-radius: var(--radius-lg);
  overflow: hidden;
  height: 300px;
  background: var(--color-bg);
}

.detail-cover-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-cover-placeholder {
  width: 100%;
  height: 300px;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-bg));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}

.detail-cover-placeholder p {
  margin-top: var(--spacing-md);
  color: var(--color-secondary);
  font-size: var(--font-size-sm);
}

.detail-category {
  display: inline-block;
  padding: 4px 12px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: 20px;
  font-size: var(--font-size-xs);
  font-weight: 500;
  margin-bottom: var(--spacing-sm);
}

.detail-title {
  font-size: var(--font-size-3xl);
  margin-bottom: var(--spacing-sm);
}

.detail-summary {
  color: var(--color-secondary);
  line-height: 1.8;
  margin-bottom: var(--spacing-md);
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: var(--spacing-md);
}

.detail-status-row {
  margin-bottom: var(--spacing-md);
}

.detail-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-sm);
  padding: 4px 14px;
  border-radius: 20px;
  background: var(--color-bg);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.detail-status.in_progress .status-dot { background: #6CB4EE; }
.detail-status.completed .status-dot { background: var(--color-primary); }

.detail-meta {
  display: flex;
  gap: var(--spacing-xl);
  padding: var(--spacing-md) 0;
  border-top: 1px solid var(--color-border-light);
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: var(--spacing-lg);
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.meta-label {
  font-size: var(--font-size-xs);
  color: var(--color-secondary);
}

.meta-value {
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.detail-actions {
  display: flex;
  gap: var(--spacing-sm);
}

/* 标签页 */
.detail-tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: var(--spacing-xl);
  overflow-x: auto;
}

.tab-btn {
  padding: 12px 20px;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: var(--font-size-md);
  color: var(--color-secondary);
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  color: var(--color-primary);
}

.tab-btn.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.content-block {
  margin-bottom: var(--spacing-xl);
}

.content-block h3 {
  font-size: var(--font-size-xl);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--color-primary-light);
}

.content-block p {
  font-size: var(--font-size-md);
  line-height: 1.9;
  color: var(--color-body);
}

.goal-list li,
.result-list li {
  padding: 8px 0;
  font-size: var(--font-size-md);
  color: var(--color-body);
}

.result-list li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.feature-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: 600;
  flex-shrink: 0;
}

.arch-diagram {
  padding: var(--spacing-lg);
  background: var(--color-bg);
  border-radius: var(--radius-md);
  border: 1px dashed var(--color-border);
}

.arch-diagram p {
  font-family: 'Courier New', monospace;
  font-size: var(--font-size-sm);
}

.tech-stack-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-item {
  padding: 8px 20px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.challenge-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.challenge-item {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}

.challenge-problem,
.challenge-solution {
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
}

.challenge-problem {
  background: #FFF5F5;
  border: 1px solid #FFE0E0;
}

.challenge-solution {
  background: #F0FFF4;
  border: 1px solid #D0F0D8;
}

.challenge-label {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  margin-bottom: var(--spacing-xs);
  background: #FFE0E0;
  color: #E53E3E;
}

.challenge-label.solution {
  background: #D0F0D8;
  color: #2F855A;
}

.challenge-problem p,
.challenge-solution p {
  font-size: var(--font-size-sm);
  line-height: 1.7;
}

.review-text {
  font-size: var(--font-size-md);
  line-height: 2;
}

@media (max-width: 768px) {
  .detail-header-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }

  .detail-cover-placeholder {
    height: 200px;
  }

  .detail-cover {
    height: 200px;
  }

  .detail-meta {
    flex-wrap: wrap;
    gap: var(--spacing-md);
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .challenge-item {
    grid-template-columns: 1fr;
  }
}
</style>
