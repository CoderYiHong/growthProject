<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  getAdminProjects,
  syncGithubProjects,
  updateProject,
  uploadFile
} from '@/api/admin'
import { formatDate, transformProject } from '@/utils'
import { ElMessage } from 'element-plus'

const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const filterVisible = ref('')
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const projectList = ref([])
const loading = ref(false)
const syncingGithub = ref(false)
const visibleLoading = ref({})
const recommendLoading = ref({})

const projectCategories = [
  { value: 'mobile', label: '移动应用' },
  { value: 'frontend', label: '前端项目' },
  { value: 'backend', label: '后端项目' },
  { value: 'fullstack', label: '全栈项目' },
  { value: 'ai', label: 'AI 工具' },
  { value: 'enterprise', label: '其他项目' }
]

async function fetchProjects() {
  loading.value = true
  try {
    const res = await getAdminProjects({
      page: currentPage.value,
      pageSize,
      keyword: searchKeyword.value || undefined,
      category: filterCategory.value || undefined,
      status: filterStatus.value || undefined,
      visible: filterVisible.value === '' ? undefined : filterVisible.value
    })
    if (res.code === 200 && res.data) {
      projectList.value = (res.data.records || []).map(transformProject)
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取 GitHub 项目失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchProjects)

function handleFilterChange() {
  currentPage.value = 1
  fetchProjects()
}

async function handleSyncGithub() {
  syncingGithub.value = true
  try {
    const res = await syncGithubProjects()
    if (res.code !== 200) throw new Error(res.message || '同步失败')
    const { total: read = 0, created = 0, updated = 0, skipped = 0 } = res.data || {}
    ElMessage.success(`同步完成：读取 ${read} 个，新增 ${created} 个，更新 ${updated} 个，跳过 Fork ${skipped} 个`)
    currentPage.value = 1
    await fetchProjects()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || 'GitHub 同步失败')
  } finally {
    syncingGithub.value = false
  }
}

const drawerVisible = ref(false)
const editingProject = ref(null)
const form = ref({})
const coverUploading = ref(false)
const coverPreview = computed(() => form.value.cover || form.value.sourceCover || '')

function openDrawer(project) {
  editingProject.value = project
  form.value = { ...project }
  drawerVisible.value = true
}

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
    if (res.code !== 200 || !res.data?.url) throw new Error(res.message || '上传失败')
    form.value.cover = res.data.url
    ElMessage.success('自定义封面上传成功')
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
  if (!editingProject.value) return
  try {
    const {
      effectiveCover, coverSource, categoryLabel, statusLabel, createdAt,
      ...editable
    } = form.value
    await updateProject(editingProject.value.id, {
      ...editable,
      techStack: JSON.stringify(form.value.techStack || []),
      features: JSON.stringify(form.value.features || []),
      goals: JSON.stringify(form.value.goals || []),
      results: JSON.stringify(form.value.results || []),
      challenges: JSON.stringify(form.value.challenges || [])
    })
    ElMessage.success('项目展示设置已保存')
    drawerVisible.value = false
    fetchProjects()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

async function toggleVisible(project) {
  const oldValue = project.visible === 1 ? 1 : 0
  const newValue = oldValue === 1 ? 0 : 1
  project.visible = newValue
  visibleLoading.value[project.id] = true
  try {
    const res = await updateProject(project.id, { visible: newValue })
    if (res.code !== 200) throw new Error(res.message || '更新失败')
    ElMessage.success(newValue === 1 ? '已在前台展示' : '已从前台隐藏')
  } catch (e) {
    project.visible = oldValue
    ElMessage.error(e.response?.data?.message || e.message || '展示状态更新失败')
  } finally {
    visibleLoading.value[project.id] = false
  }
}

async function toggleRecommend(project) {
  const oldValue = Boolean(project.isRecommended)
  const newValue = !oldValue
  project.isRecommended = newValue
  recommendLoading.value[project.id] = true
  try {
    const res = await updateProject(project.id, { isRecommended: newValue })
    if (res.code !== 200) throw new Error(res.message || '更新失败')
    ElMessage.success(newValue ? '已设为首页推荐' : '已取消首页推荐')
  } catch (e) {
    project.isRecommended = oldValue
    ElMessage.error(e.response?.data?.message || e.message || '推荐状态更新失败')
  } finally {
    recommendLoading.value[project.id] = false
  }
}
</script>

<template>
  <div class="admin-projects">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :loading="syncingGithub" @click="handleSyncGithub">
          {{ syncingGithub ? '正在同步...' : '同步 GitHub' }}
        </el-button>
        <span class="sync-hint">新仓库默认隐藏，同步不会覆盖自定义封面和展示选择</span>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目..."
          clearable
          style="width: 200px"
          @keyup.enter="handleFilterChange"
          @clear="handleFilterChange"
        />
        <el-select v-model="filterCategory" placeholder="分类筛选" clearable style="width: 130px" @change="handleFilterChange">
          <el-option v-for="c in projectCategories" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 110px" @change="handleFilterChange">
          <el-option label="已归档" value="completed" />
          <el-option label="维护中" value="in_progress" />
        </el-select>
        <el-select v-model="filterVisible" placeholder="展示筛选" clearable style="width: 120px" @change="handleFilterChange">
          <el-option label="前台展示" :value="1" />
          <el-option label="前台隐藏" :value="0" />
        </el-select>
      </div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="projectList" style="width: 100%" stripe>
        <el-table-column label="封面" width="92">
          <template #default="{ row }">
            <div v-if="row.effectiveCover" class="cover-thumb-wrap">
              <img :src="row.effectiveCover" :alt="`${row.name}封面`" class="cover-thumb" />
              <span class="cover-origin">{{ row.cover ? '自定义' : 'GitHub' }}</span>
            </div>
            <div v-else class="cover-thumb cover-thumb-empty">无</div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="项目名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="来源" width="86">
          <template #default="{ row }">
            <a :href="row.sourceUrl" target="_blank" rel="noopener noreferrer" class="source-link">GitHub ↗</a>
          </template>
        </el-table-column>
        <el-table-column prop="categoryLabel" label="分类" width="100" />
        <el-table-column label="技术栈" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="t in row.techStack.slice(0, 3)" :key="t" size="small" class="tech-tag">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'info' : 'success'" size="small">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="72">
          <template #default="{ row }">
            <el-switch
              :model-value="Boolean(row.isRecommended)"
              size="small"
              :loading="recommendLoading[row.id]"
              @change="toggleRecommend(row)"
            />
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
        <el-table-column label="创建时间" width="112">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="86" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDrawer(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="currentPage"
          :total="total"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          background
          @current-change="fetchProjects"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" title="编辑 GitHub 项目展示" size="560px">
      <el-form :model="form" label-position="top">
        <el-form-item label="GitHub 仓库">
          <a :href="form.sourceUrl" target="_blank" rel="noopener noreferrer" class="source-link">{{ form.sourceUrl }}</a>
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in projectCategories" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.summary" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="技术栈">
          <el-select v-model="form.techStack" multiple filterable allow-create default-first-option style="width: 100%" />
        </el-form-item>
        <el-form-item label="项目封面">
          <div class="cover-editor">
            <div class="cover-preview">
              <img v-if="coverPreview" :src="coverPreview" alt="项目封面预览" />
              <div v-else class="cover-placeholder">暂无来源封面</div>
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
              <el-button v-if="form.cover" type="danger" plain @click="removeCover">恢复 GitHub 封面</el-button>
              <span class="cover-hint">
                当前：{{ form.cover ? '自定义封面' : (form.sourceCover ? 'GitHub 默认封面' : '默认占位图') }}。最大 5MB。
              </span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="在前台展示">
          <el-switch v-model="form.visible" :active-value="1" :inactive-value="0" />
          <span class="field-hint">关闭后项目仍保留在后台</span>
        </el-form-item>
        <el-form-item label="首页推荐">
          <el-switch v-model="form.isRecommended" />
        </el-form-item>
        <el-form-item label="展示排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" style="width: 100%" @click="handleSave">保存展示设置</el-button>
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}
.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
.sync-hint,
.field-hint,
.cover-hint {
  color: var(--color-muted);
  font-size: 12px;
}
.table-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  box-shadow: var(--shadow-sm);
}
.cover-thumb-wrap {
  position: relative;
  width: 64px;
}
.cover-thumb {
  display: block;
  width: 64px;
  height: 42px;
  object-fit: cover;
  border: 1px solid var(--color-border-light);
  border-radius: 6px;
  background: var(--color-bg);
}
.cover-thumb-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-muted);
  font-size: 12px;
}
.cover-origin {
  position: absolute;
  right: 2px;
  bottom: 2px;
  padding: 0 3px;
  border-radius: 3px;
  background: rgba(0, 0, 0, .58);
  color: #fff;
  font-size: 9px;
  line-height: 15px;
}
.tech-tag {
  margin: 0 4px 4px 0;
}
.source-link {
  color: var(--color-primary);
  word-break: break-all;
  text-decoration: none;
}
.source-link:hover {
  text-decoration: underline;
}
.pagination-row {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.cover-editor {
  width: 100%;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.cover-preview {
  width: 260px;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
}
.cover-preview img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-muted);
  font-size: 12px;
}
.cover-actions {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}
.cover-hint {
  flex-basis: 100%;
  line-height: 1.5;
}
.field-hint {
  margin-left: 10px;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    flex-wrap: wrap;
  }
  .toolbar-right .el-input,
  .toolbar-right .el-select {
    width: 100% !important;
  }
  .sync-hint {
    flex-basis: 100%;
  }
  .cover-editor {
    flex-direction: column;
  }
  .cover-preview {
    width: 100%;
  }
}
</style>
