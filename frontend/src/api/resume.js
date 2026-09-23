/**
 * 在线简历 API 层
 *
 * 从后端 /api/public/resume 聚合接口获取数据。
 * 各辅助函数仍由前端调用，与页面组件 API 完全兼容。
 */

import api from './index'

// 缓存简历数据，避免重复请求
let cachedData = null

/** 获取完整简历数据（带缓存） */
async function fetchResumeData() {
  if (cachedData) return cachedData
  const res = await api.get('/public/resume')
  if (res.code === 200 && res.data) {
    cachedData = res.data
    return cachedData
  }
  throw new Error(res.message || '获取简历数据失败')
}

/** 获取完整简历数据 */
export function getResumeData() {
  return fetchResumeData().then(data => ({ code: 200, message: '请求成功', data }))
}

/** 获取个人信息 */
export function getProfile() {
  return fetchResumeData().then(data => ({ code: 200, message: '请求成功', data: data.profile }))
}

/** 获取核心能力 */
export function getAbilities() {
  return fetchResumeData().then(data => ({ code: 200, message: '请求成功', data: data.abilities || [] }))
}

/** 获取专业技能（按分类分组，兼容旧格式） */
export function getSkills() {
  return fetchResumeData().then(data => {
    const skills = data.skills || []
    // 分组
    const grouped = {}
    skills.forEach(skill => {
      const cat = skill.category || '其他'
      if (!grouped[cat]) grouped[cat] = []
      grouped[cat].push(skill)
    })
    const groups = Object.entries(grouped).map(([category, items]) => ({ category, items }))
    return { code: 200, message: '请求成功', data: groups }
  })
}

/** 获取代表项目 */
export function getProjects() {
  return fetchResumeData().then(data => ({ code: 200, message: '请求成功', data: data.projects || [] }))
}

/** 获取证书资质 */
export function getCertificates() {
  return fetchResumeData().then(data => ({ code: 200, message: '请求成功', data: data.certificates || [] }))
}

/** 获取个人荣誉 */
export function getHonors() {
  return fetchResumeData().then(data => ({ code: 200, message: '请求成功', data: data.honors || [] }))
}

/** 获取联系方式 */
export function getContact() {
  return fetchResumeData().then(data => ({ code: 200, message: '请求成功', data: data.contact || {} }))
}
