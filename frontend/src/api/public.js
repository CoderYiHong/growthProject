import api from './index'

/**
 * 前台公开 API — 不需要登录
 */

// 项目
export function getProjects() {
  return api.get('/public/projects')
}

export function getProjectDetail(id) {
  return api.get(`/public/projects/${id}`)
}

// 文章
export function getArticles() {
  return api.get('/public/articles')
}

export function getArticleDetail(id) {
  return api.get(`/public/articles/${id}`)
}

// 成长轨迹
export function getGrowthStages() {
  return api.get('/public/growth')
}

// 技能
export function getSkills() {
  return api.get('/public/skills')
}

// 网站设置（公开，供 Header/Footer 使用）
export function getSiteSettings() {
  return api.get('/public/settings')
}

// 课表
export function getTimetable() {
  return api.get('/public/timetable')
}

// 统计数据
export function getPublicStats() {
  return api.get('/public/stats')
}

// 在线简历（聚合接口，替代前端 mock）
export function getResumeData() {
  return api.get('/public/resume')
}
