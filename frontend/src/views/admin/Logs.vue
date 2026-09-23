<script setup>
import { ref, onMounted } from 'vue'
import { getAdminLogs, getAdminLogModules } from '@/api/admin'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const modules = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const filterModule = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = 20

onMounted(() => {
  fetchLogs()
  fetchModules()
})

async function fetchLogs() {
  loading.value = true
  try {
    const params = { page: currentPage.value, pageSize }
    if (filterModule.value) params.module = filterModule.value
    if (filterStatus.value) params.status = filterStatus.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getAdminLogs(params)
    if (res.code === 200 && res.data) {
      list.value = (res.data.records || []).map(log => ({
        ...log,
        user: log.userName || log.user || '',
        time: log.createTime || log.time || ''
      }))
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('获取操作日志失败', e)
    ElMessage.error('获取操作日志失败')
  } finally {
    loading.value = false
  }
}

async function fetchModules() {
  try {
    const res = await getAdminLogModules()
    if (res.code === 200 && res.data) {
      modules.value = res.data || []
    }
  } catch (e) { /* 筛选器非关键功能，失败静默 */ }
}

function onPageChange(page) {
  currentPage.value = page
  fetchLogs()
}

function onFilterChange() {
  currentPage.value = 1
  fetchLogs()
}
</script>

<template>
  <div class="admin-logs">
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="toolbar-info">共 {{ total }} 条记录</span>
      </div>
      <div class="toolbar-right">
        <el-input v-model="searchKeyword" placeholder="搜索日志..." clearable style="width: 200px" @keyup.enter="onFilterChange" @clear="onFilterChange" />
        <el-select v-model="filterModule" placeholder="模块" clearable style="width: 130px" @change="onFilterChange">
          <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 110px" @change="onFilterChange">
          <el-option label="成功" value="success" />
          <el-option label="失败" value="failed" />
        </el-select>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="list" style="width: 100%" stripe size="small" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="user" label="用户" width="80">
          <template #default="{ row }">{{ row.userName || row.user }}</template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="100" />
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ row.createTime || row.time }}</template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;display:flex;justify-content:flex-end;">
        <el-pagination
          v-model:current-page="currentPage"
          :total="total"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          background
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-md); flex-wrap: wrap; gap: var(--spacing-sm); }
.toolbar-left { display: flex; align-items: center; gap: var(--spacing-sm); }
.toolbar-right { display: flex; gap: var(--spacing-sm); }
.toolbar-info { font-size: var(--font-size-sm); color: var(--color-secondary); }
.table-card { background: #fff; border-radius: var(--radius-md); padding: var(--spacing-md); box-shadow: var(--shadow-sm); overflow-x: auto; }
</style>
