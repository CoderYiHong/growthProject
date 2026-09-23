import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/admin'

/**
 * 认证状态管理
 * 调用后端 /api/auth/login 真实登录
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || sessionStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(
    localStorage.getItem('admin_user') || sessionStorage.getItem('admin_user') || 'null'
  ))

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)

  // 真实登录 — 调用后端接口
  async function login(username, password, remember = false) {
    // 调用后端 /api/auth/login
    const res = await loginApi(username, password)

    // 后端返回 { code: 200, message: '登录成功', data: { token: 'xxx' } }
    if (res.code === 200) {
      const authToken = res.data.token
      const user = {
        username: username,
        nickname: res.data.nickname || 'YiHong',
        email: res.data.email || '',
        role: res.data.role || 'admin'
      }

      token.value = authToken
      userInfo.value = user

      if (remember) {
        sessionStorage.removeItem('admin_token')
        sessionStorage.removeItem('admin_user')
        localStorage.setItem('admin_token', authToken)
        localStorage.setItem('admin_user', JSON.stringify(user))
      } else {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_user')
        sessionStorage.setItem('admin_token', authToken)
        sessionStorage.setItem('admin_user', JSON.stringify(user))
      }

      return user
    } else {
      throw new Error(res.message || '登录失败')
    }
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    sessionStorage.removeItem('admin_token')
    sessionStorage.removeItem('admin_user')
  }

  return { token, userInfo, isLoggedIn, login, logout }
})
