import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/**
 * 全局应用状态
 * 管理主题、侧边栏等全局设置
 */
export const useAppStore = defineStore('app', () => {
  // 主题模式：light | dark
  const theme = ref(localStorage.getItem('app_theme') || 'light')
  // 后台侧边栏折叠
  const sidebarCollapsed = ref(false)

  // 切换主题
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  // 设置主题
  function setTheme(mode) {
    theme.value = mode
  }

  // 切换侧边栏
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 持久化主题
  watch(theme, (val) => {
    localStorage.setItem('app_theme', val)
    document.documentElement.setAttribute('data-theme', val)
  }, { immediate: true })

  return { theme, sidebarCollapsed, toggleTheme, setTheme, toggleSidebar }
})
