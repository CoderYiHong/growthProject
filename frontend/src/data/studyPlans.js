/**
 * 学习计划表 — 数据模型 v3
 * 层级：分类 → 计划 → 任务系列(group) → 子任务(item)
 */
export const STUDY_PLAN_VERSION = 3

/** ── 分类工作表 ── */
export const planSheets = [
  { key: 'all',        label: '全部计划', icon: '📋' },
  { key: '技术学习',   label: '技术学习', icon: '💻' },
  { key: '考研学习',   label: '考研学习', icon: '📚' },
  { key: '英语学习',   label: '英语学习', icon: '🌐' },
  { key: '自媒体运营', label: '自媒体运营', icon: '✍️' }
]

/** ── 状态 ── */
export const planStatuses = [
  { value: 'pending', label: '待开始',   color: '#409EFF', bg: '#ECF5FF' },
  { value: 'active',  label: '进行中',   color: '#E6A23C', bg: '#FDF6EC' },
  { value: 'paused',  label: '暂时搁置', color: '#9C6ADE', bg: '#F5F0FC' },
  { value: 'done',    label: '已完成',   color: '#67C23A', bg: '#F0F9EB' },
  { value: 'overdue', label: '已逾期',   color: '#F56C6C', bg: '#FEF0F0' }
]

export const itemStatuses = [
  { value: 'pending',  label: '待开始', color: '#409EFF' },
  { value: 'active',   label: '进行中', color: '#E6A23C' },
  { value: 'done',     label: '已完成', color: '#67C23A' },
  { value: 'paused',   label: '暂停',   color: '#9C6ADE' },
  { value: 'skipped',  label: '已跳过', color: '#909399' }
]

/** ── 技术分类 ── */
export const techCategories = [
  { value: '基础', label: '基础', color: '#409EFF' },
  { value: '技术线', label: '技术线', color: '#67C23A' },
  { value: '项目', label: '项目', color: '#E6A23C' },
  { value: '工具', label: '工具', color: '#9C6ADE' }
]

/** ── 考研科目 ── */
export const examSubjects = [
  { value: '数学', label: '数学', color: '#409EFF' },
  { value: '英语', label: '英语', color: '#9C6ADE' },
  { value: '政治', label: '政治', color: '#F56C6C' },
  { value: '专业课', label: '专业课', color: '#67C23A' }
]

/** ── 知识点类型（技术学习） ── */
export const knowledgeTypes = ['概念理解', '代码实践', '架构设计', '调试排错', '文档阅读']

/** ── 掌握程度 ── */
export const masteryLevels = ['了解', '理解', '掌握', '熟练', '精通']

// ──────────────────────────────────────
//  默认数据
// ──────────────────────────────────────

export const defaultStudyPlans = [
  {
    id: 'sp_001', category: '技术学习', type: '技术线',
    name: 'Spring Boot 微服务实战', status: 'active',
    startDate: '2026-07-01', endDate: '2026-08-15',
    plannedHours: 60, actualHours: 17.5, importance: 4, progress: 35,
    lastStudyTime: '2026-07-20T14:30:00',
    note: '重点学习 Spring Security、Redis 集成和 Docker 部署',
    groups: [
      {
        id: 'gr_001', name: 'Spring Security 系列', note: '认证与授权模块',
        order: 1, startDate: '2026-07-01', endDate: '2026-07-15', weight: 1,
        items: [
          { id: 'si_001', title: '完成认证模块', note: '', status: 'done', startDate: '2026-07-01', endDate: '2026-07-05', estPomodoros: 4, actualPomodoros: 4, weight: 1, resourceLink: '', completionCriteria: '通过 Postman 测试所有认证接口', order: 1, allowCheckin: true, knowledgeType: '代码实践', needCode: true, repoUrl: '', mastery: '掌握' },
          { id: 'si_002', title: '完成权限控制', note: 'RBAC 模型', status: 'done', startDate: '2026-07-06', endDate: '2026-07-10', estPomodoros: 3, actualPomodoros: 3, weight: 1, resourceLink: '', completionCriteria: '角色权限测试全部通过', order: 2, allowCheckin: true, knowledgeType: '代码实践', needCode: true, repoUrl: '', mastery: '掌握' },
          { id: 'si_003', title: '编写测试', note: '单元测试 + 集成测试', status: 'pending', startDate: '2026-07-11', endDate: '2026-07-15', estPomodoros: 2, actualPomodoros: 0, weight: 1, resourceLink: '', completionCriteria: '覆盖率 > 80%', order: 3, allowCheckin: true, knowledgeType: '代码实践', needCode: true, repoUrl: '', mastery: '熟练' }
        ]
      },
      {
        id: 'gr_002', name: 'Redis 缓存集成', note: '缓存策略与性能优化',
        order: 2, startDate: '2026-07-16', endDate: '2026-07-31', weight: 1,
        items: [
          { id: 'si_004', title: 'Redis 基础配置', note: '', status: 'pending', startDate: '2026-07-16', endDate: '2026-07-20', estPomodoros: 3, actualPomodoros: 0, weight: 1, resourceLink: 'https://redis.io/docs/', completionCriteria: '', order: 1, allowCheckin: true, knowledgeType: '概念理解', needCode: false, repoUrl: '', mastery: '理解' },
          { id: 'si_005', title: '缓存策略实现', note: '穿透/击穿/雪崩', status: 'pending', startDate: '2026-07-21', endDate: '2026-07-31', estPomodoros: 5, actualPomodoros: 0, weight: 2, resourceLink: '', completionCriteria: '压测通过', order: 2, allowCheckin: true, knowledgeType: '代码实践', needCode: true, repoUrl: '', mastery: '掌握' }
        ]
      }
    ],
    createdAt: '2026-07-01T09:00:00', updatedAt: '2026-07-20T14:30:00'
  },
  {
    id: 'sp_002', category: '考研学习', type: '数学',
    name: '高等数学第一轮复习', status: 'active',
    startDate: '2026-06-15', endDate: '2026-09-30',
    plannedHours: 120, actualHours: 42, importance: 5, progress: 45,
    lastStudyTime: '2026-07-19T16:00:00',
    subject: '数学', chapters: '第1-6章',
    questionsDone: 85, questionsTotal: 200, accuracy: 72, studyRound: 1,
    note: '重点突破积分计算和微分方程',
    groups: [
      {
        id: 'gr_101', name: '第一章 函数与极限', note: '基础概念', order: 1,
        startDate: '2026-06-15', endDate: '2026-06-30', weight: 1,
        items: [
          { id: 'si_101', title: '看课程视频', note: 'B站宋浩', status: 'done', startDate: '2026-06-15', endDate: '2026-06-18', estPomodoros: 6, actualPomodoros: 6, weight: 1, resourceLink: '', completionCriteria: '', order: 1, allowCheckin: true, examQuestionsTotal: 30, examQuestionsDone: 30, examCorrect: 25, examWrong: 5, examAccuracy: 83, needMemorize: false, examYear: '2024' },
          { id: 'si_102', title: '完成例题', note: '课本例题', status: 'done', startDate: '2026-06-19', endDate: '2026-06-22', estPomodoros: 4, actualPomodoros: 4, weight: 1, resourceLink: '', completionCriteria: '', order: 2, allowCheckin: true, examQuestionsTotal: 20, examQuestionsDone: 20, examCorrect: 18, examWrong: 2, examAccuracy: 90, needMemorize: false, examYear: '' },
          { id: 'si_103', title: '完成习题', note: '课后习题1-15', status: 'done', startDate: '2026-06-23', endDate: '2026-06-28', estPomodoros: 6, actualPomodoros: 5, weight: 1, resourceLink: '', completionCriteria: '', order: 3, allowCheckin: true, examQuestionsTotal: 15, examQuestionsDone: 15, examCorrect: 11, examWrong: 4, examAccuracy: 73, needMemorize: false, examYear: '' },
          { id: 'si_104', title: '整理错题', note: '等价无穷小类 4 题', status: 'done', startDate: '2026-06-29', endDate: '2026-06-30', estPomodoros: 2, actualPomodoros: 2, weight: 1, resourceLink: '', completionCriteria: '', order: 4, allowCheckin: true, examQuestionsTotal: 4, examQuestionsDone: 4, examCorrect: 4, examWrong: 0, examAccuracy: 100, needMemorize: true, examYear: '' }
        ]
      },
      {
        id: 'gr_102', name: '第二章 导数与微分', note: '', order: 2,
        startDate: '2026-07-01', endDate: '2026-07-15', weight: 1,
        items: [
          { id: 'si_105', title: '看课程视频', note: '', status: 'done', startDate: '2026-07-01', endDate: '2026-07-04', estPomodoros: 5, actualPomodoros: 5, weight: 1, resourceLink: '', completionCriteria: '', order: 1, allowCheckin: true, examQuestionsTotal: 0, examQuestionsDone: 0, examCorrect: 0, examWrong: 0, examAccuracy: 0, needMemorize: false, examYear: '' },
          { id: 'si_106', title: '完成习题', note: '导数应用题', status: 'pending', startDate: '2026-07-05', endDate: '2026-07-15', estPomodoros: 8, actualPomodoros: 0, weight: 2, resourceLink: '', completionCriteria: '', order: 2, allowCheckin: true, examQuestionsTotal: 25, examQuestionsDone: 0, examCorrect: 0, examWrong: 0, examAccuracy: 0, needMemorize: false, examYear: '' }
        ]
      }
    ],
    createdAt: '2026-06-15T08:00:00', updatedAt: '2026-07-19T16:00:00'
  },
  {
    id: 'sp_003', category: '英语学习', type: '词汇',
    name: '考研英语词汇与阅读', status: 'active',
    startDate: '2026-07-01', endDate: '2026-11-01',
    plannedHours: 80, actualHours: 12, importance: 4, progress: 20,
    lastStudyTime: '2026-07-20T09:00:00',
    note: '每日 100 词汇 + 2 篇阅读理解精读',
    groups: [
      { id: 'gr_201', name: '词汇积累', note: '核心词汇 3000', order: 1, startDate: '2026-07-01', endDate: '2026-09-01', weight: 3,
        items: [
          { id: 'si_201', title: '高频词 1000', note: '', status: 'pending', startDate: '2026-07-01', endDate: '2026-07-20', estPomodoros: 10, actualPomodoros: 2, weight: 1, resourceLink: '', completionCriteria: '', order: 1, allowCheckin: true },
          { id: 'si_202', title: '中频词 1000', note: '', status: 'pending', startDate: '2026-07-21', endDate: '2026-08-10', estPomodoros: 10, actualPomodoros: 0, weight: 1, resourceLink: '', completionCriteria: '', order: 2, allowCheckin: true },
          { id: 'si_203', title: '低频词 1000', note: '', status: 'pending', startDate: '2026-08-11', endDate: '2026-09-01', estPomodoros: 10, actualPomodoros: 0, weight: 1, resourceLink: '', completionCriteria: '', order: 3, allowCheckin: true }
        ] },
      { id: 'gr_202', name: '阅读理解', note: '真题精读', order: 2, startDate: '2026-07-15', endDate: '2026-11-01', weight: 2,
        items: [
          { id: 'si_204', title: '真题精读 50 篇', note: '2015-2025', status: 'pending', startDate: '2026-07-15', endDate: '2026-11-01', estPomodoros: 25, actualPomodoros: 0, weight: 2, resourceLink: '', completionCriteria: '每篇正确率 > 80%', order: 1, allowCheckin: true }
        ] }
    ],
    createdAt: '2026-07-01T08:00:00', updatedAt: '2026-07-20T09:00:00'
  },
  {
    id: 'sp_004', category: '技术学习', type: '项目',
    name: '个人成长平台 V3', status: 'active',
    startDate: '2026-07-10', endDate: '2026-08-20',
    plannedHours: 40, actualHours: 8, importance: 3, progress: 25,
    lastStudyTime: '2026-07-18T20:00:00',
    note: '前端架构升级',
    groups: [
      { id: 'gr_301', name: '前端规划', note: '', order: 1, startDate: '2026-07-10', endDate: '2026-07-31', weight: 1,
        items: [
          { id: 'si_301', title: '技术方案设计', note: '', status: 'done', startDate: '2026-07-10', endDate: '2026-07-12', estPomodoros: 3, actualPomodoros: 3, weight: 1, resourceLink: '', completionCriteria: '', order: 1, allowCheckin: true, knowledgeType: '架构设计', needCode: false },
          { id: 'si_302', title: '学习计划表前端', note: '', status: 'pending', startDate: '2026-07-13', endDate: '2026-07-31', estPomodoros: 10, actualPomodoros: 0, weight: 2, resourceLink: '', completionCriteria: '', order: 2, allowCheckin: true, knowledgeType: '代码实践', needCode: true }
        ] }
    ],
    createdAt: '2026-07-10T10:00:00', updatedAt: '2026-07-18T20:00:00'
  }
]

/** ── 默认打卡记录 ── */
export const defaultCheckins = [
  { id: 'ck_001', planId: 'sp_001', groupId: 'gr_001', itemId: 'si_001', content: 'Security 配置类编写', note: '理解了 Filter Chain', duration: 90, date: '2026-07-15T11:00:00' },
  { id: 'ck_002', planId: 'sp_001', groupId: 'gr_001', itemId: 'si_002', content: 'JWT 签发与验证', note: '', duration: 120, date: '2026-07-17T15:00:00' },
  { id: 'ck_003', planId: 'sp_001', groupId: null, itemId: null, content: 'Security+JWT 整合复习', note: '', duration: 60, date: '2026-07-20T14:30:00' },
  { id: 'ck_004', planId: 'sp_002', groupId: 'gr_101', itemId: 'si_101', content: '函数与极限课程', note: '', duration: 90, date: '2026-07-16T10:00:00' },
  { id: 'ck_005', planId: 'sp_002', groupId: 'gr_101', itemId: 'si_102', content: '导数复习', note: '', duration: 75, date: '2026-07-18T14:00:00' }
]
