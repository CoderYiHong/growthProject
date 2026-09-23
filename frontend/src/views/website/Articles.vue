<script setup>
import { ref, computed, onMounted } from 'vue'
import { getArticles } from '@/api/public'
import { transformArticle } from '@/utils'
import ArticleCard from '@/components/common/ArticleCard.vue'

const articles = ref([])
const searchKeyword = ref('')
const activeCategory = ref('all')
const activeTag = ref('')
const currentPage = ref(1)
const pageSize = 6
const loading = ref(true)

const articleCategories = computed(() => {
  const cats = [...new Set(articles.value.map(a => a.category).filter(Boolean))]
  return [{ value: 'all', label: '全部' }, ...cats.map(c => ({ value: c, label: c }))]
})

const articleTags = computed(() =>
  [...new Set(articles.value.flatMap(a => a.tags || []).filter(Boolean))]
)

onMounted(async () => {
  try {
    const res = await getArticles()
    articles.value = res.code === 200 && Array.isArray(res.data)
      ? res.data.map(transformArticle)
      : []
  } catch (e) {
    articles.value = []
    console.error('获取 CSDN 文章列表失败:', e.message)
  } finally {
    loading.value = false
  }
})

const filteredArticles = computed(() => {
  let result = articles.value.filter(a => a.status === 'published')

  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(a =>
      a.title.toLowerCase().includes(kw) ||
      a.summary.toLowerCase().includes(kw) ||
      a.tags.some(t => t.toLowerCase().includes(kw))
    )
  }

  if (activeCategory.value !== 'all') {
    result = result.filter(a => a.category === activeCategory.value)
  }

  if (activeTag.value) {
    result = result.filter(a => a.tags.includes(activeTag.value))
  }

  result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  return result
})

const totalPages = computed(() => Math.ceil(filteredArticles.value.length / pageSize))
const pagedArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredArticles.value.slice(start, start + pageSize)
})

function handlePageChange(page) {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleCategoryChange(cat) {
  activeCategory.value = cat
  currentPage.value = 1
}
</script>

<template>
  <div class="articles-page">
    <!-- 页面标题区域 — 浅色简洁横幅 -->
    <div class="page-hero">
      <div class="container">
        <h1 class="page-title">技术文章</h1>
        <p class="page-desc">持续写作，用文字记录技术成长</p>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="page-body">
      <div class="container">
        <!-- 工具栏：搜索 + 分类 -->
        <div class="toolbar">
          <div class="toolbar-search">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索文章..."
              class="search-input"
            />
          </div>

          <div class="toolbar-cats">
            <button
              v-for="cat in articleCategories"
              :key="cat.value"
              class="cat-btn"
              :class="{ active: activeCategory === cat.value }"
              @click="handleCategoryChange(cat.value)"
            >{{ cat.label }}</button>
          </div>
        </div>

        <!-- 标签筛选（有标签时显示） -->
        <div v-if="articleTags.length > 0" class="tag-row">
          <button
            class="tbtn"
            :class="{ active: activeTag === '' }"
            @click="activeTag = ''; currentPage = 1"
          >全部</button>
          <button
            v-for="tag in articleTags"
            :key="tag"
            class="tbtn"
            :class="{ active: activeTag === tag }"
            @click="activeTag = activeTag === tag ? '' : tag; currentPage = 1"
          >{{ tag }}</button>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="state-box">
          <p>加载中...</p>
        </div>

        <!-- 文章列表（有数据） -->
        <div v-else-if="pagedArticles.length > 0" class="articles-grid">
          <ArticleCard
            v-for="article in pagedArticles"
            :key="article.id"
            :article="article"
          />
        </div>

        <!-- 空状态（无数据） -->
        <div v-else class="state-box">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--color-muted)" stroke-width="1.5">
            <path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>
          </svg>
          <p>暂时还没有文章，正在认真积累中。</p>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination-row">
          <el-pagination
            :current-page="currentPage"
            :total="filteredArticles.length"
            :page-size="pageSize"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ---- 页面标题 ---- */
.page-hero {
  padding: 40px 0 28px;
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border-light);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-title);
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: var(--color-secondary);
}

/* ---- 主体 ---- */
.page-body {
  padding: 32px 0 64px;
  background: var(--color-bg-white);
  min-height: 50vh;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ---- 工具栏 ---- */
.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.toolbar-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: #fff;
  color: var(--color-muted);
  flex: 1;
  max-width: 320px;
}

.search-input {
  border: none;
  outline: none;
  font-size: 14px;
  color: var(--color-body);
  background: transparent;
  width: 100%;
}

.search-input::placeholder {
  color: var(--color-muted);
}

.toolbar-cats {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.cat-btn {
  padding: 5px 14px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 13px;
  color: var(--color-secondary);
  background: transparent;
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
}

.cat-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.cat-btn.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }

/* ---- 标签行 ---- */
.tag-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.tbtn {
  padding: 3px 10px;
  border: 1px solid var(--color-border-light);
  border-radius: 4px;
  font-size: 12px;
  color: var(--color-secondary);
  background: transparent;
  cursor: pointer;
  transition: all 0.15s;
}

.tbtn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.tbtn.active { background: var(--color-primary-light); color: var(--color-primary); border-color: var(--color-primary-light); }

/* ---- 文章网格 ---- */
.articles-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

/* ---- 状态 ---- */
.state-box {
  text-align: center;
  padding: 80px 24px;
  color: var(--color-secondary);
  font-size: 14px;
}

.state-box svg {
  margin-bottom: 16px;
  display: block;
  margin-left: auto;
  margin-right: auto;
}

/* ---- 分页 ---- */
.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 768px) {
  .page-hero { padding: 28px 0 20px; }
  .page-title { font-size: 24px; }
  .articles-grid { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar-search { max-width: none; }
  .container { padding: 0 16px; }
}
</style>

<!-- 非 scoped：覆盖全局透明背景规则，文章页使用纯色背景 + flex 填满 -->
<style>
.articles-page {
  background: var(--color-bg-white) !important;
  display: flex !important;
  flex-direction: column;
  flex: 1;
}
.articles-page .page-body {
  flex: 1;
}
</style>
