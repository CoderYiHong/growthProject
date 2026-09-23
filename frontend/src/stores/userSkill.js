import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useStudyPlanStore } from './studyPlan'

/** 技能领域 */
export const SKILL_DOMAINS = [
  '前端开发', '后端开发', '数据库与中间件', '移动开发', 'DevOps', '数据分析', '人工智能'
]
/** 技能等级 */
export const SKILL_LEVELS = ['了解', '入门', '熟悉', '掌握', '熟练']
/** 技能状态 */
export const SKILL_STATUSES = ['学习中', '已解锁', '已归档']

const LS_KEY = 'user_skills_v1'

function loadSkills() {
  try { const r = localStorage.getItem(LS_KEY); if (r) return JSON.parse(r) } catch(e){}
  return []
}
function saveSkills(data) { localStorage.setItem(LS_KEY, JSON.stringify(data)) }

/** 默认种子技能 */
const SEED_SKILLS = [
  { id: 'sk_vue3', name: 'Vue3', domain: '前端开发', icon: '🟢' },
  { id: 'sk_js', name: 'JavaScript', domain: '前端开发', icon: '🟨' },
  { id: 'sk_html', name: 'HTML/CSS', domain: '前端开发', icon: '🟧' },
  { id: 'sk_java', name: 'Java', domain: '后端开发', icon: '☕' },
  { id: 'sk_spring', name: 'Spring Boot', domain: '后端开发', icon: '🍃' },
  { id: 'sk_mybatis', name: 'MyBatis-Plus', domain: '后端开发', icon: '🗄' },
  { id: 'sk_mysql', name: 'MySQL', domain: '数据库与中间件', icon: '🐬' },
  { id: 'sk_redis', name: 'Redis', domain: '数据库与中间件', icon: '🔴' },
  { id: 'sk_harmony', name: 'HarmonyOS', domain: '移动开发', icon: '📱' },
  { id: 'sk_arkts', name: 'ArkTS', domain: '移动开发', icon: '🔷' },
  { id: 'sk_python', name: 'Python', domain: '数据分析', icon: '🐍' },
  { id: 'sk_ai', name: 'AI 自动化', domain: '人工智能', icon: '🤖' },
  { id: 'sk_git', name: 'Git 协作', domain: 'DevOps', icon: '🔀' }
]

export const useUserSkillStore = defineStore('userSkill', () => {
  const skills = ref(loadSkills())
  const planStore = useStudyPlanStore()

  function persist() { saveSkills(skills.value) }

  function getById(id) { return skills.value.find(s => s.id === id) }

  // ── 从学习计划同步技能 ──
  function syncFromPlans() {
    const now = new Date().toISOString()
    for (const plan of planStore.plans) {
      if (!plan.groups) continue
      for (const group of plan.groups) {
        if (!group.skillMapping) continue
        const mapping = group.skillMapping
        const groupDone = (group.items || []).filter(it => it.status !== 'skipped').every(it => it.status === 'done')
        if (!groupDone) continue

        // 查找或创建技能
        let skill = getById(mapping.skillId)
        if (!skill) {
          const seed = SEED_SKILLS.find(s => s.id === mapping.skillId)
          skill = {
            id: mapping.skillId || 'sk_' + Date.now(),
            name: mapping.skillName || seed?.name || '未命名技能',
            domain: mapping.domain || seed?.domain || '其他',
            icon: seed?.icon || '📌',
            status: '已解锁',
            level: '了解',
            totalMinutes: 0,
            linkedPlans: [],
            evidence: [],
            createdAt: now, updatedAt: now, archivedAt: null
          }
          skills.value.push(skill)
        }

        // 幂等检查：同一 group 不重复
        const evKey = `${plan.id}_${group.id}`
        const existing = skill.evidence.find(e => e.sourceType === 'group' && e.sourceId === evKey)
        if (!existing) {
          const groupMins = planStore.checkins
            .filter(c => c.planId === plan.id && c.groupId === group.id)
            .reduce((s, c) => s + (c.duration || 0), 0)
          skill.evidence.push({
            sourceType: 'group', sourceId: evKey,
            planId: plan.id, planName: plan.name,
            groupId: group.id, groupName: group.name,
            minutes: groupMins, completedAt: now
          })
          if (!skill.linkedPlans.find(p => p.planId === plan.id)) {
            skill.linkedPlans.push({ planId: plan.id, planName: plan.name, groupCount: 1 })
          } else {
            const lp = skill.linkedPlans.find(p => p.planId === plan.id)
            lp.groupCount = (lp.groupCount || 0) + 1
          }
          skill.totalMinutes = skill.evidence.reduce((s, e) => s + e.minutes, 0)
          skill.updatedAt = now
          if (skill.status === '学习中') skill.status = '已解锁'
          // 根据完成模块数量更新等级
          const doneCount = skill.evidence.length
          if (doneCount >= 1 && skill.level === '了解') skill.level = '入门'
          if (doneCount >= 3 && skill.level === '入门') skill.level = '熟悉'
          if (doneCount >= 6 && skill.level === '熟悉') skill.level = '掌握'
        }
      }
    }
    persist()
  }

  // ── 后台手动添加 ──
  function addSkill(data) {
    const id = 'sk_' + Date.now()
    skills.value.push({
      id, name: data.name, domain: data.domain, icon: data.icon || '📌',
      status: data.status || '学习中', level: data.level || '了解',
      totalMinutes: 0, linkedPlans: [], evidence: [],
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), archivedAt: null
    })
    persist(); return id
  }

  function updateSkill(id, data) {
    const sk = getById(id); if (!sk) return
    Object.assign(sk, data, { updatedAt: new Date().toISOString() })
    persist()
  }

  function archiveSkill(id) {
    const sk = getById(id); if (!sk) return
    sk.status = '已归档'; sk.archivedAt = new Date().toISOString(); sk.updatedAt = new Date().toISOString()
    persist()
  }

  function unlinkEvidence(skillId, evSourceId) {
    const sk = getById(skillId); if (!sk) return
    sk.evidence = sk.evidence.filter(e => e.sourceId !== evSourceId)
    sk.totalMinutes = sk.evidence.reduce((s, e) => s + e.minutes, 0)
    if (sk.evidence.length === 0) sk.status = '学习中'
    // 重新计算等级
    const done = sk.evidence.length
    if (done < 1) sk.level = '了解'
    else if (done < 3) sk.level = '入门'
    else if (done < 6) sk.level = '熟悉'
    else sk.level = '掌握'
    sk.updatedAt = new Date().toISOString()
    persist()
  }

  // ── 统计 ──
  const totalSkills = computed(() => skills.value.filter(s => s.status !== '已归档').length)
  const learningCount = computed(() => skills.value.filter(s => s.status === '学习中').length)
  const unlockedCount = computed(() => skills.value.filter(s => s.status === '已解锁' && s.level !== '熟练').length)
  const proficientCount = computed(() => skills.value.filter(s => s.level === '掌握' || s.level === '熟练').length)
  const totalSkillMins = computed(() => skills.value.reduce((s, sk) => s + sk.totalMinutes, 0))

  // 初始化时同步
  syncFromPlans()

  return {
    skills, getById, syncFromPlans,
    addSkill, updateSkill, archiveSkill, unlinkEvidence,
    totalSkills, learningCount, unlockedCount, proficientCount, totalSkillMins, persist
  }
})
