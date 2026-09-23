<script setup>
import { ref, onMounted } from 'vue'
import { getSiteSettings } from '@/api/public'

const siteName = ref('Orange.')
const siteDesc = ref('')
const socialLinks = ref([])

onMounted(async () => {
  try {
    const res = await getSiteSettings()
    if (res.code === 200 && res.data) {
      if (res.data.siteName) siteName.value = res.data.siteName
      if (res.data.siteDescription) siteDesc.value = res.data.siteDescription
      if (res.data.resume_github) socialLinks.value.push({ name: 'GitHub', url: res.data.resume_github })
      if (res.data.resume_csdn) socialLinks.value.push({ name: 'CSDN', url: res.data.resume_csdn })
    }
  } catch { /* 使用默认值 */ }
})

const footerNav = [
  { label: '首页', path: '/' },
  { label: '成长轨迹', path: '/growth' },
  { label: '项目作品', path: '/projects' },
  { label: '技术能力', path: '/skills' },
  { label: '课表', path: '/timetable' },
  { label: '技术文章', path: '/articles' },
  { label: '关于我', path: '/about' },
  { label: '联系我', path: '/contact' }
]
</script>

<template>
  <footer class="app-footer">
    <div class="footer-container container-wide">
      <div class="footer-grid">
        <!-- 品牌 -->
        <div class="footer-brand">
          <div class="footer-logo">
            <svg width="28" height="28" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="10" fill="#4F9D57"/>
              <path d="M24 8C24 8 16 18 16 26C16 30.4 19.6 34 24 34C28.4 34 32 30.4 32 26C32 18 24 8 24 8Z" fill="white" opacity="0.9"/>
              <circle cx="24" cy="25" r="4" fill="#24463A"/>
            </svg>
            <span class="footer-logo-text">{{ siteName }}<span class="footer-logo-dot">.</span></span>
          </div>
          <p class="footer-desc">{{ siteDesc || '记录学习、项目与成长的每一步' }}</p>
        </div>

        <!-- 导航 -->
        <div class="footer-nav-col">
          <h4 class="footer-col-title">页面导航</h4>
          <ul>
            <li v-for="item in footerNav" :key="item.path">
              <router-link :to="item.path">{{ item.label }}</router-link>
            </li>
          </ul>
        </div>

        <!-- 技术平台 -->
        <div class="footer-nav-col" v-if="socialLinks.length > 0">
          <h4 class="footer-col-title">技术平台</h4>
          <ul>
            <li v-for="s in socialLinks" :key="s.name">
              <a :href="s.url" target="_blank">{{ s.name }}</a>
            </li>
          </ul>
        </div>
      </div>

      <!-- 版权 -->
      <div class="footer-bottom">
        <p>© {{ new Date().getFullYear() }} {{ siteName }} All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  border-top: 1px solid var(--color-border);
  padding: 40px 0 24px;
  margin-top: auto;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 32px;
  margin-bottom: 28px;
}

/* 品牌 */
.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.footer-logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-title);
}

.footer-logo-dot {
  color: var(--color-primary);
}

.footer-desc {
  font-size: 13px;
  color: var(--color-secondary);
  line-height: 1.7;
  max-width: 320px;
}

/* 导航列 */
.footer-col-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-title);
  margin-bottom: 12px;
}

.footer-nav-col ul {
  list-style: none;
}

.footer-nav-col ul li {
  margin-bottom: 6px;
}

.footer-nav-col ul li a {
  font-size: 13px;
  color: var(--color-secondary);
  text-decoration: none;
  transition: color 0.15s;
}

.footer-nav-col ul li a:hover {
  color: var(--color-primary);
}

/* 版权 */
.footer-bottom {
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
  text-align: center;
}

.footer-bottom p {
  font-size: 12px;
  color: var(--color-muted);
}

@media (max-width: 768px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 24px;
  }

  .footer-brand {
    grid-column: 1 / -1;
  }
}
</style>
