<script setup>
import { ref, onMounted } from 'vue'
import { getAdminMessages, getMessageStats, updateMessageStatus, deleteMessage } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 10
const searchKeyword = ref('')
const filterStatus = ref('')
const selectedIds = ref([])

const stats = ref({ total: 0, unread: 0, read: 0, replied: 0 })

async function fetchStats() {
  try {
    const res = await getMessageStats()
    if (res.code === 200 && res.data) {
      stats.value = res.data
    }
  } catch (e) { /* 统计非关键，失败静默 */ }
}

onMounted(() => {
  fetchList()
  fetchStats()
})

async function fetchList() {
  loading.value = true
  try {
    const params = { page: currentPage.value, pageSize }
    if (filterStatus.value) params.status = filterStatus.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getAdminMessages(params)
    if (res.code === 200 && res.data) {
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('获取留言列表失败')
  } finally { loading.value = false }
}

function onSearch() { currentPage.value = 1; fetchList() }
function onFilterChange() { selectedIds.value = []; currentPage.value = 1; fetchList() }
function onPageChange(p) { currentPage.value = p; fetchList() }

const detailVisible = ref(false)
const viewingMessage = ref(null)

async function viewDetail(msg) {
  viewingMessage.value = msg; detailVisible.value = true
  if (msg.status === 'unread') {
    try { await updateMessageStatus(msg.id, 'read'); msg.status = 'read'; fetchStats() } catch {}
  }
}

async function markReplied(msg) {
  try { await updateMessageStatus(msg.id, 'replied'); msg.status = 'replied'; fetchStats(); ElMessage.success('已标记为已回复') } catch { ElMessage.error('操作失败') }
}

async function handleDelete(msg) {
  try { await ElMessageBox.confirm('确定删除此留言？', '确认', { type: 'warning' }); await deleteMessage(msg.id); ElMessage.success('已删除'); detailVisible.value = false; await fetchList(); fetchStats() } catch {}
}

async function batchMarkRead() {
  for (const id of selectedIds.value) await updateMessageStatus(id, 'read').catch(() => {})
  selectedIds.value = []; fetchList(); ElMessage.success('批量标记已读完成')
}

async function batchDelete() {
  try { await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条留言？`, '批量删除', { type: 'warning' }) } catch { return }
  for (const id of selectedIds.value) await deleteMessage(id).catch(() => {})
  selectedIds.value = []; fetchList(); ElMessage.success('批量删除完成')
}

function toggleSelect(msg) {
  const idx = selectedIds.value.indexOf(msg.id)
  if (idx >= 0) selectedIds.value.splice(idx, 1); else selectedIds.value.push(msg.id)
}

function avatarChar(name) { return (name || '?').charAt(0).toUpperCase() }

function excerpt(content, max) { return content && content.length > max ? content.slice(0, max) + '…' : (content || '') }
</script>

<template>
  <div class="admin-messages">
    <!-- 头部 -->
    <div class="msg-header">
      <div>
        <h2>留言管理</h2>
        <p>查看和处理访客发送的联系信息</p>
      </div>
      <el-button size="small" @click="fetchList">刷新</el-button>
    </div>

    <!-- 统计 -->
    <div class="msg-stats">
      <span class="ms-item" :class="{ active: !filterStatus }" @click="filterStatus='';onFilterChange()">全部 {{ stats.total }}</span>
      <span class="ms-item unread" :class="{ active: filterStatus==='unread' }" @click="filterStatus='unread';onFilterChange()">未读 {{ stats.unread }}</span>
      <span class="ms-item" :class="{ active: filterStatus==='read' }" @click="filterStatus='read';onFilterChange()">已读 {{ stats.read }}</span>
      <span class="ms-item" :class="{ active: filterStatus==='replied' }" @click="filterStatus='replied';onFilterChange()">已回复 {{ stats.replied }}</span>
    </div>

    <!-- 列表卡片 -->
    <div class="msg-card">
      <!-- 工具栏 -->
      <div class="msg-toolbar">
        <div class="mt-left">
          <el-input v-model="searchKeyword" placeholder="搜索姓名、邮箱、主题..." size="small" clearable style="width:260px" @keyup.enter="onSearch" @clear="onSearch" />
          <el-select v-model="filterStatus" placeholder="全部状态" size="small" style="width:100px" @change="onFilterChange">
            <el-option label="全部" value="" />
            <el-option label="未读" value="unread" />
            <el-option label="已读" value="read" />
            <el-option label="已回复" value="replied" />
          </el-select>
        </div>
        <div class="mt-right" v-if="selectedIds.length">
          <span style="font-size:13px;color:#65766b">已选 {{ selectedIds.length }} 项</span>
          <el-button size="small" @click="batchMarkRead">标记已读</el-button>
          <el-button size="small" type="danger" @click="batchDelete">删除</el-button>
        </div>
      </div>

      <!-- 列表 -->
      <div v-if="list.length === 0 && !loading" class="msg-empty">
        <p v-if="!searchKeyword && !filterStatus">📭 暂时还没有收到留言</p>
        <p v-else>没有找到符合条件的留言</p>
        <el-button v-if="searchKeyword || filterStatus" size="small" @click="searchKeyword='';filterStatus='';onFilterChange()">清除筛选</el-button>
      </div>

      <div v-for="msg in list" :key="msg.id" class="msg-row" :class="{ unread: msg.status === 'unread' }" @click="viewDetail(msg)">
        <el-checkbox :model-value="selectedIds.includes(msg.id)" @click.stop="toggleSelect(msg)" style="margin-right:4px" />
        <div class="mr-avatar">{{ avatarChar(msg.name) }}</div>
        <div class="mr-info">
          <div class="mr-name">{{ msg.name }} <span class="mr-email">{{ msg.email }}</span></div>
          <div class="mr-subject">{{ msg.subject || '(无主题)' }}</div>
          <div class="mr-excerpt">{{ excerpt(msg.content, 60) }}</div>
        </div>
        <div class="mr-status">
          <span v-if="msg.status==='unread'" class="mr-tag unread">未读</span>
          <span v-else-if="msg.status==='replied'" class="mr-tag replied">已回复</span>
          <span v-else class="mr-tag read">已读</span>
        </div>
        <div class="mr-time">{{ formatDate(msg.createTime, 'MM-DD HH:mm') }}</div>
        <div class="mr-actions" @click.stop>
          <el-button size="small" @click="viewDetail(msg)">查看</el-button>
          <el-dropdown trigger="click" popper-class="msg-action-menu">
            <el-button size="small">⋮</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="msg.status==='unread'" @click="updateMessageStatus(msg.id,'read');msg.status='read'">标记已读</el-dropdown-item>
                <el-dropdown-item v-if="msg.status!=='replied'" @click="markReplied(msg)">标记已回复</el-dropdown-item>
                <el-dropdown-item @click="navigator.clipboard?.writeText(msg.email);ElMessage.success('已复制')">复制邮箱</el-dropdown-item>
                <el-dropdown-item class="danger-item" divided @click="handleDelete(msg)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 分页 -->
      <div class="msg-footer" v-if="total > 0">
        <span>共 {{ total }} 条留言</span>
        <el-pagination v-model:current-page="currentPage" :total="total" :page-size="pageSize" layout="prev,next" background size="small" @current-change="onPageChange" />
      </div>
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="留言详情" direction="rtl" size="480px">
      <template v-if="viewingMessage">
        <div class="detail-wrap">
          <div class="dw-field"><label>姓名</label><span>{{ viewingMessage.name }}</span></div>
          <div class="dw-field"><label>邮箱</label><span>{{ viewingMessage.email }}</span></div>
          <div class="dw-field"><label>主题</label><span>{{ viewingMessage.subject }}</span></div>
          <div class="dw-field"><label>时间</label><span>{{ formatDate(viewingMessage.createTime, 'YYYY-MM-DD HH:mm') }}</span></div>
          <div class="dw-field"><label>IP</label><span>{{ viewingMessage.ip || '—' }}</span></div>
          <div class="dw-field"><label>状态</label><span>{{ viewingMessage.status==='unread'?'未读':viewingMessage.status==='replied'?'已回复':'已读' }}</span></div>
          <div class="dw-content">
            <label>留言内容</label>
            <p>{{ viewingMessage.content }}</p>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="navigator.clipboard?.writeText(viewingMessage.email);ElMessage.success('已复制')">复制邮箱</el-button>
        <el-button v-if="viewingMessage?.status!=='replied'" type="warning" @click="markReplied(viewingMessage)">标记已回复</el-button>
        <el-button type="danger" @click="handleDelete(viewingMessage)">删除</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.admin-messages { padding: 20px 24px; max-width: 1200px; }
.msg-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.msg-header h2 { font-size: 22px; font-weight: 700; color: #17231b; }
.msg-header p { font-size: 13px; color: #65766b; margin-top: 2px; }
.msg-stats { display: flex; gap: 4px; margin-bottom: 16px; }
.ms-item { padding: 5px 14px; border-radius: 16px; font-size: 13px; cursor: pointer; color: #53675a; border: 1px solid transparent; transition: all 0.15s; }
.ms-item:hover { background: #eaf5ec; color: #2f9142; }
.ms-item.active { background: #2f9142; color: #fff; }
.ms-item.unread.active { background: #F56C6C; }
.msg-card { background: rgba(255,255,255,0.95); border: 1px solid #e2e9e3; border-radius: 14px; box-shadow: 0 1px 6px rgba(0,0,0,0.04); overflow: hidden; }
.msg-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #edf0ed; gap: 8px; flex-wrap: wrap; }
.mt-left, .mt-right { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.msg-empty { text-align: center; padding: 64px 24px; color: #909399; font-size: 14px; }
.msg-row { display: flex; align-items: center; gap: 10px; padding: 14px 16px; border-bottom: 1px solid #f5f7f5; cursor: pointer; transition: background 0.1s; }
.msg-row:hover { background: #f7faf7; }
.msg-row.unread { background: #f8fdf8; }
.mr-avatar { width: 40px; height: 40px; border-radius: 50%; background: #eaf5ec; color: #2f9142; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; flex-shrink: 0; }
.mr-info { flex: 1; min-width: 0; }
.mr-name { font-size: 14px; font-weight: 600; color: #1f3025; }
.unread .mr-name { font-weight: 700; }
.mr-email { font-size: 12px; color: #909399; font-weight: 400; margin-left: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px; display: inline-block; vertical-align: middle; }
.mr-subject { font-size: 14px; color: #34463a; margin-top: 1px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.unread .mr-subject { font-weight: 600; }
.mr-excerpt { font-size: 12px; color: #909399; margin-top: 1px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mr-status { flex-shrink: 0; }
.mr-tag { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 11px; font-weight: 500; }
.mr-tag.unread { background: #FEF0F0; color: #F56C6C; }
.mr-tag.read { background: #F4F4F5; color: #909399; }
.mr-tag.replied { background: #F0F9EB; color: #67C23A; }
.mr-time { font-size: 12px; color: #909399; white-space: nowrap; flex-shrink: 0; }
.mr-actions { display: flex; gap: 6px; align-items: center; flex-shrink: 0; }
.msg-footer { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; font-size: 13px; color: #65766b; border-top: 1px solid #edf0ed; }
.detail-wrap { display: flex; flex-direction: column; gap: 12px; }
.dw-field { display: flex; gap: 8px; font-size: 14px; }
.dw-field label { color: #718077; min-width: 50px; }
.dw-content { margin-top: 8px; }
.dw-content label { font-size: 13px; color: #718077; display: block; margin-bottom: 6px; }
.dw-content p { font-size: 14px; line-height: 1.8; color: #34463a; background: #f7f9f6; padding: 14px; border-radius: 8px; }
</style>

<style>
.msg-action-menu { width: 130px !important; padding: 4px !important; border-radius: 8px !important; }
.msg-action-menu .el-dropdown-menu__item { height: 38px; padding: 0 14px; font-size: 13px; border-radius: 4px; }
.msg-action-menu .el-dropdown-menu__item:hover { background: #eaf5ec; }
.msg-action-menu .danger-item { color: #d94b4b !important; }
.msg-action-menu .danger-item:hover { background: #fff1f1 !important; }
</style>
