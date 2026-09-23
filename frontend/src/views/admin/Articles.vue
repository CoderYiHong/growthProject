<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  getAdminArticles, updateArticle, deleteArticle,
  syncCsdnArticles, uploadFile
} from '@/api/admin'
import { transformArticle, formatDate } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const filterVisible = ref('')
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)

const articleList = ref([])
const loading = ref(false)

const articleCategories = [
  { value: 'frontend', label: '前端开发' },
  { value: 'backend', label: '后端开发' },
  { value: 'tools', label: '工具效率' },
  { value: 'mobile', label: '移动开发' },
  { value: 'ai', label: 'AI 探索' },
  { value: 'growth', label: '成长笔记' }
]

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getAdminArticles({
      page: currentPage.value,
      pageSize,
      keyword: searchKeyword.value || undefined,
      category: filterCategory.value || undefined,
      status: filterStatus.value || undefined,
      visible: filterVisible.value === '' ? undefined : filterVisible.value
    })
    if (res.code === 200 && res.data) {
      articleList.value = (res.data.records || []).map(transformArticle)
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('获取文章列表失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchArticles)

const pagedList = computed(() => articleList.value)

function handleFilterChange() {
  currentPage.value = 1
  fetchArticles()
}

const syncingCsdn = ref(false)

async function handleSyncCsdn() {
  syncingCsdn.value = true
  try {
    const res = await syncCsdnArticles()
    if (res.code !== 200) throw new Error(res.message || '同步失败')
    const { total = 0, created = 0, updated = 0 } = res.data || {}
    ElMessage.success(`同步完成：读取 ${total} 篇，新增 ${created} 篇，更新 ${updated} 篇`)
    currentPage.value = 1
    await fetchArticles()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || 'CSDN 同步失败')
  } finally {
    syncingCsdn.value = false
  }
}

// 编辑弹窗
const dialogVisible = ref(false)
const editingArticle = ref(null)
const form = ref({})
const coverUploading = ref(false)

function openDialog(article = null) {
  if (!article) return
  editingArticle.value = article
  form.value = { ...article }
  dialogVisible.value = true
}

const coverPreview = computed(() => form.value.cover || form.value.sourceCover || '')

async function handleCoverUpload({ file }) {
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('封面图片不能超过 5MB')
    return
  }
  if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)) {
    ElMessage.error('请选择 JPG、PNG、GIF 或 WebP 图片')
    return
  }

  coverUploading.value = true
  try {
    const res = await uploadFile(file)
    if (res.code !== 200 || !res.data?.url) {
      throw new Error(res.message || '上传失败')
    }
    form.value.cover = res.data.url
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

function removeCover() {
  form.value.cover = ''
}

async function handleSave() {
  if (!form.value.title) { ElMessage.warning('请输入文章标题'); return }
  try {
    const {
      effectiveCover, coverSource, categoryLabel, statusLabel, createdAt,
      ...editable
    } = form.value
    const data = {
      ...editable,
      tags: JSON.stringify(form.value.tags || [])
    }
    await updateArticle(editingArticle.value.id, data)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    fetchArticles()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(article) {
  try {
    await ElMessageBox.confirm(`确定要删除「${article.title}」吗？`, '确认删除', { type: 'warning' })
    await deleteArticle(article.id)
    ElMessage.success('删除成功')
    fetchArticles()
  } catch (e) {
    // 取消或失败
  }
}

const recommendLoading = ref({}) // articleId → boolean，防重复点击
const visibleLoading = ref({})

async function toggleRecommend(article) {
  const newVal = !article.isRecommended
  // 乐观更新
  article.isRecommended = newVal
  recommendLoading.value[article.id] = true
  try {
    await updateArticle(article.id, { isRecommended: newVal })
    ElMessage.success(newVal ? '已设为推荐' : '已取消推荐')
  } catch (e) {
    // 失败回滚
    article.isRecommended = !newVal
    ElMessage.error('操作失败，请重试')
  } finally {
    recommendLoading.value[article.id] = false
  }
}

async function toggleVisible(article) {
  const oldValue = article.visible === 1 ? 1 : 0
  const newValue = oldValue === 1 ? 0 : 1
  article.visible = newValue
  visibleLoading.value[article.id] = true
  try {
    const res = await updateArticle(article.id, { visible: newValue })
    if (res.code !== 200) throw new Error(res.message || '更新失败')
    ElMessage.success(newValue === 1 ? '已在前台展示' : '已从前台隐藏')
  } catch (e) {
    article.visible = oldValue
    ElMessage.error(e.response?.data?.message || e.message || '展示状态更新失败')
  } finally {
    visibleLoading.value[article.id] = false
  }
}

async function togglePublish(article) {
  try {
    const newStatus = article.status === 'published' ? 'draft' : 'published'
    await updateArticle(article.id, { status: newStatus })
    ElMessage.success(newStatus === 'published' ? '已发布' : '已取消发布')
    fetchArticles()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>

<template>
  <div class="admin-articles">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" plain :loading="syncingCsdn" @click="handleSyncCsdn">
          {{ syncingCsdn ? '正在同步...' : '同步 CSDN' }}
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input v-model="searchKeyword" placeholder="搜索文章..." clearable style="width: 200px" @keyup.enter="handleFilterChange" @clear="handleFilterChange" />
        <el-select v-model="filterCategory" placeholder="分类筛选" clearable style="width: 140px" @change="handleFilterChange">
          <el-option v-for="c in articleCategories.filter(c => c.value !== 'all')" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 120px" @change="handleFilterChange">
          <el-option label="已发布" value="published" />
          <el-option label="草稿" value="draft" />
        </el-select>
        <el-select v-model="filterVisible" placeholder="展示筛选" clearable style="width: 120px" @change="handleFilterChange">
          <el-option label="前台展示" :value="1" />
          <el-option label="前台隐藏" :value="0" />
        </el-select>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="pagedList" style="width: 100%" stripe>
        <el-table-column label="封面" width="84">
          <template #default="{ row }">
            <div v-if="row.effectiveCover" class="cover-thumb-wrap">
              <img :src="row.effectiveCover" :alt="`${row.title}封面`" class="cover-thumb" />
              <span class="cover-origin">{{ row.cover ? '自定义' : 'CSDN' }}</span>
            </div>
            <div v-else class="cover-thumb cover-thumb-empty">无</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="来源" width="82">
          <template #default="{ row }">
            <el-tag type="danger" size="small">CSDN</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryLabel" label="分类" width="100" />
        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <el-tag v-for="t in row.tags.slice(0, 3)" :key="t" size="small" style="margin-right:4px">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="阅读" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'" size="small">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="70">
          <template #default="{ row }">
            <el-switch :model-value="row.isRecommended" size="small" :loading="recommendLoading[row.id]" @change="toggleRecommend(row)" />
          </template>
        </el-table-column>
        <el-table-column label="前台展示" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.visible === 1"
              size="small"
              :loading="visibleLoading[row.id]"
              @change="toggleVisible(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="日期" width="110">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" @click="togglePublish(row)">
              {{ row.status === 'published' ? '取消发布' : '发布' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;display:flex;justify-content:flex-end;">
        <el-pagination v-model:current-page="currentPage" :total="total" :page-size="pageSize" layout="total, prev, pager, next" background @current-change="fetchArticles" />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑 CSDN 文章展示" width="640px">
      <el-form :model="form" label-position="top">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in articleCategories.filter(c => c.value !== 'all')" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item v-if="form.sourceUrl" label="CSDN 原文">
          <a :href="form.sourceUrl" target="_blank" rel="noopener noreferrer" class="source-link">
            {{ form.sourceUrl }}
          </a>
        </el-form-item>
        <el-form-item label="文章封面">
          <div class="cover-editor">
            <div class="cover-preview">
              <img v-if="coverPreview" :src="coverPreview" alt="文章封面预览" />
              <div v-else class="cover-placeholder">
                <span class="cover-placeholder-icon">＋</span>
                <span>建议上传 16:9 横图</span>
              </div>
            </div>
            <div class="cover-actions">
              <el-upload
                accept=".jpg,.jpeg,.png,.gif,.webp"
                :show-file-list="false"
                :http-request="handleCoverUpload"
                :disabled="coverUploading"
              >
                <el-button type="success" plain :loading="coverUploading">
                  {{ form.cover ? '更换自定义封面' : '上传自定义封面' }}
                </el-button>
              </el-upload>
              <el-button v-if="form.cover" type="danger" plain @click="removeCover">恢复 CSDN 封面</el-button>
              <span class="cover-hint">
                当前：{{ form.cover ? '自定义封面' : (form.sourceCover ? 'CSDN 原文封面' : '默认占位图') }}。
                支持 JPG、PNG、GIF、WebP，最大 5MB。
              </span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入文章正文（支持 Markdown）" />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="form.status">
            <el-radio value="published">发布</el-radio>
            <el-radio value="draft">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设为推荐">
          <el-switch v-model="form.isRecommended" />
        </el-form-item>
        <el-form-item label="在前台展示">
          <el-switch v-model="form.visible" :active-value="1" :inactive-value="0" />
          <span class="visible-hint">关闭后文章仍保留在后台，但访客无法访问</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-md); flex-wrap: wrap; gap: var(--spacing-sm); }
.toolbar-left, .toolbar-right { display: flex; align-items: center; gap: var(--spacing-sm); }
.table-card { background: #fff; border-radius: var(--radius-md); padding: var(--spacing-md); box-shadow: var(--shadow-sm); }
.cover-thumb { display: block; width: 56px; height: 38px; object-fit: cover; border-radius: 6px; border: 1px solid var(--color-border-light); background: var(--color-bg); }
.cover-thumb-wrap { position: relative; width: 56px; }
.cover-origin { position: absolute; right: 2px; bottom: 2px; padding: 0 3px; border-radius: 3px; background: rgba(0, 0, 0, .58); color: #fff; font-size: 9px; line-height: 15px; }
.cover-thumb-empty { display: flex; align-items: center; justify-content: center; color: var(--color-muted); font-size: 12px; }
.cover-editor { width: 100%; display: flex; gap: 16px; align-items: flex-start; }
.cover-preview { width: 240px; aspect-ratio: 16 / 9; overflow: hidden; flex-shrink: 0; border: 1px dashed var(--color-border); border-radius: var(--radius-sm); background: var(--color-bg); }
.cover-preview img { display: block; width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; color: var(--color-muted); font-size: 12px; }
.cover-placeholder-icon { font-size: 26px; line-height: 1; color: var(--color-primary); }
.cover-actions { display: flex; align-items: flex-start; gap: 8px; flex-wrap: wrap; padding-top: 4px; }
.cover-hint { flex-basis: 100%; color: var(--color-muted); font-size: 12px; line-height: 1.5; }
.source-link { color: var(--color-primary); word-break: break-all; }
.visible-hint { margin-left: 10px; color: var(--color-muted); font-size: 12px; }
@media (max-width: 768px) {
  .toolbar { flex-direction: column; align-items: flex-start; }
  .toolbar-right { flex-wrap: wrap; }
  .toolbar-right .el-input, .toolbar-right .el-select { width: 100% !important; }
  .cover-editor { flex-direction: column; }
  .cover-preview { width: 100%; }
}
</style>
