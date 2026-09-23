/**
 * 当前学习计划 Mock 数据
 * 展示正在进行的成长方向、阶段和投入时间
 */
export const learningPlans = [
  {
    id: 1,
    direction: '考研学习',
    icon: 'book',
    currentPhase: '数学基础复习',
    progress: 45,
    weeklyHours: 14,
    recentTasks: [
      { name: '线性代数第三章习题', status: 'done', date: '2026-07-19' },
      { name: '概率论第二章复习', status: 'in_progress', date: '2026-07-20' },
      { name: '高数错题整理', status: 'pending', date: '2026-07-21' }
    ],
    goals: ['完成数学第一轮复习', '英语词汇量达到 6000', '专业课教材通读一遍']
  },
  {
    id: 2,
    direction: '技术学习',
    icon: 'code',
    currentPhase: 'Spring Boot 微服务实战',
    progress: 60,
    weeklyHours: 10,
    recentTasks: [
      { name: 'Spring Security 认证模块', status: 'done', date: '2026-07-18' },
      { name: 'Redis 缓存集成', status: 'in_progress', date: '2026-07-20' },
      { name: 'Docker 容器化部署', status: 'pending', date: '2026-07-22' }
    ],
    goals: ['完成 Spring Boot 全栈项目', '掌握 Redis 缓存策略', '学习 Docker 与 CI/CD']
  },
  {
    id: 3,
    direction: '英语学习',
    icon: 'lang',
    currentPhase: '考研英语阅读理解',
    progress: 35,
    weeklyHours: 8,
    recentTasks: [
      { name: '每日词汇记忆 100 词', status: 'done', date: '2026-07-20' },
      { name: '阅读理解真题 2 篇', status: 'in_progress', date: '2026-07-20' },
      { name: '长难句分析练习', status: 'pending', date: '2026-07-21' }
    ],
    goals: ['考研英语词汇量 6000+', '阅读理解正确率 80%+', '写作模板整理']
  },
  {
    id: 4,
    direction: '自媒体运营',
    icon: 'media',
    currentPhase: '技术博客与内容输出',
    progress: 50,
    weeklyHours: 6,
    recentTasks: [
      { name: 'Spring Boot 系列文章第 3 篇', status: 'done', date: '2026-07-17' },
      { name: '项目复盘文章撰写', status: 'in_progress', date: '2026-07-20' },
      { name: '技术分享 PPT 制作', status: 'pending', date: '2026-07-23' }
    ],
    goals: ['每月输出 4 篇技术文章', 'CSDN 粉丝突破 1000', '建立个人技术品牌']
  }
]

/** 阶段总结数据 */
export const weeklySummary = {
  week: '2026 年第 29 周（7.14 - 7.20）',
  completedItems: [
    '完成 Spring Security 认证模块学习与代码实践',
    '线性代数第三章全部习题',
    '发表 1 篇技术博客（Spring Boot 入门系列）',
    '英语阅读理解 10 篇',
    '重构个人网站首页组件'
  ],
  totalLearningHours: 38,
  projectCount: 2,
  nextWeekGoals: [
    '开始 Redis 缓存模块学习',
    '完成概率论第二轮复习',
    '发表第 2 篇 Spring Boot 系列文章',
    '搭建 Docker 本地开发环境'
  ]
}

export const monthlySummary = {
  month: '2026 年 7 月',
  completedItems: [
    'Spring Security 完整学习并实践',
    '数学第一轮复习进度达 45%',
    '输出 3 篇技术文章',
    '个人网站完成 V2 升级',
    '英语词汇量增加 400 词'
  ],
  totalLearningHours: 152,
  projectCount: 5,
  articleCount: 3,
  nextMonthGoals: [
    '完成 Spring Boot 微服务实战项目',
    '数学复习进度推进到 70%',
    '开始学习 Docker & K8s',
    '输出 4 篇技术文章'
  ]
}
