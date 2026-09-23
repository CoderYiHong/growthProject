<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function handleLogin() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authStore.login(form.username, form.password, form.remember)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/admin/dashboard'
      router.push(redirect)
    } catch (err) {
      ElMessage.error(err.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="admin-login-page">
    <div class="login-container">
      <!-- 左侧品牌 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <svg width="56" height="56" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="12" fill="white" opacity="0.95"/>
              <path d="M24 8C24 8 16 18 16 26C16 30.4 19.6 34 24 34C28.4 34 32 30.4 32 26C32 18 24 8 24 8Z" fill="#4F9D57" opacity="0.9"/>
              <circle cx="24" cy="25" r="4" fill="#24463A"/>
            </svg>
            <span class="brand-name">Orange<span class="brand-dot">.</span></span>
          </div>
          <h2 class="brand-title">个人成长管理后台</h2>
          <p class="brand-desc">记录每一步成长，管理每一个作品</p>

          <!-- 装饰 -->
          <div class="brand-decorations">
            <div class="deco-plant">
              <svg viewBox="0 0 60 80" width="60" height="80">
                <rect x="26" y="50" width="8" height="30" fill="rgba(255,255,255,0.2)" rx="2"/>
                <circle cx="30" cy="35" r="22" fill="rgba(255,255,255,0.12)"/>
                <circle cx="22" cy="25" r="15" fill="rgba(255,255,255,0.18)"/>
                <circle cx="36" cy="28" r="13" fill="rgba(255,255,255,0.22)"/>
              </svg>
            </div>
            <div class="deco-code">
              <div class="deco-code-line" v-for="i in 4" :key="i" :style="{ width: (50 + i * 15) + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单 -->
      <div class="login-form-area">
        <div class="login-form-card">
          <div class="form-header">
            <h3>管理员登录</h3>
            <p>请输入账号密码访问管理后台</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            @submit.prevent="handleLogin"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                :prefix-icon="null"
                size="large"
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-checkbox v-model="form.remember">记住登录状态</el-checkbox>
            </el-form-item>

            <el-form-item>
              <el-button
                type="success"
                :loading="loading"
                @click="handleLogin"
                size="large"
                style="width: 100%; --el-button-bg-color: #4F9D57; --el-button-border-color: #4F9D57;"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <router-link to="/" class="back-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
              返回网站首页
            </router-link>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
}

.login-container {
  display: flex;
  width: 900px;
  min-height: 560px;
  background: var(--color-bg-white);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

/* 左侧 */
.login-brand {
  width: 420px;
  background: linear-gradient(135deg, #24463A 0%, #4F9D57 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
  position: relative;
  overflow: hidden;
}

.brand-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: var(--spacing-lg);
}

.brand-name {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.brand-dot {
  color: var(--color-accent);
}

.brand-title {
  font-size: var(--font-size-2xl);
  color: #fff;
  margin-bottom: var(--spacing-sm);
}

.brand-desc {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.7);
}

.brand-decorations {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: center;
  gap: var(--spacing-lg);
}

.deco-code {
  background: rgba(0, 0, 0, 0.2);
  border-radius: var(--radius-sm);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.deco-code-line {
  height: 5px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

/* 右侧 */
.login-form-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
}

.login-form-card {
  width: 100%;
  max-width: 320px;
}

.form-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.form-header h3 {
  font-size: var(--font-size-2xl);
  margin-bottom: var(--spacing-xs);
}

.form-header p {
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
}

.form-footer {
  text-align: center;
  margin-top: var(--spacing-sm);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--color-secondary);
  text-decoration: none;
}

.back-link:hover {
  color: var(--color-primary);
}

.login-hint {
  text-align: center;
  margin-top: var(--spacing-md);
  padding: var(--spacing-sm);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.login-hint p {
  font-size: var(--font-size-xs);
  color: var(--color-muted);
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    width: calc(100vw - 32px);
    min-height: auto;
    margin: var(--spacing-md);
  }

  .login-brand {
    width: 100%;
    padding: var(--spacing-xl) var(--spacing-md);
  }

  .brand-decorations {
    display: none;
  }

  .login-form-area {
    padding: var(--spacing-xl);
  }
}
</style>
