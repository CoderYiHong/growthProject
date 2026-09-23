<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStudyPlanStore } from '@/stores/studyPlan'
import { planSheets, planStatuses, itemStatuses, techCategories, examSubjects, knowledgeTypes, masteryLevels } from '@/data/studyPlans'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as studyPlanApi from '@/api/admin'

const store = useStudyPlanStore()
const today = new Date().toISOString().slice(0, 10)

// 初始化后端同步
store.setApi(studyPlanApi)
onMounted(() => store.pullFromBackend())

const activeSheet = ref('all')
const searchKeyword = ref('')
const filteredPlans = computed(() => {
  let list = activeSheet.value === 'all' ? [...store.plans] : store.plans.filter(p => p.category === activeSheet.value)
  if (searchKeyword.value) { const kw = searchKeyword.value.toLowerCase(); list = list.filter(p => p.name.toLowerCase().includes(kw)) }
  return list
})

// 统计子任务数
function itemStats(plan) {
  let total = 0, done = 0
  if (plan.groups) for (const g of plan.groups) { total += (g.items || []).length; done += (g.items || []).filter(it => it.status === 'done').length }
  return { total, done }
}

// ── 编辑弹窗 ──
const showForm = ref(false); const editingId = ref(null)
const defaultForm = () => ({ category: '技术学习', type: '技术线', name: '', status: 'pending', startDate: '', endDate: '', plannedHours: 10, importance: 3, note: '', groups: [], subject: '', chapters: '', questionsDone: 0, questionsTotal: 0, accuracy: 0, studyRound: 1 })
const form = ref(defaultForm())

// ── 草稿保护 ──
const DRAFT_KEY = 'study_plan_draft'

function saveDraft() {
  // 仅新建时有意义，编辑模式不保存草稿
  if (editingId.value) return
  // 没有任何输入内容时不保存
  const f = form.value
  if (!f.name.trim() && !f.note.trim() && f.category === '技术学习' && f.type === '技术线'
    && f.plannedHours === 10 && f.importance === 3 && !f.startDate && !f.endDate
    && !f.subject && !f.chapters && !f.questionsTotal && f.studyRound === 1) return
  localStorage.setItem(DRAFT_KEY, JSON.stringify(f))
}

function loadDraft() {
  try { const raw = localStorage.getItem(DRAFT_KEY); return raw ? JSON.parse(raw) : null }
  catch { return null }
}

function clearDraft() { localStorage.removeItem(DRAFT_KEY) }

function onDialogClosed() {
  // 弹窗关闭时自动保存草稿（仅新建模式）
  saveDraft()
}

function openCreate() {
  editingId.value = null
  const draft = loadDraft()
  if (draft) {
    ElMessageBox.confirm(
      '检测到上次未完成的新建计划，是否恢复草稿继续编辑？',
      '恢复草稿',
      { confirmButtonText: '恢复草稿', cancelButtonText: '放弃', type: 'info', distinguishCancelAndClose: true }
    ).then(() => {
      form.value = draft
      showForm.value = true
    }).catch(() => {
      clearDraft()
      form.value = defaultForm()
      showForm.value = true
    })
  } else {
    form.value = defaultForm()
    showForm.value = true
  }
}

function openEdit(plan) { editingId.value = plan.id; form.value = JSON.parse(JSON.stringify(plan)); showForm.value = true }

function savePlan() {
  if (!form.value.name.trim()) return ElMessage.warning('请输入学习内容')
  if (editingId.value) { store.updatePlan(editingId.value, form.value); ElMessage.success('已更新') }
  else { store.addPlan({ ...form.value, id: 'sp_' + Date.now(), groups: form.value.groups || [], actualHours: 0, progress: 0, lastStudyTime: null, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() }); ElMessage.success('已创建') }
  // 同步到后端
  const plan = store.getPlanById(editingId.value || store.plans[store.plans.length - 1]?.id)
  if (plan) store.pushToBackend(plan)
  clearDraft()  // 保存成功后清除草稿
  showForm.value = false
}
async function confirmDelete(id) { try { await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' }); store.deletePlan(id); store.deleteFromBackend(id); ElMessage.success('已删除') } catch { /* cancelled */ } }
function copyPlan(id) { store.copyPlan(id); ElMessage.success('已复制') }
function cellUpdate(plan, field, value) { store.updatePlan(plan.id, { [field]: value }) }
const statusEditing = ref(null)
function statusSelect(plan, newStatus) {
  const old = plan.status
  store.updatePlan(plan.id, { status: newStatus })
  // 验证是否更新成功（从 store 重新读取）
  const updated = store.getPlanById(plan.id)
  if (updated?.status !== newStatus) { plan.status = old; ElMessage.error('状态更新失败') }
  statusEditing.value = null
}
function isOverdue(plan) { return plan.endDate && plan.endDate < today && plan.status !== 'done' && plan.status !== 'paused' }
function statusTag(s) { return planStatuses.find(st => st.value === s) || { label: s, color: '#909399', bg: '#F4F4F5' } }

// ── 子任务管理 ──
const subtaskVisible = ref(false)
const subtaskPlan = ref(null)
const activeGroupId = ref('')
const activeGroup = computed(() => subtaskPlan.value?.groups?.find(g => g.id === activeGroupId.value) || null)

function refreshPlan() { subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value?.id))) }
function openSubtasks(plan) {
  subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(plan.id)))
  if (subtaskPlan.value?.groups?.length) activeGroupId.value = subtaskPlan.value.groups[0].id
  else activeGroupId.value = ''
  subtaskVisible.value = true
}

// ── 系列操作 ──
function addGroup() {
  ElMessageBox.prompt('系列名称', '新增任务系列', { confirmButtonText: '确定' }).then(({ value }) => {
    if (value?.trim()) { store.addGroup(subtaskPlan.value.id, { name: value.trim() }); subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value.id))); activeGroupId.value = subtaskPlan.value.groups[subtaskPlan.value.groups.length - 1].id }
  }).catch(() => {})
}
function renameGroup(group) {
  ElMessageBox.prompt('新名称', '重命名', { inputValue: group.name }).then(({ value }) => { if (value?.trim()) store.updateGroup(subtaskPlan.value.id, group.id, { name: value.trim() }); subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value.id))) }).catch(() => {})
}
async function deleteGroupWrap(group) {
  const hasItems = (group.items || []).length > 0
  if (!hasItems) { store.deleteGroup(subtaskPlan.value.id, group.id, false); subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value.id))); if (activeGroupId.value === group.id) activeGroupId.value = subtaskPlan.value?.groups?.[0]?.id || ''; return }
  try {
    const action = await ElMessageBox.confirm(`「${group.name}」包含 ${group.items.length} 个子任务，如何处理？`, '删除系列', { confirmButtonText: '同时删除子任务', cancelButtonText: '取消', distinguishCancelAndClose: true, type: 'warning' })
    store.deleteGroup(subtaskPlan.value.id, group.id, true); subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value.id))); activeGroupId.value = subtaskPlan.value?.groups?.[0]?.id || ''
  } catch { /* 取消 */ }
}

// ── 子任务操作 ──
const newItemTitle = ref('')
const batchPasteText = ref('')
const showBatchPaste = ref(false)

function quickAddItem() {
  if (!newItemTitle.value.trim() || !activeGroupId.value) return
  store.addItem(subtaskPlan.value.id, activeGroupId.value, { title: newItemTitle.value.trim() })
  newItemTitle.value = ''; subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value.id)))
}
function batchPaste() {
  const lines = batchPasteText.value.split('\n').map(l => l.trim()).filter(Boolean)
  if (!lines.length) return ElMessage.warning('请输入子任务标题')
  store.batchAddItems(subtaskPlan.value.id, activeGroupId.value, lines)
  batchPasteText.value = ''; showBatchPaste.value = false; subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value.id))); ElMessage.success(`已创建 ${lines.length} 个子任务`)
}
function itemStatusColor(s) { return itemStatuses.find(st => st.value === s)?.color || '#909399' }
function duplicateItem(item) {
  store.addItem(subtaskPlan.value.id, activeGroupId.value, { ...item, title: item.title + ' (副本)', status: 'pending', actualPomodoros: 0 })
  subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value.id))); ElMessage.success('已复制')
}
async function deleteItemConfirm(item) {
  try { await ElMessageBox.confirm(`删除「${item.title}」？`, '确认', { type: 'warning' }); store.deleteItem(subtaskPlan.value.id, item.id); subtaskPlan.value = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.value.id))); ElMessage.success('已删除') } catch { /* cancelled */ }
}

// ── Excel ──
function exportExcel() {
  const data = filteredPlans.value
  if (!data.length) return ElMessage.warning('无数据')

  function statusLabel(s) { return planStatuses.find(st => st.value === s)?.label || s }
  function statusColorHex(s) { return planStatuses.find(st => st.value === s)?.color || '#909399' }
  function esc(s) { return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;') }

  const rows = data.map((p, i) => {
    const bg = i % 2 === 0 ? '#ffffff' : '#f7f9f6'
    const sc = statusColorHex(p.status)
    return `<tr style="background:${bg}">
      <td style="font-weight:600">${esc(p.category)}</td>
      <td>${esc(p.type || '')}</td>
      <td style="font-weight:600;color:#1f3025">${esc(p.name)}</td>
      <td style="color:${sc};font-weight:600;text-align:center">
        <span style="display:inline-block;padding:3px 12px;border-radius:4px;background:${sc}18;border:1px solid ${sc}40">${statusLabel(p.status)}</span>
      </td>
      <td style="text-align:center">${esc(p.startDate || '—')}</td>
      <td style="text-align:center;color:${p.endDate && p.endDate < today && p.status !== 'done' && p.status !== 'paused' ? '#F56C6C' : '#5B7262'}">${esc(p.endDate || '—')}</td>
      <td style="text-align:right">${p.plannedHours}h</td>
      <td style="text-align:right">${(p.actualHours || 0).toFixed(1)}h</td>
      <td style="text-align:center;white-space:nowrap">
        <span style="display:inline-block;width:60px;height:6px;border-radius:3px;background:#E4E7ED;vertical-align:middle;margin-right:6px">
          <span style="display:inline-block;height:6px;border-radius:3px;background:#4F9D57;width:${Math.min(100, p.progress || 0)}%"></span>
        </span>
        <span style="font-size:11px;font-weight:700;color:#4F9D57">${p.progress}%</span>
      </td>
      <td style="text-align:center;color:#F0B85A;font-size:14px;letter-spacing:1px">${'★'.repeat(p.importance || 0)}${'☆'.repeat(Math.max(0, 5 - (p.importance || 0)))}</td>
      <td style="color:#909399;font-size:11px">${esc(p.note || '')}</td>
    </tr>`
  }).join('')

  const html = `<!DOCTYPE html><html><head><meta charset="UTF-8">
<style>
  table { border-collapse:collapse; width:100%; font-family:"Microsoft YaHei","PingFang SC",sans-serif; font-size:12px }
  th { background:#24463A; color:#ffffff; font-weight:600; font-size:12px; padding:10px 12px; border:1px solid #1f3a2e; text-align:center; white-space:nowrap }
  td { padding:8px 12px; border:1px solid #d8e5db; vertical-align:middle }
  tr:hover td { background:#eaf5ec !important }
</style></head><body>
  <h2 style="color:#24463A;font-family:'Microsoft YaHei',sans-serif;margin-bottom:4px">📋 学习计划</h2>
  <p style="color:#909399;font-size:11px;margin:0 0 12px 0">导出日期：${today}　｜　共 ${data.length} 条计划</p>
  <table>
    <thead><tr>
      <th>分类</th><th>类型</th><th>学习计划</th><th>状态</th><th>开始</th><th>截止</th><th>计划(h)</th><th>实际(h)</th><th style="min-width:140px">进度</th><th>重要</th><th>备注</th>
    </tr></thead>
    <tbody>${rows}</tbody>
  </table>
</body></html>`

  const blob = new Blob(['﻿' + html], { type: 'application/vnd.ms-excel;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `学习计划_${today}.xls`
  a.click()
  ElMessage.success(`已导出 ${data.length} 条计划`)
}
function handleFileUpload(e) {
  const file = e.target.files[0]; if (!file) return
  const reader = new FileReader(); reader.onload = (ev) => {
    const lines = ev.target.result.split('\n').filter(l => l.trim()); if (lines.length < 2) return ElMessage.error('文件为空')
    const preview = lines.slice(1).map((l, i) => { const c = l.split(',').map(c => c.replace(/^"(.*)"$/, '$1').trim()); return { id: 'imp_'+i, category: c[0]||'技术学习', type: c[1]||'', name: c[2]||'', status: c[3]||'pending', startDate: c[4]||'', endDate: c[5]||'', plannedHours: Number(c[6])||0, importance: Number(c[7])||3, note: c[8]||'', groups: [] } }).filter(p => p.name)
    store.importPlans(preview); ElMessage.success(`导入 ${preview.length} 条`)
  }; reader.readAsText(file); e.target.value = ''
}
</script>

<template>
  <div class="admin-plans">
    <div class="ap-head"><h2>学习计划管理</h2><div class="ap-acts"><label class="ap-btn"><input type="file" accept=".csv,.xls,.xlsx" @change="handleFileUpload" hidden />📥 导入</label><button class="ap-btn" @click="exportExcel">📤 导出</button><button class="ap-btn primary" @click="openCreate">＋ 新建计划</button></div></div>

    <div class="ap-tabs"><button v-for="sh in planSheets" :key="sh.key" class="atab" :class="{ active: activeSheet === sh.key }" @click="activeSheet = sh.key">{{ sh.icon }} {{ sh.label }} <span class="atab-n">{{ sh.key === 'all' ? store.plans.length : store.plans.filter(p => p.category === sh.key).length }}</span></button></div>
    <div class="ap-bar"><el-input v-model="searchKeyword" placeholder="搜索..." size="small" clearable style="width:260px" /></div>

    <div class="ap-table-wrap">
      <el-table :data="filteredPlans" stripe style="width:100%" :header-cell-style="{ background:'#F7F9F6',color:'#5B7262',fontWeight:600,fontSize:'12px' }" :cell-style="{ fontSize:'13px',padding:'10px 8px' }">
        <!-- 1. 分类/类型 -->
        <el-table-column label="分类/类型" width="100">
          <template #default="{ row }">
            <div>
              <span class="cat-tag" :style="{ background: (techCategories.find(c=>c.value===row.type)||examSubjects.find(s=>s.value===row.type)||{}).color + '18', color: (techCategories.find(c=>c.value===row.type)||examSubjects.find(s=>s.value===row.type)||{}).color || '#909399', fontWeight:600, fontSize:'11px' }">{{ row.category }}</span>
              <div style="font-size:11px;color:#909399;margin-top:1px">{{ row.type }}</div>
            </div>
          </template>
        </el-table-column>
        <!-- 2. 学习计划 -->
        <el-table-column label="学习计划" min-width="240">
          <template #default="{ row }">
            <el-tooltip :content="row.name" placement="top" :show-after="500">
              <span class="plan-name-cell">{{ row.name }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <!-- 3. 状态/截止 -->
        <el-table-column label="状态/截止" width="130" sortable="custom" prop="endDate">
          <template #default="{ row }">
            <el-popover :visible="statusEditing === row.id" trigger="manual" placement="bottom" :width="140">
              <template #reference>
                <span class="status-click-tag" :style="{ background: statusTag(row.status).bg, color: statusTag(row.status).color, borderColor: statusTag(row.status).color + '40' }" @click.stop="statusEditing = statusEditing === row.id ? null : row.id">{{ statusTag(row.status).label }}</span>
              </template>
              <div style="display:flex;flex-direction:column;gap:2px">
                <div v-for="s in planStatuses" :key="s.value" style="padding:6px 10px;cursor:pointer;border-radius:4px;font-size:13px" :style="{ background: s.value === row.status ? s.bg : 'transparent', color: s.color }" @click="statusSelect(row, s.value)">{{ s.label }}</div>
              </div>
            </el-popover>
            <div :style="{ fontSize:'11px', color: isOverdue(row) ? '#F56C6C' : '#909399', marginTop:2 }">截止 {{ row.endDate || '—' }}</div>
          </template>
        </el-table-column>
        <!-- 4. 任务进度 -->
        <el-table-column label="任务进度" width="160">
          <template #default="{ row }">
            <div style="font-size:11px;color:#65766b;margin-bottom:2px">完成 {{ itemStats(row).done }}/{{ itemStats(row).total }} 项</div>
            <div style="display:flex;align-items:center;gap:6px">
              <el-progress :percentage="row.progress" :stroke-width="6" :show-text="false" style="flex:1" />
              <span style="font-size:12px;font-weight:700;color:#4F9D57;min-width:32px">{{ row.progress }}%</span>
            </div>
          </template>
        </el-table-column>
        <!-- 5. 重要程度 -->
        <el-table-column label="重要" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.importance" size="small" @change="v => cellUpdate(row, 'importance', v)" />
          </template>
        </el-table-column>
        <!-- 6. 操作 -->
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button size="small" @click="openSubtasks(row)">📋 子任务</el-button>
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-dropdown trigger="click" popper-class="plan-action-menu">
                <el-button size="small">⋮</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="copyPlan(row.id)">
                      <span class="menu-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg></span>
                      <span>复制</span>
                    </el-dropdown-item>
                    <el-dropdown-item class="danger-item" @click="confirmDelete(row.id)" divided>
                      <span class="menu-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg></span>
                      <span>删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showForm" :title="editingId ? '编辑计划' : '新建计划'" width="620px" destroy-on-close @closed="onDialogClosed">
      <div class="form-grid">
        <label>分类 <el-select v-model="form.category" size="small"><el-option v-for="sh in planSheets.filter(s=>s.key!=='all')" :key="sh.key" :label="sh.label" :value="sh.key" /></el-select></label>
        <label>类型 <el-select v-model="form.type" size="small"><el-option v-for="t in [...techCategories,...examSubjects]" :key="t.value" :label="t.label" :value="t.value" /></el-select></label>
        <label>状态 <el-select v-model="form.status" size="small"><el-option v-for="s in planStatuses" :key="s.value" :label="s.label" :value="s.value" /></el-select></label>
        <label>重要 <el-rate v-model="form.importance" size="small" /></label>
        <label class="span-2">名称 <el-input v-model="form.name" size="small" /></label>
        <label>开始 <el-input v-model="form.startDate" type="date" size="small" /></label>
        <label>截止 <el-input v-model="form.endDate" type="date" size="small" /></label>
        <label>计划h <el-input-number v-model="form.plannedHours" :min="1" size="small" /></label>
        <label v-if="form.category==='考研学习'">科目 <el-select v-model="form.type" size="small"><el-option v-for="s in examSubjects" :key="s.value" :label="s.label" :value="s.value" /></el-select></label>
        <label v-if="form.category==='考研学习'">章节 <el-input v-model="form.chapters" size="small" /></label>
        <label v-if="form.category==='考研学习'">题总数 <el-input-number v-model="form.questionsTotal" :min="0" size="small" /></label>
        <label v-if="form.category==='考研学习'">轮次 <el-input-number v-model="form.studyRound" :min="1" size="small" /></label>
        <label class="span-2">备注 <el-input v-model="form.note" size="small" type="textarea" :rows="2" /></label>
      </div>
      <template #footer><el-button @click="showForm = false">取消</el-button><el-button type="primary" @click="savePlan">保存</el-button></template>
    </el-dialog>

    <!-- 子任务管理 -->
    <el-drawer v-model="subtaskVisible" :title="subtaskPlan?.name || '子任务管理'" direction="rtl" size="80%" destroy-on-close>
      <template v-if="subtaskPlan">
        <div class="st-head">
          <div><strong>总体进度</strong> <span style="color:var(--color-primary);font-weight:700">{{ subtaskPlan.progress }}%</span></div>
          <div><strong>计划/实际</strong> {{ subtaskPlan.plannedHours }}h / {{ (subtaskPlan.actualHours||0).toFixed(1) }}h</div>
          <div><strong>总子任务</strong> {{ itemStats(subtaskPlan).done }}/{{ itemStats(subtaskPlan).total }} 完成</div>
        </div>
        <div class="st-layout">
          <!-- 左侧系列列表 -->
          <div class="st-groups">
            <div class="st-groups-head"><strong>任务系列</strong><el-button size="small" @click="addGroup">＋ 新增</el-button></div>
            <div v-for="(g, gi) in subtaskPlan.groups" :key="g.id" class="st-group-item" :class="{ active: activeGroupId === g.id }" @click="activeGroupId = g.id">
              <div class="st-gi-head">
                <span class="st-gi-name">{{ g.name }}</span>
                <span class="st-gi-progress">{{ store.calcGroupProgress(g) }}%</span>
              </div>
              <div class="st-gi-stat">{{ (g.items||[]).length }} 项 · 完成 {{ (g.items||[]).filter(it=>it.status==='done').length }} 项</div>
              <div class="st-gi-date">{{ g.startDate || '—' }} ~ {{ g.endDate || '—' }}</div>
              <div class="st-gi-acts">
                <el-button size="small" text @click.stop="renameGroup(g)">改名</el-button>
                <el-button size="small" text type="danger" @click.stop="deleteGroupWrap(g)">删除</el-button>
                <el-button size="small" text @click.stop="store.reorderGroups(subtaskPlan.id, gi, gi-1)" :disabled="gi===0">↑</el-button>
                <el-button size="small" text @click.stop="store.reorderGroups(subtaskPlan.id, gi, gi+1)" :disabled="gi===subtaskPlan.groups.length-1">↓</el-button>
              </div>
            </div>
            <div v-if="!subtaskPlan.groups?.length" style="color:var(--color-muted);font-size:13px;text-align:center;padding:24px">暂无系列，点击"新增"创建</div>
          </div>
          <!-- 右侧子任务明细表 -->
          <div class="st-items">
            <template v-if="activeGroup">
              <div class="st-items-head">
                <strong>{{ activeGroup.name }} · 子任务明细</strong>
                <span style="font-size:12px;color:var(--color-secondary)">{{ (activeGroup.items||[]).length }} 项，完成 {{ (activeGroup.items||[]).filter(it=>it.status==='done').length }} 项</span>
              </div>
              <div class="st-items-bar">
                <input v-model="newItemTitle" placeholder="输入子任务标题，Enter 添加" style="flex:1;min-width:160px;padding:6px 10px;border:1px solid var(--color-border);border-radius:4px;font-size:13px;outline:none" @keydown.enter.prevent="quickAddItem" />
                <el-button size="small" type="primary" @click="quickAddItem">添加</el-button>
                <el-popover trigger="click" placement="bottom" :width="360" v-model:visible="showBatchPaste">
                  <template #reference><el-button size="small">📋 批量粘贴</el-button></template>
                  <el-input v-model="batchPasteText" type="textarea" :rows="6" size="small" placeholder="每行一个子任务&#10;例如：&#10;完成项目初始化&#10;学习配置文件&#10;掌握依赖注入&#10;完成 REST API&#10;编写单元测试" />
                  <div style="display:flex;gap:8px;margin-top:8px;justify-content:space-between;align-items:center">
                    <span style="font-size:12px;color:var(--color-secondary)">{{ batchPasteText.split('\n').filter(l=>l.trim()).length }} 条待添加</span>
                    <div><el-button size="small" @click="showBatchPaste = false">取消</el-button><el-button size="small" type="primary" @click="batchPaste">确认创建</el-button></div>
                  </div>
                </el-popover>
              </div>
              <el-table :data="activeGroup.items" size="small" stripe style="width:100%" max-height="52vh" row-key="id"
                :header-cell-style="{ background:'#F7F9F6',fontSize:'11px' }" :cell-style="{ fontSize:'12px',padding:'6px 4px' }">
                <el-table-column label="状态" width="90">
                  <template #default="{ row }"><el-select v-model="row.status" size="small" style="width:85px" @change="store.updateItem(subtaskPlan.id, row.id, { status: row.status }); subtaskPlan = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.id)))"><el-option v-for="s in itemStatuses" :key="s.value" :label="s.label" :value="s.value" /></el-select></template>
                </el-table-column>
                <el-table-column label="子任务标题" min-width="200">
                  <template #default="{ row }"><el-input v-model="row.title" size="small" @change="store.updateItem(subtaskPlan.id, row.id, { title: row.title })" /></template>
                </el-table-column>
                <el-table-column label="进度" width="80">
                  <template #default="{ row }"><span :style="{color: row.status==='done'?'#67C23A':'#909399',fontWeight:600}">{{ row.status === 'done' ? '100%' : '0%' }}</span></template>
                </el-table-column>
                <el-table-column label="预计🍅" width="85">
                  <template #default="{ row }"><el-input-number v-model="row.estPomodoros" :min="0" size="small" style="width:72px" @change="store.updateItem(subtaskPlan.id, row.id, { estPomodoros: row.estPomodoros })" /></template>
                </el-table-column>
                <el-table-column label="实际🍅" width="75">
                  <template #default="{ row }"><span :style="{color: (row.actualPomodoros||0)>0?'#67C23A':'#909399'}">{{ row.actualPomodoros || 0 }}</span></template>
                </el-table-column>
                <el-table-column label="权重" width="75">
                  <template #default="{ row }"><el-input-number v-model="row.weight" :min="1" :max="5" size="small" style="width:65px" @change="store.updateItem(subtaskPlan.id, row.id, { weight: row.weight })" /></template>
                </el-table-column>
                <el-table-column label="开始" width="115">
                  <template #default="{ row }"><el-input v-model="row.startDate" type="date" size="small" style="width:105px" @change="store.updateItem(subtaskPlan.id, row.id, { startDate: row.startDate })" /></template>
                </el-table-column>
                <el-table-column label="截止" width="115">
                  <template #default="{ row }"><el-input v-model="row.endDate" type="date" size="small" style="width:105px" @change="store.updateItem(subtaskPlan.id, row.id, { endDate: row.endDate })" /></template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row, $index }">
                    <el-button size="small" text @click="duplicateItem(row)">复制</el-button>
                    <el-button size="small" text @click="store.reorderItems(subtaskPlan.id, activeGroupId, $index, $index-1); subtaskPlan = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.id)))" :disabled="$index===0">↑</el-button>
                    <el-button size="small" text @click="store.reorderItems(subtaskPlan.id, activeGroupId, $index, $index+1); subtaskPlan = JSON.parse(JSON.stringify(store.getPlanById(subtaskPlan.id)))" :disabled="$index===activeGroup.items.length-1">↓</el-button>
                    <el-button size="small" type="danger" text @click="deleteItemConfirm(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </template>
            <div v-else style="color:var(--color-muted);text-align:center;padding:48px">选择左侧任务系列查看子任务</div>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.admin-plans { padding: 20px 24px; }
.ap-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.ap-head h2 { font-size: 20px; font-weight: 700; }
.ap-acts { display: flex; gap: 8px; flex-wrap: wrap; }
.ap-btn { padding: 7px 16px; border: 1px solid var(--color-border); border-radius: 6px; font-size: 13px; background: transparent; cursor: pointer; color: var(--color-secondary); transition: all 0.15s; }
.ap-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.ap-btn.primary { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.ap-tabs { display: flex; gap: 2px; margin-bottom: 12px; border-bottom: 2px solid var(--color-border-light); }
.atab { padding: 7px 14px; border: none; background: transparent; font-size: 13px; color: var(--color-secondary); cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; display: flex; align-items: center; gap: 6px; }
.atab.active { color: var(--color-primary); font-weight: 600; border-bottom-color: var(--color-primary); }
.atab-n { font-size: 11px; color: var(--color-muted); }
.ap-bar { margin-bottom: 12px; }
.ap-table-wrap { background: #fff; border-radius: 6px; border: 1px solid #E4E7ED; overflow: hidden; }
.cat-tag { display: inline-block; padding: 2px 8px; border-radius: 4px; font-weight: 600; }
.plan-name-cell { font-weight: 600; color: #1f3025; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 400px; }
.status-click-tag { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; border: 1px solid; cursor: pointer; white-space: nowrap; transition: opacity 0.15s; }
.status-click-tag:hover { opacity: 0.8; }
.action-cell { display: flex; align-items: center; gap: 8px; }
.menu-icon { width: 18px; height: 18px; display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0; color: inherit; }
</style>

<!-- 全局样式：下拉菜单 Teleport 到 body，scoped 无法覆盖 -->
<style>
.plan-action-menu { width: 116px !important; padding: 6px !important; border-radius: 8px !important; }
.plan-action-menu .el-dropdown-menu__item { height: 40px; padding: 0 14px !important; display: flex; align-items: center; justify-content: flex-start; gap: 8px; line-height: 1; font-size: 13px; border-radius: 4px; margin: 2px 0; }
.plan-action-menu .el-dropdown-menu__item:hover { background: #eaf5ec !important; color: inherit; }
.plan-action-menu .danger-item { color: #d94b4b !important; border-top: 1px solid #edf0ed; margin-top: 4px; padding-top: 10px !important; height: 44px; }
.plan-action-menu .danger-item:hover { background: #fff1f1 !important; color: #c93636 !important; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.form-grid label { display: flex; flex-direction: column; gap: 4px; font-size: 12px; color: var(--color-secondary); }
.form-grid label.span-2 { grid-column: 1 / -1; }

/* 子任务管理 */
.st-head { display: flex; gap: 24px; padding: 12px 16px; background: #F7F9F6; border-radius: 8px; margin-bottom: 16px; font-size: 13px; flex-wrap: wrap; }
.st-layout { display: flex; gap: 16px; height: calc(100vh - 180px); }
.st-groups { width: 240px; flex-shrink: 0; border-right: 1px solid var(--color-border-light); overflow-y: auto; padding-right: 8px; }
.st-groups-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.st-group-item { padding: 12px; border: 1px solid var(--color-border-light); border-radius: 6px; margin-bottom: 6px; cursor: pointer; transition: all 0.15s; }
.st-group-item:hover { border-color: var(--color-primary); }
.st-group-item.active { border-color: var(--color-primary); background: var(--color-primary-light); }
.st-gi-head { display: flex; justify-content: space-between; font-weight: 600; font-size: 13px; }
.st-gi-stat { font-size: 11px; color: var(--color-secondary); margin-top: 2px; }
.st-gi-date { font-size: 10px; color: var(--color-muted); margin-top: 1px; }
.st-gi-acts { display: flex; gap: 2px; margin-top: 6px; flex-wrap: wrap; }
.st-items { flex: 1; overflow-y: auto; min-width: 0; }
.st-items-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; gap: 8px; flex-wrap: wrap; }
.st-items-bar { display: flex; gap: 6px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
.st-items-acts { display: flex; gap: 6px; align-items: center; }
</style>
