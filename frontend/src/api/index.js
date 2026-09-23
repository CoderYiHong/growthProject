import axios from 'axios'

/**
 * Axios 实例 — 模拟 API
 * 后续接入 Spring Boot 时修改 baseURL 即可
 */

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 — 携带 Token，FormData 不强制 json
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token') || sessionStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // FormData 上传时删掉默认的 application/json，让浏览器自动设 multipart boundary
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 — 统一错误处理
api.interceptors.response.use(
  response => response.data,
  error => {
    // 仅 admin 接口返回 401 时才跳转登录页，避免前台公开页被误伤
    if (error.response?.status === 401) {
      const requestUrl = error.config?.url || ''
      if (requestUrl.includes('/admin/') || requestUrl.includes('/auth/')) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_user')
        sessionStorage.removeItem('admin_token')
        sessionStorage.removeItem('admin_user')
        if (window.location.pathname !== '/admin/login') {
          window.location.replace('/admin/login')
        }
      }
    }
    return Promise.reject(error)
  }
)

export default api
