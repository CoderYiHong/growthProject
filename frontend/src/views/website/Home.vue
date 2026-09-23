<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProjects, getArticles, getGrowthStages } from '@/api/public'
import { transformProject, transformArticle } from '@/utils'
import { learningPlans, weeklySummary, monthlySummary } from '@/data/learningPlans'
import { growthRecords } from '@/data/growthRecords'
import { ElMessage } from 'element-plus'
import HeroSection from '@/components/home/HeroSection.vue'
import LearningPlanSection from '@/components/home/LearningPlanSection.vue'
import RecentActivity from '@/components/home/RecentActivity.vue'
import WeeklySummary from '@/components/home/WeeklySummary.vue'
import ProjectCard from '@/components/common/ProjectCard.vue'
import ArticleCard from '@/components/common/ArticleCard.vue'

const router = useRouter()

/* ---- Mock 数据（本地） ---- */
const plans = learningPlans
const records = growthRecords
const summaryTab = ref('week')

/* ---- API 数据 ---- */
const projects = ref([])
const articles = ref([])
const growthStages = ref([])
const loading = ref(true)

const featuredProjects = ref([])
const recentArticles = ref([])

onMounted(async () => {
  const [projectResult, articleResult, growthResult] = await Promise.allSettled([
    getProjects(),
    getArticles(),
    getGrowthStages()
  ])

  if (projectResult.status === 'fulfilled' && projectResult.value.code === 200) {
    projects.value = (projectResult.value.data || []).map(transformProject)
  } else {
    ElMessage.warning('项目数据加载失败，请稍后刷新重试')
  }

  if (articleResult.status === 'fulfilled' && articleResult.value.code === 200) {
    articles.value = (articleResult.value.data || []).map(transformArticle)
  } else {
    ElMessage.warning('文章数据加载失败，请稍后刷新重试')
  }

  if (growthResult.status === 'fulfilled' && growthResult.value.code === 200) {
    growthStages.value = growthResult.value.data || []
  } else {
    ElMessage.warning('成长轨迹加载失败，请稍后刷新重试')
  }

  try {
    featuredProjects.value = projects.value.filter(p => p.isRecommended).slice(0, 4)
    recentArticles.value = articles.value.filter(a => a.status === 'published').slice(0, 3)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <!-- 1. Hero -->
    <HeroSection />

    <!-- 2. 当前成长计划 -->
    <LearningPlanSection :plans="plans" />

    <!-- 3. 最近成长记录 -->
    <RecentActivity :records="records" />

    <!-- 4. 精选项目 -->
    <section v-if="featuredProjects.length > 0" class="section">
      <div class="container-wide">
        <div class="section-head">
          <div>
            <h2 class="section-title">项目作品</h2>
            <p class="section-sub">每一个项目，都解决一个真实的问题</p>
          </div>
          <button class="link-btn" @click="router.push('/projects')">查看全部 →</button>
        </div>

        <div class="projects-grid">
          <ProjectCard
            v-for="project in featuredProjects"
            :key="project.id"
            :project="project"
          />
        </div>
      </div>
    </section>

    <!-- 5. 技术文章 -->
    <section v-if="recentArticles.length > 0" class="section bg-tint">
      <div class="container-wide">
        <div class="section-head">
          <div>
            <h2 class="section-title">技术文章</h2>
            <p class="section-sub">持续写作，用文字记录技术成长</p>
          </div>
          <button class="link-btn" @click="router.push('/articles')">查看全部 →</button>
        </div>

        <div class="articles-grid">
          <ArticleCard
            v-for="article in recentArticles"
            :key="article.id"
            :article="article"
          />
        </div>
      </div>
    </section>

    <!-- 6. 成长轨迹预览 -->
    <section v-if="growthStages.length > 0" class="section">
      <div class="container-wide">
        <div class="section-head">
          <div>
            <h2 class="section-title">成长轨迹</h2>
            <p class="section-sub">每一步，都是成长的印记</p>
          </div>
          <button class="link-btn" @click="router.push('/growth')">查看全部 →</button>
        </div>

        <div class="stages-row">
          <div v-for="stage in growthStages" :key="stage.id" class="stage-chip">
            <span class="stage-period">{{ stage.period }}</span>
            <span class="stage-title-sm">{{ stage.title }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 7. 阶段总结 -->
    <WeeklySummary
      v-model:tab="summaryTab"
      :weekly="weeklySummary"
      :monthly="monthlySummary"
    />

    <!-- 8. 加载状态 -->
    <section v-if="loading" class="section">
      <div class="container-wide" style="text-align:center;padding:48px;color:var(--color-muted)">
        加载中...
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  /* 整体容器 */
}

/* 分区标题 */
.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 8px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-title);
  margin-bottom: 4px;
}

.section-sub {
  font-size: 14px;
  color: var(--color-secondary);
}

.link-btn {
  padding: 6px 14px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 13px;
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  transition: all 0.15s ease;
}

.link-btn:hover {
  background: var(--color-primary-light);
  border-color: var(--color-primary);
}

/* 项目网格 */
.projects-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

/* 文章网格 */
.articles-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

/* 成长轨迹预览 */
.stages-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.stage-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: rgba(255,255,255,0.7);
}

.stage-period {
  font-size: 12px;
  color: var(--color-muted);
  white-space: nowrap;
}

.stage-title-sm {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-body);
}

/* 浅色背景 */
.bg-tint {
  background: rgba(247, 249, 246, 0.3);
}

@media (max-width: 1200px) {
  .projects-grid { grid-template-columns: repeat(2, 1fr); }
  .articles-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .projects-grid,
  .articles-grid { grid-template-columns: 1fr; }
  .stages-row { flex-direction: column; }
}
</style>
