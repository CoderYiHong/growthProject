<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAdminSkills, createSkill, updateSkill, deleteSkill } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const searchKeyword = ref('')
const loading = ref(false)
const drawerVisible = ref(false)
const editingSkill = ref(null)
const form = ref({})

// 从后端获取的分类列表
const categories = computed(() => {
  const cats = [...new Set(list.value.map(s => s.category).filter(Boolean))]
  return cats.sort()
})

const statusOptions = [
  { value: 'mastered', label: '已掌握基础' },
  { value: 'practicing', label: '项目实践中' },
  { value: 'learning', label: '持续学习' },
  { value: 'planned', label: '计划深入' }
]

const statusTagType = (status) => {
  return status === 'mastered' ? 'success' : status === 'practicing' ? '' : 'warning'
}

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getAdminSkills()
    if (res.code === 200) {
      list.value = res.data || []
    }
  } catch (e) {
    console.error('获取技能列表失败', e)
    ElMessage.error('获取技能列表失败')
  } finally {
    loading.value = false
  }
}

const allSkills = computed(() => {
  let result = [...list.value]
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(s => s.name.toLowerCase().includes(kw))
  }
  return result
})

function openDrawer(skill = null) {
  editingSkill.value = skill
  form.value = skill
    ? { name: skill.name, category: skill.category, level: skill.level || 'learning' }
    : { name: '', category: '', level: 'learning' }
  drawerVisible.value = true
}

async function handleSave() {
  if (!form.value.name) { ElMessage.warning('请输入技能名称'); return }
  if (!form.value.category) { ElMessage.warning('请选择分类'); return }
  try {
    const payload = {
      name: form.value.name,
      category: form.value.category,
      level: form.value.level || 'learning'
    }
    if (editingSkill.value) {
      const res = await updateSkill(editingSkill.value.id, payload)
      if (res.code === 200) ElMessage.success('更新成功')
    } else {
      const res = await createSkill(payload)
      if (res.code === 200) ElMessage.success('新增成功')
    }
    drawerVisible.value = false
    await fetchList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function handleDelete(skill) {
  ElMessageBox.confirm(`确定要删除「${skill.name}」吗？`, '确认', { type: 'warning' })
    .then(async () => {
      const res = await deleteSkill(skill.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await fetchList()
      }
    })
    .catch(() => {})
}
</script>

<template>
  <div class="admin-skills">
    <div class="toolbar">
      <el-button type="success" @click="openDrawer()">新增技能</el-button>
      <el-input v-model="searchKeyword" placeholder="搜索技能..." clearable style="width: 200px" />
    </div>

    <div class="table-card">
      <el-table :data="allSkills" style="width: 100%" stripe v-loading="loading">
        <el-table-column prop="name" label="技能名称" width="180" />
        <el-table-column prop="category" label="所属分类" width="140" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.level)" size="small">
              {{ statusOptions.find(s => s.value === row.level)?.label || row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openDrawer(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="drawerVisible" :title="editingSkill ? '编辑技能' : '新增技能'" size="400px">
      <el-form :model="form" label-position="top">
        <el-form-item label="技能名称" required>
          <el-input v-model="form.name" placeholder="如：Vue3" />
        </el-form-item>
        <el-form-item label="所属分类" required>
          <el-select v-model="form.category" style="width:100%" placeholder="选择或输入分类" filterable allow-create>
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="掌握状态">
          <el-select v-model="form.level" style="width:100%">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleSave" style="width:100%">{{ editingSkill ? '保存' : '新增' }}</el-button>
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-md); gap: var(--spacing-sm); }
.table-card { background: #fff; border-radius: var(--radius-md); padding: var(--spacing-md); box-shadow: var(--shadow-sm); }
</style>
