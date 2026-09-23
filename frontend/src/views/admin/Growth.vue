<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAdminGrowthStages, createGrowthStage, updateGrowthStage, deleteGrowthStage } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const searchKeyword = ref('')
const loading = ref(false)
const drawerVisible = ref(false)
const editingItem = ref(null)
const form = ref({})

// 后端字段映射辅助
function toForm(item) {
  return {
    period: item.period || '',
    title: item.title || '',
    subtitle: item.subtitle || '',
    icon: item.icon || 'seed',
    description: item.description || '',
    skills: item.skills ? JSON.parse(item.skills) : [],
    achievements: item.achievements ? JSON.parse(item.achievements) : [],
    color: item.color || '#A8D8B9',
    sortOrder: item.sortOrder || 0
  }
}

function toPayload(formData) {
  return {
    ...formData,
    skills: JSON.stringify(formData.skills || []),
    achievements: JSON.stringify(formData.achievements || [])
  }
}

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getAdminGrowthStages()
    if (res.code === 200) {
      list.value = (res.data || []).map(item => ({
        ...item,
        skills: item.skills ? JSON.parse(item.skills) : [],
        achievements: item.achievements ? JSON.parse(item.achievements) : []
      }))
    }
  } catch (e) {
    console.error('获取成长阶段失败', e)
    ElMessage.error('获取成长阶段失败')
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() => {
  let result = [...list.value]
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(s =>
      s.title.toLowerCase().includes(kw) ||
      s.description.toLowerCase().includes(kw)
    )
  }
  return result
})

function openDrawer(item = null) {
  editingItem.value = item
  form.value = item
    ? toForm(item)
    : { period: '', title: '', subtitle: '', icon: 'seed', description: '', skills: [], achievements: [], color: '#A8D8B9', sortOrder: 0 }
  drawerVisible.value = true
}

async function handleSave() {
  if (!form.value.title) { ElMessage.warning('请输入阶段名称'); return }
  try {
    const payload = toPayload({ ...form.value })
    if (editingItem.value) {
      const res = await updateGrowthStage(editingItem.value.id, payload)
      if (res.code === 200) ElMessage.success('更新成功')
    } else {
      const res = await createGrowthStage(payload)
      if (res.code === 200) ElMessage.success('新增成功')
    }
    drawerVisible.value = false
    await fetchList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function handleDelete(item) {
  ElMessageBox.confirm(`确定要删除「${item.title}」吗？`, '确认', { type: 'warning' })
    .then(async () => {
      const res = await deleteGrowthStage(item.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await fetchList()
      }
    })
    .catch(() => {})
}
</script>

<template>
  <div class="admin-growth">
    <div class="toolbar">
      <el-button type="success" @click="openDrawer()">新增阶段</el-button>
      <el-input v-model="searchKeyword" placeholder="搜索..." clearable style="width: 240px" />
    </div>

    <div class="table-card">
      <el-table :data="filteredList" style="width: 100%" stripe v-loading="loading">
        <el-table-column label="图标" width="70">
          <template #default="{ row }">
            <div class="icon-cell" :style="{ background: row.color }">
              <span style="color:#fff;font-size:18px;">
                {{ row.icon === 'seed' ? '🌱' : row.icon === 'sprout' ? '🌿' : row.icon === 'tree' ? '🌳' : '🎋' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="period" label="时间" width="130" />
        <el-table-column prop="title" label="阶段名称" width="150" />
        <el-table-column prop="subtitle" label="副标题" width="180" />
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openDrawer(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="drawerVisible" :title="editingItem ? '编辑阶段' : '新增阶段'" size="500px">
      <el-form :model="form" label-position="top">
        <el-form-item label="时间范围">
          <el-input v-model="form.period" placeholder="如：2025 — 至今" />
        </el-form-item>
        <el-form-item label="阶段名称" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleSave" style="width:100%">{{ editingItem ? '保存修改' : '确认新增' }}</el-button>
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-md); gap: var(--spacing-sm); }
.table-card { background: #fff; border-radius: var(--radius-md); padding: var(--spacing-md); box-shadow: var(--shadow-sm); }
.icon-cell { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
</style>
