/**
 * 通用工具函数
 */

// 格式化日期
export function formatDate(dateStr, format = 'YYYY-MM-DD') {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
}

// 格式化阅读量
export function formatViews(num) {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num)
}

// 防抖
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

// ==================== 后端数据 → 前端格式转换 ====================

const CATEGORY_MAP = {
  mobile: '移动应用',
  frontend: '前端项目',
  backend: '后端项目',
  fullstack: '全栈项目',
  ai: 'AI 工具',
  enterprise: '企业实践'
}

const STATUS_MAP = {
  completed: '已完成',
  in_progress: '开发中',
  planned: '计划中',
  draft: '草稿',
  published: '已发布'
}

const ARTICLE_CATEGORY_MAP = {
  frontend: '前端',
  backend: '后端',
  tools: '工具',
  ai: 'AI',
  devops: 'DevOps',
  mobile: '移动开发',
  general: '综合',
  growth: '成长笔记'
}

/** 兼容 JSON 数组与历史逗号分隔字符串 */
function parseStringList(value) {
  if (Array.isArray(value)) return value
  if (typeof value !== 'string') return []

  const text = value.trim()
  if (!text) return []

  if (text.startsWith('[')) {
    try {
      const parsed = JSON.parse(text)
      if (Array.isArray(parsed)) return parsed
    } catch {
      // 继续按历史逗号分隔格式解析
    }
  }

  return text.split(/[,，]/).map(item => item.trim()).filter(Boolean)
}

function parseJsonValue(value, fallback) {
  if (value === null || value === undefined || value === '') return fallback
  if (typeof value !== 'string') return value
  try {
    return JSON.parse(value)
  } catch {
    return fallback
  }
}

/** 将后端返回的项目数据转为前端格式 */
export function transformProject(p) {
  if (!p) return p
  return {
    ...p,
    isRecommended: p.isRecommended === true || p.isRecommended === 1 || p.isRecommended === '1',
    techStack: parseStringList(p.techStack),
    categoryLabel: CATEGORY_MAP[p.category] || p.category,
    statusLabel: STATUS_MAP[p.status] || p.status,
    createdAt: p.createTime || p.createdAt,
    effectiveCover: p.cover || p.sourceCover || '',
    coverSource: p.cover ? 'custom' : (p.sourceCover ? 'github' : 'placeholder'),
    features: parseStringList(p.features),
    goals: parseStringList(p.goals),
    results: parseStringList(p.results),
    challenges: parseJsonValue(p.challenges, [])
  }
}

/** 将后端返回的文章数据转为前端格式 */
export function transformArticle(a) {
  if (!a) return a
  return {
    ...a,
    isRecommended: a.isRecommended === true || a.isRecommended === 1 || a.isRecommended === '1',
    isHot: a.isHot === true || a.isHot === 1 || a.isHot === '1',
    tags: parseStringList(a.tags),
    categoryLabel: ARTICLE_CATEGORY_MAP[a.category] || a.category || '综合',
    statusLabel: STATUS_MAP[a.status] || a.status,
    createdAt: a.createTime || a.createdAt,
    effectiveCover: a.cover || a.sourceCover || '',
    coverSource: a.cover ? 'custom' : (a.sourceCover ? 'csdn' : 'placeholder')
  }
}

// 节流
export function throttle(fn, delay = 300) {
  let last = 0
  return function (...args) {
    const now = Date.now()
    if (now - last >= delay) {
      last = now
      fn.apply(this, args)
    }
  }
}

// 模拟延迟
export function delay(ms = 500) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// 获取随机颜色（用于占位图）
export function getRandomColor(seed) {
  const colors = ['#4F9D57', '#24463A', '#F0B85A', '#6CB4EE', '#A8D8B9', '#E8D5B7']
  return colors[seed % colors.length]
}

// SVG 图标简单生成器
export function createSvgIcon(name) {
  const icons = {
    seed: '<circle cx="12" cy="18" r="3" fill="#8B6914"/><ellipse cx="12" cy="15" rx="2" ry="3" fill="#A0A0A0"/>',
    sprout: '<path d="M12 20V12M12 12C10 10 8 8 12 4C16 8 14 10 12 12Z" fill="#4F9D57" stroke="#24463A" stroke-width="0.5"/>',
    tree: '<rect x="10" y="14" width="4" height="6" fill="#8B6914" rx="1"/><circle cx="12" cy="10" r="6" fill="#4F9D57"/><circle cx="9" cy="8" r="4" fill="#5DBE6E"/><circle cx="14" cy="9" r="3.5" fill="#3D8B47"/>',
    bigTree: '<rect x="10" y="15" width="5" height="7" fill="#6B4914" rx="1"/><circle cx="12.5" cy="10" r="8" fill="#24463A"/><circle cx="9" cy="7" r="5.5" fill="#3D7B47"/><circle cx="15" cy="8" r="5" fill="#4F9D57"/><circle cx="12" cy="5" r="4" fill="#5DBE6E"/>'
  }
  return icons[name] || ''
}
