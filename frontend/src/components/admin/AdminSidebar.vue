<script setup>
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

defineProps({
  collapsed: { type: Boolean, default: false }
})

const emit = defineEmits(['logout'])
const route = useRoute()
const appStore = useAppStore()

// 菜单配置
const menuGroups = [
  {
    title: '',
    items: [
      { path: '/admin/dashboard', label: '数据概览', icon: 'dashboard' }
    ]
  },
  {
    title: '内容管理',
    items: [
      { path: '/admin/study-plans', label: '学习计划', icon: 'plans' },
      { path: '/admin/timetable', label: '课表管理', icon: 'timetable' },
      { path: '/admin/projects', label: '项目管理', icon: 'projects' },
      { path: '/admin/articles', label: '文章管理', icon: 'articles' },
      { path: '/admin/growth', label: '成长经历', icon: 'growth' },
      { path: '/admin/skills', label: '技能管理', icon: 'skills' },
      { path: '/admin/certificates', label: '证书资质', icon: 'certs' }
    ]
  },
  {
    title: '互动管理',
    items: [
      { path: '/admin/messages', label: '留言管理', icon: 'messages' }
    ]
  },
  {
    title: '系统管理',
    items: [
      { path: '/admin/settings', label: '网站设置', icon: 'settings' },
      { path: '/admin/profile', label: '个人设置', icon: 'profile' },
      { path: '/admin/logs', label: '操作日志', icon: 'logs' }
    ]
  }
]

function isActive(path) {
  if (path === '/admin/dashboard') return route.path === '/admin/dashboard'
  return route.path.startsWith(path)
}

function toggleSidebar() {
  appStore.toggleSidebar()
}
</script>

<template>
  <aside class="admin-sidebar" :class="{ collapsed }">
    <!-- Logo -->
    <div class="sidebar-logo">
      <router-link to="/admin/dashboard" class="sidebar-logo-link">
        <svg width="28" height="28" viewBox="0 0 48 48" fill="none">
          <rect width="48" height="48" rx="12" fill="#4F9D57"/>
          <path d="M24 8C24 8 16 18 16 26C16 30.4 19.6 34 24 34C28.4 34 32 30.4 32 26C32 18 24 8 24 8Z" fill="white" opacity="0.9"/>
          <circle cx="24" cy="25" r="4" fill="#24463A"/>
        </svg>
        <span v-show="!collapsed" class="sidebar-logo-text">Orange<span style="color: #F0B85A">.</span></span>
      </router-link>
    </div>

    <!-- 菜单 -->
    <nav class="sidebar-nav">
      <div v-for="group in menuGroups" :key="group.title" class="menu-group">
        <div v-if="group.title && !collapsed" class="menu-group-title">{{ group.title }}</div>
        <router-link
          v-for="item in group.items"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
          :title="collapsed ? item.label : ''"
        >
          <span class="menu-item-icon" v-html="getIcon(item.icon)"></span>
          <span v-show="!collapsed" class="menu-item-label">{{ item.label }}</span>
        </router-link>
      </div>
    </nav>

    <!-- 折叠按钮 -->
    <div class="sidebar-footer">
      <button class="collapse-btn" @click="toggleSidebar" :title="collapsed ? '展开菜单' : '折叠菜单'">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline v-if="collapsed" points="9 18 15 12 9 6" />
          <polyline v-else points="15 18 9 12 15 6" />
        </svg>
      </button>
      <router-link to="/" class="back-home-btn" :title="collapsed ? '返回网站' : ''">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        <span v-show="!collapsed" style="margin-left:8px">返回网站</span>
      </router-link>
    </div>
  </aside>
</template>

<!-- 图标函数 -->
<script>
export function getIcon(name) {
  const icons = {
    plans: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>',
    dashboard: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>',
    projects: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>',
    articles: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>',
    growth: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>',
    skills: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>',
    messages: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>',
    settings: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>',
    profile: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
    logs: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>',
    certs: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="6"/><path d="M15.477 12.89L17 22l-5-3-5 3 1.523-9.11"/></svg>',
    timetable: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="17" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="7" y1="13" x2="10" y2="13"/><line x1="7" y1="17" x2="10" y2="17"/><line x1="13" y1="13" x2="17" y2="13"/><line x1="13" y1="17" x2="17" y2="17"/></svg>'
  }
  return icons[name] || ''
}
</script>

<style scoped>
.admin-sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 240px;
  background: var(--color-primary-dark);
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: width var(--transition-normal);
  overflow-x: hidden;
}

.admin-sidebar.collapsed {
  width: 64px;
}

/* Logo */
.sidebar-logo {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-logo-link {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  white-space: nowrap;
}

.sidebar-logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

/* 菜单 */
.sidebar-nav {
  flex: 1;
  padding: 12px 0;
  overflow-y: auto;
}

.menu-group {
  margin-bottom: 8px;
}

.menu-group-title {
  padding: 8px 20px 4px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.35);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  margin: 2px 8px;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.collapsed .menu-item {
  justify-content: center;
  padding: 10px;
}

.menu-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
}

.menu-item.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  font-weight: 500;
}

.menu-item-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

/* 底部 */
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  background: rgba(255, 255, 255, 0.08);
  border: none;
  border-radius: var(--radius-sm);
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.back-home-btn {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-sm);
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
  font-size: var(--font-size-xs);
  transition: all var(--transition-fast);
}

.collapsed .back-home-btn {
  justify-content: center;
}

.back-home-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}
</style>
