<script setup>
import { ref, computed, onMounted } from 'vue'
import { getProjects } from '@/api/public'
import { transformProject } from '@/utils'
import ProjectCard from '@/components/common/ProjectCard.vue'

const projects = ref([])
const searchKeyword = ref('')
const activeCategory = ref('all')
const activeTech = ref('')
const sortBy = ref('newest')
const currentPage = ref(1)
const pageSize = 8
const loading = ref(true)

const sortOptions = [
  { value: 'newest', label: '最新发布' },
  { value: 'oldest', label: '最早发布' },
  { value: 'name', label: '名称排序' }
]

// 从数据中动态提取分类和标签
const projectCategories = computed(() => {
  const cats = new Map(projects.value
    .filter(p => p.category)
    .map(p => [p.category, p.categoryLabel || p.category]))
  return [
    { value: 'all', label: '全部' },
    ...[...cats].map(([value, label]) => ({ value, label }))
  ]
})
const techFilterOptions = computed(() =>
  [...new Set(projects.value.flatMap(p => p.techStack).filter(Boolean))]
)

onMounted(async () => {
  try {
    const res = await getProjects()
    projects.value = res.code === 200 && Array.isArray(res.data)
      ? res.data.map(transformProject)
      : []
  } catch (e) {
    projects.value = []
    console.error('获取 GitHub 项目列表失败:', e.message)
  } finally {
    loading.value = false
  }
})

// 筛选和排序
const filteredProjects = computed(() => {
  let result = [...projects.value]

  // 搜索
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(p =>
      p.name.toLowerCase().includes(kw) ||
      p.summary.toLowerCase().includes(kw) ||
      p.techStack.some(t => t.toLowerCase().includes(kw))
    )
  }

  // 分类
  if (activeCategory.value !== 'all') {
    result = result.filter(p => p.category === activeCategory.value)
  }

  // 技术栈
  if (activeTech.value) {
    result = result.filter(p => p.techStack.includes(activeTech.value))
  }

  // 排序
  if (sortBy.value === 'newest') {
    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } else if (sortBy.value === 'oldest') {
    result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
  } else {
    result.sort((a, b) => a.name.localeCompare(b.name))
  }

  return result
})

const totalPages = computed(() => Math.ceil(filteredProjects.value.length / pageSize))
const pagedProjects = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredProjects.value.slice(start, start + pageSize)
})

function handleCategoryChange(cat) {
  activeCategory.value = cat
  currentPage.value = 1
}

function handlePageChange(page) {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <div class="projects-page">
    <section class="section">
      <div class="container-wide">
        <!-- 工具栏 -->
        <div class="projects-toolbar">
          <div class="toolbar-info">
            共 <strong>{{ filteredProjects.length }}</strong> 个项目
          </div>

          <div class="toolbar-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索项目..."
              :prefix-icon="null"
              clearable
              class="search-input"
            >
              <template #prefix>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              </template>
            </el-input>

            <el-select v-model="sortBy" class="sort-select">
              <el-option v-for="opt in sortOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>
        </div>

        <!-- 分类筛选 -->
        <div class="category-filter">
          <button
            v-for="cat in projectCategories"
            :key="cat.value"
            class="filter-btn"
            :class="{ active: activeCategory === cat.value }"
            @click="handleCategoryChange(cat.value)"
          >
            {{ cat.label }}
          </button>
        </div>

        <!-- 技术栈筛选 -->
        <div class="tech-filter">
          <span class="filter-label">技术栈：</span>
          <button
            class="filter-btn-sm"
            :class="{ active: activeTech === '' }"
            @click="activeTech = ''"
          >全部</button>
          <button
            v-for="tech in techFilterOptions"
            :key="tech"
            class="filter-btn-sm"
            :class="{ active: activeTech === tech }"
            @click="activeTech = activeTech === tech ? '' : tech"
          >{{ tech }}</button>
        </div>

        <!-- 项目列表 -->
        <div v-if="loading" class="empty-state">
          <p>加载中...</p>
        </div>
        <div v-else-if="pagedProjects.length > 0" class="projects-grid">
          <ProjectCard
            v-for="project in pagedProjects"
            :key="project.id"
            :project="project"
          />
        </div>
        <div v-else class="empty-state">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--color-border)" stroke-width="1.5">
            <rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
          <p>暂时还没有公开项目，请在后台同步 GitHub 并选择要展示的仓库。</p>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination-wrapper">
          <el-pagination
            :current-page="currentPage"
            :total="filteredProjects.length"
            :page-size="pageSize"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* 工具栏 */
.projects-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.toolbar-info {
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
}

.toolbar-info strong {
  color: var(--color-primary);
  font-size: var(--font-size-xl);
}

.toolbar-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.search-input {
  width: 240px;
}

.sort-select {
  width: 130px;
}

/* 分类筛选 */
.category-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: var(--spacing-md);
}

.filter-btn {
  padding: 6px 16px;
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  color: var(--color-body);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.filter-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.filter-btn.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

/* 技术筛选 */
.tech-filter {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.filter-label {
  font-size: var(--font-size-xs);
  color: var(--color-secondary);
  margin-right: 4px;
}

.filter-btn-sm {
  padding: 3px 10px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 11px;
  color: var(--color-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.filter-btn-sm:hover {
  color: var(--color-primary);
}

.filter-btn-sm.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-color: var(--color-primary-light);
}

/* 项目网格 */
.projects-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--color-secondary);
}

.empty-state p {
  margin-top: var(--spacing-md);
}

@media (max-width: 1024px) {
  .projects-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .projects-grid {
    grid-template-columns: 1fr;
  }

  .projects-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-actions {
    width: 100%;
  }

  .search-input {
    flex: 1;
    width: auto;
  }
}
</style>
