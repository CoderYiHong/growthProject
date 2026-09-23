import api from './index'

/**
 * 后台管理 API — 需要登录（自动携带 Token）
 */

// ==================== 登录 ====================
export function login(username, password) {
  return api.post('/auth/login', { username, password })
}

// ==================== 仪表盘 ====================
export function getDashboardStats() {
  return api.get('/admin/dashboard/stats')
}

export function getDashboardTrend() {
  return api.get('/admin/dashboard/trend')
}

export function getDashboardCategories() {
  return api.get('/admin/dashboard/categories')
}

// ==================== 项目管理 ====================
export function getAdminProjects(params = {}) {
  return api.get('/admin/projects', { params })
}

export function getAdminProjectDetail(id) {
  return api.get(`/admin/projects/${id}`)
}

export function createProject(data) {
  return api.post('/admin/projects', data)
}

export function updateProject(id, data) {
  return api.put(`/admin/projects/${id}`, data)
}

export function deleteProject(id) {
  return api.delete(`/admin/projects/${id}`)
}

export function syncGithubProjects() {
  return api.post('/admin/projects/sync/github', null, { timeout: 30000 })
}

// ==================== 文章管理 ====================
export function getAdminArticles(params = {}) {
  return api.get('/admin/articles', { params })
}

export function getAdminArticleDetail(id) {
  return api.get(`/admin/articles/${id}`)
}

export function createArticle(data) {
  return api.post('/admin/articles', data)
}

export function updateArticle(id, data) {
  return api.put(`/admin/articles/${id}`, data)
}

export function deleteArticle(id) {
  return api.delete(`/admin/articles/${id}`)
}

export function syncCsdnArticles() {
  return api.post('/admin/articles/sync/csdn', null, { timeout: 90000 })
}

// ==================== 成长经历管理 ====================
export function getAdminGrowthStages() {
  return api.get('/admin/growth')
}

export function createGrowthStage(data) {
  return api.post('/admin/growth', data)
}

export function updateGrowthStage(id, data) {
  return api.put(`/admin/growth/${id}`, data)
}

export function deleteGrowthStage(id) {
  return api.delete(`/admin/growth/${id}`)
}

export function getAdminCertificates() { return api.get('/admin/certificates') }
export function createCertificate(data) { return api.post('/admin/certificates', data) }
export function updateCertificate(id, data) { return api.put(`/admin/certificates/${id}`, data) }
export function deleteCertificate(id) { return api.delete(`/admin/certificates/${id}`) }
export function getCertificates() { return api.get('/certificates') }

// ==================== 技能管理 ====================
export function getAdminSkills() {
  return api.get('/admin/skills')
}

export function createSkill(data) {
  return api.post('/admin/skills', data)
}

export function updateSkill(id, data) {
  return api.put(`/admin/skills/${id}`, data)
}

export function deleteSkill(id) {
  return api.delete(`/admin/skills/${id}`)
}

// ==================== 留言管理 ====================
export function getAdminMessages(params = {}) {
  return api.get('/admin/messages', { params })
}

export function getMessageStats() {
  return api.get('/admin/messages/stats')
}

export function updateMessageStatus(id, status) {
  return api.put(`/admin/messages/${id}/status`, null, { params: { status } })
}

export function deleteMessage(id) {
  return api.delete(`/admin/messages/${id}`)
}

// ==================== 操作日志 ====================
export function getAdminLogs(params = {}) {
  return api.get('/admin/logs', { params })
}

export function getAdminLogModules() {
  return api.get('/admin/logs/modules')
}

// ==================== 网站设置 ====================
export function getSiteSettings() {
  return api.get('/admin/settings')
}

export function saveSiteSettings(data) {
  return api.put('/admin/settings', data)
}

// ==================== 个人设置 ====================
export function getAdminProfile() {
  return api.get('/admin/profile')
}

export function updateAdminProfile(data) {
  return api.put('/admin/profile', data)
}

export function changePassword(data) {
  return api.put('/admin/profile/password', data)
}

// ==================== 文件上传 ====================
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/admin/upload', formData)
}

// ==================== 学习计划 ====================
export function getStudyPlans() {
  return api.get('/admin/study-plans')
}

export function createStudyPlan(data) {
  return api.post('/admin/study-plans', data)
}

export function updateStudyPlan(id, data) {
  return api.put(`/admin/study-plans/${id}`, data)
}

export function deleteStudyPlan(id) {
  return api.delete(`/admin/study-plans/${id}`)
}

export function getPlanGroups(planId) {
  return api.get(`/admin/study-plans/${planId}/groups`)
}

export function createPlanGroup(planId, data) {
  return api.post(`/admin/study-plans/${planId}/groups`, data)
}

export function updatePlanGroup(planId, groupId, data) {
  return api.put(`/admin/study-plans/${planId}/groups/${groupId}`, data)
}

export function deletePlanGroup(planId, groupId) {
  return api.delete(`/admin/study-plans/${planId}/groups/${groupId}`)
}

export function getPlanItems(planId, groupId) {
  return api.get(`/admin/study-plans/${planId}/items`, { params: { groupId } })
}

export function createPlanItem(planId, data) {
  return api.post(`/admin/study-plans/${planId}/items`, data)
}

export function updatePlanItem(planId, itemId, data) {
  return api.put(`/admin/study-plans/${planId}/items/${itemId}`, data)
}

export function deletePlanItem(planId, itemId) {
  return api.delete(`/admin/study-plans/${planId}/items/${itemId}`)
}

export function getPlanCheckins(planId) {
  return api.get(`/admin/study-plans/${planId}/checkins`)
}

export function createPlanCheckin(planId, data) {
  return api.post(`/admin/study-plans/${planId}/checkins`, data)
}

export function importStudyPlans(data) {
  return api.post('/admin/study-plans/import', data)
}

// ==================== 课表 ====================
export function getAdminTimetable() {
  return api.get('/admin/timetable')
}

export function createTimetableCourse(data) {
  return api.post('/admin/timetable/courses', data)
}

export function updateTimetableCourse(id, data) {
  return api.put(`/admin/timetable/courses/${id}`, data)
}

export function deleteTimetableCourse(id) {
  return api.delete(`/admin/timetable/courses/${id}`)
}

export function createTimetableEntry(data) {
  return api.post('/admin/timetable/entries', data)
}

export function updateTimetableEntry(id, data) {
  return api.put(`/admin/timetable/entries/${id}`, data)
}

export function deleteTimetableEntry(id) {
  return api.delete(`/admin/timetable/entries/${id}`)
}

export function importTimetable(data) {
  return api.post('/admin/timetable/import', data)
}
