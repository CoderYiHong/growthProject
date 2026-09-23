<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import AdminHeader from '@/components/admin/AdminHeader.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)

// 移动端侧边栏抽屉
const drawerVisible = ref(false)

function handleLogout() {
  authStore.logout()
  router.push('/admin/login')
}
</script>

<template>
  <div class="admin-layout">
    <!-- 桌面端侧边栏 -->
    <AdminSidebar
      :collapsed="isCollapsed"
      class="admin-sidebar-desktop"
      @logout="handleLogout"
    />

    <!-- 移动端侧边栏抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="260px"
      :with-header="false"
      class="admin-drawer"
    >
      <AdminSidebar :collapsed="false" @logout="handleLogout" />
    </el-drawer>

    <div class="admin-main" :class="{ collapsed: isCollapsed }">
      <AdminHeader @toggle-drawer="drawerVisible = true" @logout="handleLogout" />

      <div class="admin-content">
        <!-- 面包屑 -->
        <div class="admin-breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
              首页
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title && route.meta.title !== '数据概览'">
              {{ route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <router-view />
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: transparent;
}

/* 桌面端侧边栏 */
.admin-sidebar-desktop {
  display: block;
}

/* 移动端隐藏桌面侧边栏 */
@media (max-width: 768px) {
  .admin-sidebar-desktop {
    display: none;
  }
}

.admin-main {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
  transition: margin-left var(--transition-normal);
  min-width: 0;
}

.admin-main.collapsed {
  margin-left: 64px;
}

.admin-content {
  flex: 1;
  padding: var(--spacing-lg);
  overflow-y: auto;
}

.admin-breadcrumb {
  margin-bottom: var(--spacing-md);
}

/* 移动端 */
@media (max-width: 768px) {
  .admin-main {
    margin-left: 0;
  }

  .admin-main.collapsed {
    margin-left: 0;
  }

  .admin-content {
    padding: var(--spacing-sm);
  }
}
</style>
