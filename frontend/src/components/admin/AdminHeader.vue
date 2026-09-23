<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

defineEmits(['toggleDrawer', 'logout'])

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/admin/login')
}
</script>

<template>
  <header class="admin-header">
    <div class="admin-header-left">
      <!-- 移动端菜单按钮 -->
      <button class="drawer-toggle" @click="$emit('toggleDrawer')" title="菜单">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
      </button>
      <span class="page-title-sm">管理后台</span>
    </div>

    <div class="admin-header-right">
      <!-- 用户下拉 -->
      <el-dropdown trigger="click">
        <div class="user-info">
          <div class="user-avatar">
            {{ (authStore.userInfo?.nickname || 'YH').charAt(0) }}
          </div>
          <span class="user-name">{{ authStore.userInfo?.nickname || '管理员' }}</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>
              <router-link to="/admin/profile" style="color:inherit;text-decoration:none;">个人设置</router-link>
            </el-dropdown-item>
            <el-dropdown-item>
              <router-link to="/" style="color:inherit;text-decoration:none;">返回网站</router-link>
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 var(--spacing-lg);
  background: rgba(255, 255, 255, 0.85);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.admin-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drawer-toggle {
  display: none;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--color-body);
  border-radius: var(--radius-sm);
}

.drawer-toggle:hover {
  background: var(--color-bg);
}

.page-title-sm {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--color-title);
}

/* 右侧 */
.admin-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-badge {
  cursor: pointer;
}

.header-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  color: var(--color-body);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.header-icon-btn:hover {
  background: var(--color-bg);
  color: var(--color-primary);
}

/* 用户 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.user-info:hover {
  background: var(--color-bg);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.user-name {
  font-size: var(--font-size-sm);
  color: var(--color-body);
}

/* 移动端 */
@media (max-width: 768px) {
  .drawer-toggle {
    display: flex;
  }

  .user-name {
    display: none;
  }
}
</style>
