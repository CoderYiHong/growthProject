/**
 * 成长记录 Mock 数据
 * 记录学习、项目、问题解决等成长活动
 */
export const growthRecords = [
  {
    id: 1,
    date: '2026-07-20',
    type: 'learning',
    category: '技术学习',
    title: 'Redis 缓存策略学习',
    description: '学习了 Redis 的五种基本数据类型及其适用场景，理解了缓存穿透、缓存击穿和缓存雪崩的概念与解决方案。使用 Spring Boot 集成 Redis 完成了缓存注解的配置。',
    tags: ['Redis', 'Spring Boot', '缓存'],
    duration: 180,
    outcome: '掌握了 Redis 基本操作和 Spring Cache 注解使用'
  },
  {
    id: 2,
    date: '2026-07-19',
    type: 'problem',
    category: '技术学习',
    title: '解决 MyBatis-Plus 分页插件不生效问题',
    description: '在项目中使用分页查询时发现返回的是全部数据而非分页结果。排查后发现是 MyBatisPlusConfig 中 PaginationInnerInterceptor 的 DbType 配置错误，修正后分页正常。',
    tags: ['MyBatis-Plus', 'Debug', '分页'],
    duration: 45,
    outcome: '理解了 MyBatis-Plus 分页插件的工作原理，形成了排查思路'
  },
  {
    id: 3,
    date: '2026-07-18',
    type: 'project',
    category: '技术学习',
    title: '完成 Spring Security JWT 认证模块',
    description: '实现了基于 JWT 的用户认证流程：登录接口生成 Token、拦截器校验 Token、全局异常处理认证失败。替换了原有的简易内存 Token 方案。',
    tags: ['Spring Security', 'JWT', '认证'],
    duration: 240,
    outcome: '完成了认证模块的升级，Token 支持过期和刷新机制'
  },
  {
    id: 4,
    date: '2026-07-17',
    type: 'learning',
    category: '考研学习',
    title: '线性代数第三章：向量空间',
    description: '学习了向量空间的定义、线性相关与线性无关、基与维数、坐标变换。完成了课后习题 1-15 题。',
    tags: ['线性代数', '向量空间'],
    duration: 150,
    outcome: '掌握了向量空间的核心概念和计算方法'
  },
  {
    id: 5,
    date: '2026-07-16',
    type: 'learning',
    category: '考研学习',
    title: '概率论第二章：随机变量及其分布',
    description: '复习了离散型和连续型随机变量的分布函数、概率密度函数。重点练习了正态分布、指数分布的相关计算题。',
    tags: ['概率论', '随机变量'],
    duration: 120,
    outcome: '能够独立完成常见分布的计算题'
  },
  {
    id: 6,
    date: '2026-07-15',
    type: 'article',
    category: '自媒体',
    title: '发表《Spring Boot 入门系列：从零搭建 RESTful API》',
    description: '撰写并发布了 Spring Boot 入门系列的第二篇文章，介绍了如何使用 Spring Boot 快速搭建 RESTful API，包括项目初始化、Controller 编写、参数校验和统一响应格式。',
    tags: ['Spring Boot', '技术写作', 'RESTful API'],
    duration: 180,
    outcome: '文章发布后获得 200+ 阅读和 15 个收藏'
  },
  {
    id: 7,
    date: '2026-07-14',
    type: 'learning',
    category: '英语学习',
    title: '考研英语阅读理解精练',
    description: '完成了 2018 年真题阅读理解 Text 1-2 的精读分析。逐句翻译、标注生词、分析长难句结构、总结出题规律。',
    tags: ['英语', '阅读理解', '真题'],
    duration: 90,
    outcome: '掌握了细节题和主旨题的解题技巧'
  },
  {
    id: 8,
    date: '2026-07-13',
    type: 'project',
    category: '技术学习',
    title: '个人成长平台前端 V2 升级启动',
    description: '对个人网站进行 V2 版本升级规划：重新设计首页布局、增加成长记录模块、优化移动端体验、统一视觉风格。完成了首页 Hero 区域的改版设计稿。',
    tags: ['Vue3', '前端开发', 'UI设计'],
    duration: 120,
    outcome: '完成了 V2 升级的技术方案和设计稿'
  },
  {
    id: 9,
    date: '2026-07-12',
    type: 'learning',
    category: '考研学习',
    title: '高等数学错题整理与复习',
    description: '整理了近两周高数学习中出错的题目（共 23 道），按知识点分类：极限计算 8 题、导数应用 7 题、积分计算 8 题。重新做了一遍并分析了错误原因。',
    tags: ['高等数学', '错题整理'],
    duration: 150,
    outcome: '明确了薄弱环节，下一步重点突破积分计算'
  },
  {
    id: 10,
    date: '2026-07-11',
    type: 'learning',
    category: '英语学习',
    title: '英语词汇记忆与长难句练习',
    description: '使用 Anki 完成每日词汇复习 150 词、新学 50 词。练习了 5 个考研英语典型长难句的结构分析和翻译。',
    tags: ['英语', '词汇', '长难句'],
    duration: 60,
    outcome: '词汇记忆保持率 85%，长难句分析速度提升'
  }
]

/** 成长记录类型 */
export const recordTypes = [
  { value: 'all', label: '全部' },
  { value: 'learning', label: '学习记录' },
  { value: 'project', label: '项目进展' },
  { value: 'problem', label: '问题解决' },
  { value: 'article', label: '文章输出' }
]

/** 成长方向 */
export const growthDirections = [
  { value: 'all', label: '全部方向' },
  { value: '考研学习', label: '考研学习' },
  { value: '技术学习', label: '技术学习' },
  { value: '英语学习', label: '英语学习' },
  { value: '自媒体', label: '自媒体运营' }
]

/** 时间段 */
export const timePeriods = [
  { value: 'all', label: '全部时间' },
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' },
  { value: 'quarter', label: '本季度' }
]
