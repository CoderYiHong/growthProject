import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { defaultStudyPlans, defaultCheckins, STUDY_PLAN_VERSION } from '@/data/studyPlans'

const LS_PLANS_KEY = 'study_plans_v3'
const LS_CHECKINS_KEY = 'study_checkins_v3'
const LS_VERSION_KEY = 'study_plan_version'

function load(key, fallback) {
  try { const r = localStorage.getItem(key); if (r) return JSON.parse(r) } catch (e) {/**/}
  return JSON.parse(JSON.stringify(fallback))
}
function save(key, data) { localStorage.setItem(key, JSON.stringify(data)) }

export const useStudyPlanStore = defineStore('studyPlan', () => {
  // 版本迁移
  const v = localStorage.getItem(LS_VERSION_KEY)
  if (!v || Number(v) < STUDY_PLAN_VERSION) localStorage.setItem(LS_VERSION_KEY, String(STUDY_PLAN_VERSION))

  const plans = ref(load(LS_PLANS_KEY, defaultStudyPlans))
  const checkins = ref(load(LS_CHECKINS_KEY, defaultCheckins))

  function persist() { save(LS_PLANS_KEY, plans.value); save(LS_CHECKINS_KEY, checkins.value) }

  // ── 查询 ──
  function getPlanById(id) { return plans.value.find(p => p.id === id) || null }
  function getGroupById(planId, groupId) { const p = getPlanById(planId); return p?.groups?.find(g => g.id === groupId) || null }
  function getItemById(planId, itemId) {
    const p = getPlanById(planId); if (!p?.groups) return null
    for (const g of p.groups) { const it = g.items?.find(i => i.id === itemId); if (it) return it }
    return null
  }
  function getCheckinsByPlan(planId) { return checkins.value.filter(c => c.planId === planId).sort((a, b) => new Date(b.date) - new Date(a.date)) }

  // ── 进度计算 ──
  function calcGroupProgress(group) {
    if (!group.items || group.items.length === 0) return 0
    const valid = group.items.filter(it => it.status !== 'skipped')
    if (valid.length === 0) return 0
    const totalW = valid.reduce((s, it) => s + (it.weight || 1), 0)
    const doneW = valid.filter(it => it.status === 'done').reduce((s, it) => s + (it.weight || 1), 0)
    return Math.round((doneW / totalW) * 100)
  }

  function calcPlanProgress(plan) {
    if (!plan.groups || plan.groups.length === 0) return plan.progress || 0
    let totalW = 0, doneW = 0
    for (const g of plan.groups) {
      const valid = (g.items || []).filter(it => it.status !== 'skipped')
      const gw = g.weight || 1
      const itTotal = valid.reduce((s, it) => s + (it.weight || 1), 0)
      const itDone = valid.filter(it => it.status === 'done').reduce((s, it) => s + (it.weight || 1), 0)
      if (itTotal > 0) {
        totalW += gw
        doneW += gw * (itDone / itTotal)
      }
    }
    return totalW > 0 ? Math.round((doneW / totalW) * 100) : 0
  }

  function syncPlanStatus(plan) {
    const hasCheckins = checkins.value.some(c => c.planId === plan.id)
    const allDone = plan.groups?.every(g => (g.items || []).every(it => it.status === 'done'))
    if (allDone && plan.status !== 'done') plan.status = 'done'
    else if (!allDone && hasCheckins && plan.status === 'pending') plan.status = 'active'
  }

  // ── Plan CRUD ──
  function addPlan(plan) { plans.value.push({ ...plan, groups: plan.groups || [], actualHours: plan.actualHours || 0, progress: 0 }); persist() }
  function updatePlan(id, data) {
    const idx = plans.value.findIndex(p => p.id === id); if (idx === -1) return
    plans.value[idx] = { ...plans.value[idx], ...data, updatedAt: new Date().toISOString() }
    plans.value[idx].progress = calcPlanProgress(plans.value[idx])
    syncPlanStatus(plans.value[idx])
    persist()
  }
  function deletePlan(id) { plans.value = plans.value.filter(p => p.id !== id); checkins.value = checkins.value.filter(c => c.planId !== id); persist() }
  function copyPlan(id) {
    const src = getPlanById(id); if (!src) return
    const copy = JSON.parse(JSON.stringify(src))
    copy.id = 'sp_' + Date.now(); copy.name = src.name + ' (副本)'; copy.status = 'pending'
    copy.progress = 0; copy.actualHours = 0; copy.lastStudyTime = null
    copy.createdAt = new Date().toISOString(); copy.updatedAt = copy.createdAt
    // 重置所有 item 状态
    if (copy.groups) for (const g of copy.groups) {
      g.id = 'gr_' + Date.now() + '_' + Math.random().toString(36).slice(2, 6)
      if (g.items) for (const it of g.items) { it.id = 'si_' + Date.now() + '_' + Math.random().toString(36).slice(2, 6); it.status = 'pending'; it.actualPomodoros = 0 }
    }
    plans.value.push(copy); persist(); return copy
  }

  // ── Group CRUD ──
  function addGroup(planId, group) {
    const p = getPlanById(planId); if (!p) return
    if (!p.groups) p.groups = []
    p.groups.push({ id: 'gr_' + Date.now(), name: group.name || '新系列', note: '', order: p.groups.length + 1, startDate: group.startDate || '', endDate: group.endDate || '', weight: group.weight || 1, items: [] })
    p.updatedAt = new Date().toISOString(); persist()
  }
  function updateGroup(planId, groupId, data) {
    const g = getGroupById(planId, groupId); if (!g) return
    Object.assign(g, data)
    const p = getPlanById(planId); p.updatedAt = new Date().toISOString(); p.progress = calcPlanProgress(p)
    persist()
  }
  function deleteGroup(planId, groupId, deleteItems = false) {
    const p = getPlanById(planId); if (!p) return
    const g = p.groups.find(gr => gr.id === groupId); if (!g) return
    if (g.items?.length && !deleteItems) return false // 需要确认
    p.groups = p.groups.filter(gr => gr.id !== groupId)
    if (deleteItems && g.items) for (const it of g.items) { checkins.value = checkins.value.filter(c => c.itemId !== it.id) }
    p.updatedAt = new Date().toISOString(); p.progress = calcPlanProgress(p)
    persist(); return true
  }
  function reorderGroups(planId, fromIdx, toIdx) {
    const p = getPlanById(planId); if (!p?.groups) return
    const item = p.groups.splice(fromIdx, 1)[0]; p.groups.splice(toIdx, 0, item)
    p.groups.forEach((g, i) => g.order = i + 1); p.updatedAt = new Date().toISOString(); persist()
  }
  function moveItemsToGroup(planId, fromGroupId, toGroupId, itemIds) {
    const fromG = getGroupById(planId, fromGroupId); const toG = getGroupById(planId, toGroupId)
    if (!fromG || !toG) return
    const moved = fromG.items.filter(it => itemIds.includes(it.id))
    fromG.items = fromG.items.filter(it => !itemIds.includes(it.id))
    toG.items.push(...moved.map(it => ({ ...it, order: toG.items.length + 1 })))
    const p = getPlanById(planId); p.updatedAt = new Date().toISOString(); p.progress = calcPlanProgress(p)
    persist()
  }

  // ── Item CRUD ──
  function addItem(planId, groupId, item) {
    const g = getGroupById(planId, groupId); if (!g) return
    if (!g.items) g.items = []
    g.items.push({ id: 'si_' + Date.now(), title: item.title || '', note: item.note || '', status: 'pending', startDate: item.startDate || '', endDate: item.endDate || '', estPomodoros: item.estPomodoros || 0, actualPomodoros: 0, weight: item.weight || 1, resourceLink: item.resourceLink || '', completionCriteria: item.completionCriteria || '', order: g.items.length + 1, allowCheckin: item.allowCheckin !== false, knowledgeType: item.knowledgeType || '', needCode: item.needCode || false, repoUrl: item.repoUrl || '', mastery: item.mastery || '', examQuestionsTotal: item.examQuestionsTotal || 0, examQuestionsDone: 0, examCorrect: 0, examWrong: 0, examAccuracy: 0, needMemorize: item.needMemorize || false, examYear: item.examYear || '' })
    const p = getPlanById(planId); p.updatedAt = new Date().toISOString(); p.progress = calcPlanProgress(p)
    persist()
  }
  function batchAddItems(planId, groupId, titles) {
    const g = getGroupById(planId, groupId); if (!g || !titles.length) return
    for (const t of titles) addItem(planId, groupId, { title: t })
  }
  function updateItem(planId, itemId, data) {
    const it = getItemById(planId, itemId); if (!it) return
    Object.assign(it, data)
    if (data.examQuestionsDone !== undefined && it.examQuestionsTotal > 0) {
      it.examAccuracy = Math.round((it.examCorrect || 0) / it.examQuestionsTotal * 100)
    }
    const p = getPlanById(planId); p.updatedAt = new Date().toISOString(); p.progress = calcPlanProgress(p)
    syncPlanStatus(p); persist()
  }
  function deleteItem(planId, itemId) {
    const p = getPlanById(planId); if (!p?.groups) return
    for (const g of p.groups) g.items = (g.items || []).filter(it => it.id !== itemId)
    p.updatedAt = new Date().toISOString(); p.progress = calcPlanProgress(p); persist()
  }
  function reorderItems(planId, groupId, fromIdx, toIdx) {
    const g = getGroupById(planId, groupId); if (!g?.items) return
    const item = g.items.splice(fromIdx, 1)[0]; g.items.splice(toIdx, 0, item)
    g.items.forEach((it, i) => it.order = i + 1)
    const p = getPlanById(planId); p.updatedAt = new Date().toISOString(); persist()
  }
  function batchUpdateItems(planId, itemIds, data) {
    for (const id of itemIds) updateItem(planId, id, data)
  }

  // ── 打卡 ──
  function addCheckin(rec) {
    const chk = { id: 'ck_' + Date.now(), planId: rec.planId, groupId: rec.groupId || null, itemId: rec.itemId || null, content: rec.content || '', note: rec.note || '', duration: rec.duration || 0, date: rec.date || new Date().toISOString() }
    checkins.value.push(chk)
    const p = getPlanById(rec.planId)
    if (p) {
      p.actualHours = (p.actualHours || 0) + (rec.duration || 0) / 60
      p.lastStudyTime = chk.date
      if (rec.itemId) {
        const it = getItemById(rec.planId, rec.itemId)
        if (it) { it.actualPomodoros = (it.actualPomodoros || 0) + 1; if (rec.markDone) it.status = 'done' }
      }
      p.progress = calcPlanProgress(p); syncPlanStatus(p); p.updatedAt = new Date().toISOString()
    }
    persist(); return chk
  }
  function updateCheckin(cid, data) {
    const chk = checkins.value.find(c => c.id === cid); if (!chk) return
    const oldDur = chk.duration || 0; Object.assign(chk, data)
    const p = getPlanById(chk.planId)
    if (p) { p.actualHours = Math.max(0, (p.actualHours || 0) - oldDur / 60 + (data.duration || oldDur) / 60); p.updatedAt = new Date().toISOString() }
    persist()
  }
  function deleteCheckin(cid) {
    const chk = checkins.value.find(c => c.id === cid); if (!chk) return
    const p = getPlanById(chk.planId)
    if (p) { p.actualHours = Math.max(0, (p.actualHours || 0) - (chk.duration || 0) / 60); p.updatedAt = new Date().toISOString() }
    checkins.value = checkins.value.filter(c => c.id !== cid); persist()
  }

  // ── 统计 ──
  const todayCheckins = computed(() => { const t = new Date().toISOString().slice(0, 10); return checkins.value.filter(c => c.date?.startsWith(t)) })
  const todayMinutes = computed(() => todayCheckins.value.reduce((s, c) => s + (c.duration || 0), 0))

  // ── 导入导出 ──
  function importPlans(importData) { for (const p of importData) { const ex = plans.value.findIndex(e => e.id === p.id); if (ex >= 0) plans.value[ex] = { ...plans.value[ex], ...p, updatedAt: new Date().toISOString() }; else plans.value.push({ ...p, groups: p.groups || [], actualHours: p.actualHours || 0 }) }; persist() }
  function exportPlans() { return JSON.parse(JSON.stringify(plans.value)) }

  // ── 后端同步（可选，在管理后台调用） ──
  let _api = null
  function setApi(apiModule) { _api = apiModule }

  async function pullFromBackend() {
    if (!_api) return
    try {
      const res = await _api.getStudyPlans()
      if (res.code === 200 && res.data?.length) {
        // 合并后端数据到本地：以后端 id 匹配
        const backendPlans = res.data.map(p => ({
          ...p,
          id: 'sp_' + p.id,
          groups: [],
          lastStudyTime: p.lastStudyTime || null,
          createdAt: p.createdAt,
          updatedAt: p.updatedAt
        }))
        for (const bp of backendPlans) {
          const existing = plans.value.find(lp => lp.id === bp.id)
          if (!existing) {
            plans.value.push(bp)
          }
        }
        persist()
      }
    } catch (e) { /* 后端不可用时使用本地数据 */ }
  }

  async function pushToBackend(plan) {
    if (!_api) return
    const numericId = plan.id?.startsWith('sp_') ? Number(plan.id.slice(3)) : plan.id
    try {
      if (!numericId || isNaN(numericId)) {
        const res = await _api.createStudyPlan({
          category: plan.category, type: plan.type, name: plan.name,
          status: plan.status, startDate: plan.startDate, endDate: plan.endDate,
          plannedHours: plan.plannedHours, importance: plan.importance, note: plan.note
        })
        if (res.code === 200 && res.data) {
          plan.id = 'sp_' + res.data.id
          persist()
        }
      } else {
        await _api.updateStudyPlan(numericId, {
          category: plan.category, type: plan.type, name: plan.name,
          status: plan.status, startDate: plan.startDate, endDate: plan.endDate,
          plannedHours: plan.plannedHours, actualHours: plan.actualHours,
          progress: plan.progress, importance: plan.importance, note: plan.note
        })
      }
    } catch (e) { /* 同步失败不影响本地 */ }
  }

  async function deleteFromBackend(planId) {
    if (!_api) return
    const numericId = planId?.startsWith('sp_') ? Number(planId.slice(3)) : planId
    if (!numericId || isNaN(numericId)) return
    try { await _api.deleteStudyPlan(numericId) } catch (e) { /* ignore */ }
  }

  return {
    plans, checkins, todayMinutes, todayCheckins,
    getPlanById, getGroupById, getItemById, getCheckinsByPlan,
    calcGroupProgress, calcPlanProgress, syncPlanStatus,
    addPlan, updatePlan, deletePlan, copyPlan,
    addGroup, updateGroup, deleteGroup, reorderGroups, moveItemsToGroup,
    addItem, batchAddItems, updateItem, deleteItem, reorderItems, batchUpdateItems,
    addCheckin, updateCheckin, deleteCheckin,
    importPlans, exportPlans, persist,
    setApi, pullFromBackend, pushToBackend, deleteFromBackend
  }
})
