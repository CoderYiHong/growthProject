/**
 * 在线简历 Mock 数据
 *
 * 所有数据通过 resume API 层获取，页面组件不直接引用本文件。
 * 接入 Spring Boot 后删除本文件，改为从后端接口获取数据。
 *
 * 排序规则：sort 值越小越靠前
 * 可见性：visible === false 的不在前台展示
 * 推荐优先：featured === true 的排在同类前面
 */

export const resumeData = {
  profile: {
    name: 'Orange',
    avatar: '',
    position: '全栈开发工程师',
    degree: '本科',
    major: '数据科学与大数据技术',
    politicalStatus: '中共党员',
    location: '中国',
    email: '',
    phone: '',
    github: 'https://github.com/CoderOrange',
    csdn: 'https://blog.csdn.net/Yihong1833100198?type=blog',
    introduction: '我是一名数据科学与大数据技术专业的本科生，技术方向为全栈开发、数据分析和人工智能应用。热爱技术分享，持续撰写技术博客，具备从需求分析到项目交付的完整开发能力。拥有华为 HCIP 应用开发认证，获得过国家励志奖学金等多项荣誉。目前专注于 Vue3 + Spring Boot 全栈开发，同时深入学习 Python 数据分析与 AI 自动化技术。',
    technicalDirection: [
      '全栈开发',
      '数据分析',
      '人工智能应用'
    ]
  },

  abilities: [
    {
      id: 1,
      title: '全栈开发',
      icon: 'code',
      description: '具备 Web 全栈开发能力，掌握 Vue3 前端框架和 Spring Boot 后端框架，能够独立完成从数据库设计到前端页面的完整开发流程。',
      sort: 1,
      visible: true
    },
    {
      id: 2,
      title: '移动开发',
      icon: 'mobile',
      description: '熟练掌握 HarmonyOS 应用开发，使用 ArkTS + ArkUI 完成多个上架应用，深入理解移动端 UI 设计、数据持久化和网络通信。',
      sort: 2,
      visible: true
    },
    {
      id: 3,
      title: '数据分析',
      icon: 'data',
      description: '掌握 Python 数据分析基础，能够使用 Pandas、NumPy 进行数据处理与分析，具备数据可视化和基本机器学习应用能力。',
      sort: 3,
      visible: true
    },
    {
      id: 4,
      title: 'AI 应用',
      icon: 'ai',
      description: '熟练使用大语言模型进行智能应用开发，能够将 AI 能力集成到实际项目中，提升开发效率和产品质量。',
      sort: 4,
      visible: true
    },
    {
      id: 5,
      title: '项目管理',
      icon: 'product',
      description: '具备需求分析、功能拆解、项目文档撰写和团队协作能力，能够推动项目从规划到交付的全过程。',
      sort: 5,
      visible: true
    },
    {
      id: 6,
      title: '技术写作',
      icon: 'write',
      description: '持续撰写技术博客，在 CSDN 等平台累计发布 100+ 篇技术文章，善于将复杂技术知识转化为清晰易懂的内容。',
      sort: 6,
      visible: true
    }
  ],

  skills: [
    // 前端开发
    { id: 1, category: '前端开发', name: 'HTML / CSS', level: '掌握', percentage: 85, sort: 1, visible: true },
    { id: 2, category: '前端开发', name: 'JavaScript', level: '掌握', percentage: 80, sort: 2, visible: true },
    { id: 3, category: '前端开发', name: 'Vue3', level: '掌握', percentage: 78, sort: 3, visible: true },
    { id: 4, category: '前端开发', name: 'Element Plus', level: '掌握', percentage: 80, sort: 4, visible: true },
    { id: 5, category: '前端开发', name: 'ECharts', level: '熟悉', percentage: 65, sort: 5, visible: true },

    // 后端开发
    { id: 6, category: '后端开发', name: 'Java', level: '掌握', percentage: 75, sort: 1, visible: true },
    { id: 7, category: '后端开发', name: 'Spring Boot', level: '掌握', percentage: 70, sort: 2, visible: true },
    { id: 8, category: '后端开发', name: 'MyBatis-Plus', level: '熟悉', percentage: 65, sort: 3, visible: true },
    { id: 9, category: '后端开发', name: 'MySQL', level: '掌握', percentage: 72, sort: 4, visible: true },
    { id: 10, category: '后端开发', name: 'RESTful API', level: '掌握', percentage: 78, sort: 5, visible: true },

    // 移动开发
    { id: 11, category: '移动开发', name: 'HarmonyOS', level: '掌握', percentage: 80, sort: 1, visible: true },
    { id: 12, category: '移动开发', name: 'ArkTS', level: '掌握', percentage: 78, sort: 2, visible: true },
    { id: 13, category: '移动开发', name: 'ArkUI', level: '掌握', percentage: 75, sort: 3, visible: true },

    // 数据与 AI
    { id: 14, category: '数据与 AI', name: 'Python', level: '熟悉', percentage: 55, sort: 1, visible: true },
    { id: 15, category: '数据与 AI', name: '数据分析', level: '熟悉', percentage: 50, sort: 2, visible: true },
    { id: 16, category: '数据与 AI', name: 'AI 自动化', level: '熟悉', percentage: 55, sort: 3, visible: true },

    // 工具与协作
    { id: 17, category: '工具与协作', name: 'Git', level: '掌握', percentage: 82, sort: 1, visible: true },
    { id: 18, category: '工具与协作', name: 'Markdown', level: '掌握', percentage: 88, sort: 2, visible: true },
    { id: 19, category: '工具与协作', name: 'VS Code', level: '掌握', percentage: 85, sort: 3, visible: true }
  ],

  projects: [
    {
      id: 1,
      name: 'QingHeAccountBook · 青禾记账',
      cover: '',
      summary: '基于 HarmonyOS 的个人账单管理 APP，支持收支记录、分类统计和预算管理。',
      responsibility: '独立开发，负责全部前端 UI、业务逻辑和数据持久化。',
      technologies: ['HarmonyOS', 'ArkTS', 'ArkUI', 'TypeScript'],
      previewUrl: '#',
      repositoryUrl: 'https://github.com/CoderOrange/QingHeAccountBook',
      featured: true,
      visible: true,
      sort: 1
    },
    {
      id: 2,
      name: 'ClassMS · 班级管理系统',
      cover: '',
      summary: '基于 Vue3 + Node.js 的班级信息管理系统，包含学生管理、课程管理、成绩分析、课表管理等功能。',
      responsibility: '全栈开发，负责数据库设计、API 开发和前端页面实现。',
      technologies: ['Vue3', 'Vite', 'Node.js', 'JavaScript'],
      previewUrl: '#',
      repositoryUrl: 'https://github.com/CoderOrange/ClassMS',
      featured: true,
      visible: true,
      sort: 2
    },
    {
      id: 3,
      name: 'Orange. 个人成长作品集',
      cover: '',
      summary: '基于 Vue3 + Vite 构建的个人品牌网站，展示技术能力、项目作品、成长轨迹和技术文章。',
      responsibility: '独立开发，负责完整的前端架构设计、组件开发和响应式适配。',
      technologies: ['Vue3', 'Vite', 'Element Plus', 'ECharts', 'Pinia'],
      previewUrl: '#',
      repositoryUrl: 'https://github.com/CoderOrange',
      featured: true,
      visible: true,
      sort: 3
    },
    {
      id: 4,
      name: 'AI 需求文档生成工具',
      cover: '',
      summary: '利用大语言模型自动生成标准化软件需求文档，支持需求输入、文档模板定制和在线编辑。',
      responsibility: '全栈开发，负责前端交互设计、AI 接口集成和文档渲染引擎。',
      technologies: ['Vue3', 'Python', 'DeepSeek', 'Claude Code'],
      previewUrl: '#',
      repositoryUrl: 'https://github.com/CoderOrange',
      featured: false,
      visible: true,
      sort: 4
    },
    {
      id: 5,
      name: 'js-study · JavaScript 学习笔记',
      cover: '',
      summary: '系统性 JavaScript 学习代码仓库，涵盖 ES6+ 语法、异步编程、DOM 操作和常用设计模式。',
      responsibility: '学习记录，整理知识点并编写可运行的示例代码。',
      technologies: ['JavaScript', 'HTML', 'ES6+'],
      previewUrl: '#',
      repositoryUrl: 'https://github.com/CoderOrange/js-study',
      featured: false,
      visible: true,
      sort: 5
    }
  ],

  certificates: [
    {
      id: 1,
      name: 'HCIP 应用开发认证',
      category: '厂商认证',
      issuingOrganization: '华为',
      issueDate: '2025-06',
      certificateNumber: 'HCIP-APP-202506001234',
      imageUrl: '',
      fileUrl: '',
      description: '华为认证 ICT 专家级应用开发工程师，证明持证人具备 HarmonyOS 应用开发的高级能力，能够独立完成复杂应用的设计与开发。',
      publicCertificateNumber: false,
      featured: true,
      visible: true,
      sort: 1
    },
    {
      id: 2,
      name: '国家励志奖学金',
      category: '奖学金',
      issuingOrganization: '',
      issueDate: '2025-01',
      certificateNumber: '',
      imageUrl: '',
      fileUrl: '',
      description: '国家励志奖学金获奖证明，表彰学业成绩优秀且品学兼优的学生。',
      publicCertificateNumber: false,
      featured: false,
      visible: false,
      sort: 2
    },
    {
      id: 3,
      name: 'CET-4 大学英语四级',
      category: '语言能力',
      issuingOrganization: '教育部教育考试院',
      issueDate: '2024-06',
      certificateNumber: '',
      imageUrl: '',
      fileUrl: '',
      description: '全国大学英语四级考试合格证明，具备良好的英语阅读和写作能力。',
      publicCertificateNumber: false,
      featured: true,
      visible: true,
      sort: 3
    },
    {
      id: 4,
      name: '普通话水平测试二级甲等',
      category: '语言能力',
      issuingOrganization: '国家语委',
      issueDate: '2024-03',
      certificateNumber: '',
      imageUrl: '',
      fileUrl: '',
      description: '普通话水平测试二级甲等证书，具备标准、流利的普通话表达能力。',
      publicCertificateNumber: false,
      featured: false,
      visible: true,
      sort: 4
    }
  ],

  honors: [
    {
      id: 1,
      name: '国家励志奖学金',
      level: '国家级',
      issuingOrganization: '',
      issueDate: '2025-01',
      imageUrl: '',
      fileUrl: '',
      description: '表彰学业成绩优秀且品学兼优的学生，综合测评排名前列。',
      featured: true,
      visible: true,
      sort: 1
    },
    {
      id: 2,
      name: '华为 HCIP 应用开发认证',
      level: '厂商认证',
      issuingOrganization: '华为技术有限公司',
      issueDate: '2025-06',
      imageUrl: '',
      fileUrl: '',
      description: '通过华为 HCIP 认证考试，具备 HarmonyOS 应用开发专业能力。',
      featured: true,
      visible: true,
      sort: 2
    },
    {
      id: 3,
      name: 'CSDN 技术博客认证作者',
      level: '平台认证',
      issuingOrganization: 'CSDN',
      issueDate: '2025-03',
      imageUrl: '',
      fileUrl: '',
      description: '在 CSDN 平台累计发布 100+ 篇技术文章，获得平台优质创作者认证。',
      featured: false,
      visible: true,
      sort: 3
    },
    {
      id: 4,
      name: '优秀学生干部',
      level: '校级',
      issuingOrganization: '',
      issueDate: '2024-12',
      imageUrl: '',
      fileUrl: '',
      description: '表彰在学生工作中表现突出的学生干部。',
      featured: false,
      visible: true,
      sort: 4
    }
  ],

  contact: {
    email: '',
    github: 'https://github.com/CoderOrange',
    csdn: 'https://blog.csdn.net/Yihong1833100198?type=blog',
    location: '中国'
  },

  // 元数据（后台预留）
  meta: {
    lastUpdateTime: '2025-07-15',
    version: '1.0.0'
  }
}

/**
 * 辅助函数：对列表数据排序（featured 优先 + sort 升序），并过滤 visible
 */
export function sortAndFilter(list) {
  return list
    .filter(item => item.visible !== false)
    .sort((a, b) => {
      // featured 优先
      if (a.featured && !b.featured) return -1
      if (!a.featured && b.featured) return 1
      // sort 升序
      return (a.sort || 0) - (b.sort || 0)
    })
}

/**
 * 辅助函数：对技能按 category 分组
 */
export function groupSkills(skills) {
  const grouped = {}
  const visible = skills.filter(s => s.visible !== false).sort((a, b) => a.sort - b.sort)
  visible.forEach(skill => {
    if (!grouped[skill.category]) grouped[skill.category] = []
    grouped[skill.category].push(skill)
  })
  return Object.entries(grouped).map(([category, items]) => ({ category, items }))
}
