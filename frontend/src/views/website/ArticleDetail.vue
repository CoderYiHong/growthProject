<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleDetail, getArticles } from '@/api/public'
import { formatDate, formatViews, transformArticle } from '@/utils'

const route = useRoute()
const router = useRouter()

const article = ref(null)
const allArticles = ref([])

const prevArticle = computed(() => {
  if (!article.value || !allArticles.value.length) return null
  const idx = allArticles.value.findIndex(a => a.id === article.value.id)
  return idx > 0 ? allArticles.value[idx - 1] : null
})

const nextArticle = computed(() => {
  if (!article.value || !allArticles.value.length) return null
  const idx = allArticles.value.findIndex(a => a.id === article.value.id)
  return idx < allArticles.value.length - 1 ? allArticles.value[idx + 1] : null
})

onMounted(async () => {
  try {
    const [detailRes, listRes] = await Promise.all([
      getArticleDetail(route.params.id),
      getArticles()
    ])
    if (detailRes.code === 200 && detailRes.data) {
      article.value = transformArticle(detailRes.data)
    } else {
      router.replace('/articles')
      return
    }
    if (listRes.code === 200) {
      allArticles.value = (listRes.data || []).map(transformArticle)
    }
  } catch (e) {
    console.error('获取文章详情失败', e)
    router.replace('/articles')
  }
})

// HTML 转义，防止 XSS
function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// 简单 Markdown 渲染（将 ## 转为标题，``` 转为代码块，普通文本为段落）
const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  const lines = article.value.content.split('\n')
  let html = ''
  let inCode = false
  let headingIndex = 0

  for (const line of lines) {
    if (line.startsWith('```')) {
      inCode = !inCode
      html += inCode ? '<div class="md-code-block"><pre>' : '</pre></div>'
      continue
    }
    if (inCode) {
      html += escapeHtml(line) + '\n'
      continue
    }
    if (line.startsWith('## ')) {
      html += `<h2 class="md-h2" id="h-${headingIndex++}">${escapeHtml(line.slice(3))}</h2>`
    } else if (line.startsWith('### ')) {
      html += `<h3 class="md-h3" id="h-${headingIndex++}">${escapeHtml(line.slice(4))}</h3>`
    } else if (line.startsWith('- ')) {
      html += `<li class="md-li">${escapeHtml(line.slice(2))}</li>`
    } else if (line.trim()) {
      html += `<p class="md-p">${escapeHtml(line)}</p>`
    }
  }
  return html
})

// 目录
const toc = computed(() => {
  if (!article.value?.content) return []
  const headings = []
  const lines = article.value.content.split('\n')
  let idx = 0
  for (const line of lines) {
    if (line.startsWith('## ')) headings.push({ level: 2, text: line.slice(3), id: `h-${idx++}` })
    else if (line.startsWith('### ')) headings.push({ level: 3, text: line.slice(4), id: `h-${idx++}` })
  }
  return headings
})

function scrollToHeading(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const showScrollTop = ref(false)
function onScroll() { showScrollTop.value = window.scrollY > 300 }
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
</script>

<template>
  <div class="article-detail-page" v-if="article">
    <!-- 头部 -->
    <section class="article-hero">
      <div class="container">
        <div class="breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="separator">/</span>
          <router-link to="/articles">技术文章</router-link>
          <span class="separator">/</span>
          <span>{{ article.title }}</span>
        </div>

        <h1 class="article-title">{{ article.title }}</h1>

        <div class="article-meta-row">
          <span class="article-author">{{ article.author }}</span>
          <span class="meta-dot">·</span>
          <span>{{ formatDate(article.createdAt) }}</span>
          <span class="meta-dot">·</span>
          <span>{{ formatViews(article.views) }} 阅读</span>
          <span class="meta-dot">·</span>
          <span>{{ article.readTime }}</span>
        </div>

        <a
          v-if="article.sourceUrl"
          :href="article.sourceUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="article-source-link"
        >
          在 CSDN 查看完整原文 →
        </a>

        <div class="article-tags-row">
          <span v-for="tag in article.tags" :key="tag" class="tag">{{ tag }}</span>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="article-layout">
          <!-- 目录 -->
          <aside class="article-toc" v-if="toc.length > 0">
            <h4>文章目录</h4>
            <nav>
              <a
                v-for="(h, i) in toc"
                :key="i"
                :class="{ 'toc-h2': h.level === 2, 'toc-h3': h.level === 3 }"
                :href="`#${h.id}`"
                @click.prevent="scrollToHeading(h.id)"
              >{{ h.text }}</a>
            </nav>
          </aside>

          <!-- 正文 -->
          <div class="article-body">
            <div class="markdown-content" v-html="renderedContent"></div>

            <!-- 上一篇/下一篇 -->
            <div class="article-nav">
              <div v-if="prevArticle" class="article-nav-item prev">
                <span class="nav-label">上一篇</span>
                <router-link :to="`/articles/${prevArticle.id}`">{{ prevArticle.title }}</router-link>
              </div>
              <div v-if="nextArticle" class="article-nav-item next">
                <span class="nav-label">下一篇</span>
                <router-link :to="`/articles/${nextArticle.id}`">{{ nextArticle.title }}</router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 返回顶部 -->
    <Transition name="fade">
      <button v-if="showScrollTop" class="scroll-to-top" @click="window.scrollTo({ top: 0, behavior: 'smooth' })">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg>
      </button>
    </Transition>
  </div>
</template>

<style scoped>
.article-hero {
  padding: var(--spacing-2xl) 0 var(--spacing-lg);
  background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-bg) 100%);
}

.article-title {
  font-size: var(--font-size-4xl);
  margin: var(--spacing-md) 0;
  line-height: 1.3;
}

.article-source-link {
  display: inline-flex;
  margin-top: var(--spacing-sm);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
  text-decoration: none;
}

.article-source-link:hover {
  text-decoration: underline;
}

.article-meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
  margin-bottom: var(--spacing-md);
}

.article-author {
  color: var(--color-primary);
  font-weight: 500;
}

.meta-dot {
  color: var(--color-border);
}

.article-tags-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 布局 */
.article-layout {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: var(--spacing-2xl);
}

/* 目录 */
.article-toc {
  position: sticky;
  top: calc(var(--header-height) + 24px);
  align-self: start;
}

.article-toc h4 {
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-md);
}

.article-toc nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
  border-left: 2px solid var(--color-border-light);
  padding-left: var(--spacing-sm);
}

.article-toc a {
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
  line-height: 1.5;
}

.article-toc a:hover {
  color: var(--color-primary);
}

.article-toc a.toc-h3 {
  padding-left: 12px;
  font-size: var(--font-size-xs);
}

/* 正文 */
.article-body {
  background: var(--color-bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-2xl);
  border: 1px solid var(--color-border-light);
}

/* Markdown 样式 */
.markdown-content :deep(.md-h2) {
  font-size: var(--font-size-2xl);
  margin: var(--spacing-xl) 0 var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--color-primary-light);
}

.markdown-content :deep(.md-h3) {
  font-size: var(--font-size-xl);
  margin: var(--spacing-lg) 0 var(--spacing-sm);
}

.markdown-content :deep(.md-p) {
  font-size: var(--font-size-md);
  line-height: 1.9;
  color: var(--color-body);
  margin-bottom: var(--spacing-md);
}

.markdown-content :deep(.md-li) {
  font-size: var(--font-size-md);
  line-height: 1.8;
  color: var(--color-body);
  padding-left: var(--spacing-md);
  position: relative;
}

.markdown-content :deep(.md-li::before) {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--color-primary);
}

.markdown-content :deep(.md-code-block) {
  background: #1e1e1e;
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  margin: var(--spacing-md) 0;
  overflow-x: auto;
}

.markdown-content :deep(.md-code-block pre) {
  color: #d4d4d4;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: var(--font-size-sm);
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
}

/* 上下篇 */
.article-nav {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
  margin-top: var(--spacing-2xl);
  padding-top: var(--spacing-xl);
  border-top: 1px solid var(--color-border-light);
}

.article-nav-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.article-nav-item.next {
  text-align: right;
}

.nav-label {
  font-size: var(--font-size-xs);
  color: var(--color-muted);
}

.article-nav-item a {
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.scroll-to-top {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
  z-index: 999;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .article-layout {
    grid-template-columns: 1fr;
  }

  .article-toc {
    display: none;
  }

  .article-body {
    padding: var(--spacing-md);
  }

  .article-title {
    font-size: var(--font-size-2xl);
  }

  .article-nav {
    grid-template-columns: 1fr;
  }
}
</style>
