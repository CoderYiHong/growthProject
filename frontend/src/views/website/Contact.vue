<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { sendContact } from '@/api/contact'
import { getSiteSettings } from '@/api/public'

const formRef = ref(null)
const submitting = ref(false)

// 从后端获取联系方式，失败时兜底
const contactInfo = ref({
  email: '',
  location: '中国',
  social: [
    { name: 'GitHub', icon: 'github', url: 'https://github.com/CoderYiHong' },
    { name: 'CSDN', icon: 'csdn', url: 'https://blog.csdn.net/Yihong1833100198?type=blog' }
  ]
})

onMounted(async () => {
  try {
    const res = await getSiteSettings()
    if (res.code === 200 && res.data) {
      const s = res.data
      if (s.profile_email) contactInfo.value.email = s.profile_email
      if (s.resume_location) contactInfo.value.location = s.resume_location
      if (s.resume_github || s.resume_csdn) {
        contactInfo.value.social = []
        if (s.resume_github) contactInfo.value.social.push({ name: 'GitHub', icon: 'github', url: s.resume_github })
        if (s.resume_csdn) contactInfo.value.social.push({ name: 'CSDN', icon: 'csdn', url: s.resume_csdn })
      }
    }
  } catch { /* 使用兜底值 */ }
})

const form = reactive({
  name: '',
  email: '',
  subject: '',
  content: ''
})

const rules = {
  name: [{ required: true, message: '请输入您的姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  subject: [{ required: true, message: '请输入联系主题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur', min: 10 }]
}

async function handleSubmit() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const result = await sendContact({
        name: form.name,
        email: form.email,
        subject: form.subject,
        content: form.content
      })
      ElMessage.success(result.message)
      form.name = ''
      form.email = ''
      form.subject = ''
      form.content = ''
    } catch (err) {
      ElMessage.error(err?.message || '发送失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<template>
  <div class="contact-page">
    <section class="section">
      <div class="container">
        <div class="contact-grid">
          <!-- 左侧联系信息 -->
          <div class="contact-info">
            <h2>联系方式</h2>
            <p class="contact-info-desc">期待与你的交流，无论是技术探讨、项目合作还是简单的问候。</p>

            <div class="contact-methods">
              <div v-if="contactInfo.email" class="contact-method">
                <div class="method-icon">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                </div>
                <div>
                  <span class="method-label">邮箱</span>
                  <a :href="'mailto:' + contactInfo.email" class="method-value">{{ contactInfo.email }}</a>
                </div>
              </div>

              <div class="contact-method">
                <div class="method-icon">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                </div>
                <div>
                  <span class="method-label">所在地</span>
                  <span class="method-value">{{ contactInfo.location }}</span>
                </div>
              </div>
            </div>

            <div class="contact-social">
              <h4>社交平台</h4>
              <div class="social-links">
                <a v-for="s in contactInfo.social" :key="s.name" :href="s.url" target="_blank" class="social-link-card">
                  <span class="social-link-name">{{ s.name }}</span>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                </a>
              </div>
            </div>
          </div>

          <!-- 右侧联系表单 -->
          <div class="contact-form-wrapper">
            <div class="contact-form-card">
              <h3>发送消息</h3>
              <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                label-position="top"
                @submit.prevent="handleSubmit"
              >
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="form.name" placeholder="请输入您的姓名" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="请输入您的邮箱" />
                </el-form-item>
                <el-form-item label="联系主题" prop="subject">
                  <el-input v-model="form.subject" placeholder="请输入联系主题" />
                </el-form-item>
                <el-form-item label="留言内容" prop="content">
                  <el-input
                    v-model="form.content"
                    type="textarea"
                    :rows="5"
                    placeholder="请输入您的留言内容..."
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="success"
                    :loading="submitting"
                    @click="handleSubmit"
                    style="width: 100%; --el-button-bg-color: var(--color-primary); --el-button-border-color: var(--color-primary);"
                  >
                    {{ submitting ? '发送中...' : '发送消息' }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.contact-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: var(--spacing-3xl);
}

.contact-info h2 {
  margin-bottom: var(--spacing-sm);
}

.contact-info-desc {
  color: var(--color-secondary);
  line-height: 1.8;
  margin-bottom: var(--spacing-xl);
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
}

.contact-method {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.method-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-light);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.method-label {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--color-secondary);
}

.method-value {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--color-title);
  text-decoration: none;
}

a.method-value:hover {
  color: var(--color-primary);
}

.contact-social h4 {
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
}

.social-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.social-link-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border-light);
  text-decoration: none;
  color: var(--color-body);
  font-size: var(--font-size-sm);
  transition: all var(--transition-fast);
}

.social-link-card:hover {
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-color: var(--color-primary-light);
}

/* 表单 */
.contact-form-card {
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
}

.contact-form-card h3 {
  margin-bottom: var(--spacing-lg);
}

@media (max-width: 768px) {
  .contact-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-xl);
  }
}
</style>
