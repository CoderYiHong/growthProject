<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { getSiteSettings } from '@/api/public'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const scrolled = ref(false)
const mobileMenuOpen = ref(false)
const siteName = ref('Orange.')

onMounted(async () => {
  try {
    const res = await getSiteSettings()
    if (res.code === 200 && res.data?.siteName) {
      siteName.value = res.data.siteName
    }
  } catch { /* 设置加载失败，使用默认值 */ }
})

// 导航菜单
const navItems = [
  { path: '/', name: 'Home', label: '首页' },
  { path: '/growth', name: 'Growth', label: '成长轨迹' },
  { path: '/projects', name: 'Projects', label: '项目作品' },
  { path: '/skills', name: 'Skills', label: '技术能力' },
  { path: '/plans', name: 'StudyPlans', label: '学习计划' },
  { path: '/timetable', name: 'Timetable', label: '课表' },
  { path: '/articles', name: 'Articles', label: '技术文章' },
  { path: '/about', name: 'About', label: '关于我' },
  { path: '/contact', name: 'Contact', label: '联系我' }
]

// 监听滚动
function onScroll() {
  scrolled.value = window.scrollY > 10
}

// 挂载滚动监听
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// 判断当前路由是否激活
function isActive(item) {
  if (item.path === '/') return route.path === '/'
  return route.path.startsWith(item.path)
}

// 主题切换
function toggleTheme() {
  appStore.toggleTheme()
}

// 关闭移动端菜单
function closeMenu() {
  mobileMenuOpen.value = false
}

// 进入后台
function goAdmin() {
  const token = localStorage.getItem('admin_token')
  if (token) {
    router.push('/admin/dashboard')
  } else {
    router.push('/admin/login')
  }
}
</script>

<template>
  <header class="app-header" :class="{ scrolled }">
    <div class="header-container container-wide">
      <!-- Logo -->
      <router-link to="/" class="logo" @click="closeMenu">
        <div class="logo-icon">
          <svg width="28" height="28" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="#4F9D57"/>
            <path d="M24 8C24 8 16 18 16 26C16 30.4 19.6 34 24 34C28.4 34 32 30.4 32 26C32 18 24 8 24 8Z" fill="white" opacity="0.9"/>
            <circle cx="24" cy="25" r="4" fill="#24463A"/>
            <path d="M24 32V38M20 38H28" stroke="white" stroke-width="2" stroke-linecap="round"/>
            <path d="M32 20L36 14M16 20L12 14" stroke="#F0B85A" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="logo-text">{{ siteName }}<span class="logo-dot">.</span></span>
      </router-link>

      <!-- 桌面导航 -->
      <nav class="nav-desktop">
        <router-link
          v-for="item in navItems"
          :key="item.name"
          :to="item.path"
          class="nav-link"
          :class="{ active: isActive(item) }"
        >
          {{ item.label }}
          <span class="nav-indicator" v-if="isActive(item)"></span>
        </router-link>
      </nav>

      <!-- 右侧操作 -->
      <div class="header-actions">
        <!-- 主题切换 -->
        <button class="icon-btn" @click="toggleTheme" title="切换主题">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="5" v-if="appStore.theme === 'light'" />
            <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" v-if="appStore.theme === 'light'" />
            <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" v-else />
          </svg>
        </button>

        <!-- 后台入口（低调的小图标） -->
        <button class="icon-btn admin-btn" @click="goAdmin" title="管理后台">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>
          </svg>
        </button>

        <!-- 移动端菜单按钮 -->
        <button
          class="mobile-menu-btn icon-btn"
          @click="mobileMenuOpen = !mobileMenuOpen"
          title="菜单"
        >
          <svg v-if="!mobileMenuOpen" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 移动端菜单 -->
    <Transition name="slide-down">
      <nav v-if="mobileMenuOpen" class="nav-mobile">
        <router-link
          v-for="item in navItems"
          :key="item.name"
          :to="item.path"
          class="nav-link-mobile"
          :class="{ active: isActive(item) }"
          @click="closeMenu"
        >
          {{ item.label }}
        </router-link>
        <div class="nav-mobile-divider"></div>
        <button class="nav-link-mobile admin-link" @click="goAdmin">
          管理后台
        </button>
      </nav>
    </Transition>
  </header>
</template>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  z-index: 1000;
  border-bottom: 1px solid transparent;
  transition: all var(--transition-normal);
}

.app-header.scrolled {
  border-bottom-color: var(--color-border);
  box-shadow: var(--shadow-sm);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-title);
  letter-spacing: -0.3px;
}

.logo-dot {
  color: var(--color-primary);
}

/* 桌面导航 */
.nav-desktop {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link {
  position: relative;
  padding: 8px 16px;
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-body);
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-primary);
  background: var(--color-primary-light);
}

.nav-link.active {
  color: var(--color-primary);
}

.nav-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: var(--color-primary);
  border-radius: 1px;
}

/* 右侧操作 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-body);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.icon-btn:hover {
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.mobile-menu-btn {
  display: none;
}

/* 移动端导航 */
.nav-mobile {
  display: none;
  position: fixed;
  top: var(--header-height);
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(12px);
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-md);
  z-index: 999;
}

.nav-link-mobile {
  display: block;
  padding: 12px 16px;
  font-size: var(--font-size-md);
  color: var(--color-body);
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.nav-link-mobile:hover,
.nav-link-mobile.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.nav-mobile-divider {
  height: 1px;
  background: var(--color-border);
  margin: 8px 0;
}

.admin-link {
  color: var(--color-secondary);
}

/* 动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.25s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-desktop {
    display: none;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .nav-mobile {
    display: block;
  }
}
</style>
