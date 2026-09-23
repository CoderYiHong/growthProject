<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getProfile,
  getAbilities,
  getSkills,
  getProjects,
  getCertificates,
  getHonors,
  getContact
} from '@/api/resume'

import ResumeHeader from '@/components/resume/ResumeHeader.vue'
import ResumeIntroduction from '@/components/resume/ResumeIntroduction.vue'
import ResumeBasicInfo from '@/components/resume/ResumeBasicInfo.vue'
import ResumeAbilities from '@/components/resume/ResumeAbilities.vue'
import ResumeSkills from '@/components/resume/ResumeSkills.vue'
import ResumeProjects from '@/components/resume/ResumeProjects.vue'
import ResumeCertificates from '@/components/resume/ResumeCertificates.vue'
import ResumeHonors from '@/components/resume/ResumeHonors.vue'
import ResumeContact from '@/components/resume/ResumeContact.vue'

const router = useRouter()

// ==================== 状态管理 ====================
const loading = ref(true)
const error = ref(false)
const errorMsg = ref('')

const profile = ref(null)
const abilities = ref([])
const skillGroups = ref([])
const projects = ref([])
const certificates = ref([])
const honors = ref([])
const contact = ref(null)

// ==================== 数据加载 ====================
async function loadData() {
  loading.value = true
  error.value = false

  try {
    const results = await Promise.all([
      getProfile(),
      getAbilities(),
      getSkills(),
      getProjects(),
      getCertificates(),
      getHonors(),
      getContact()
    ])

    // 统一取出 data 字段
    profile.value = results[0].data
    abilities.value = results[1].data
    skillGroups.value = results[2].data
    projects.value = results[3].data
    certificates.value = results[4].data
    honors.value = results[5].data
    contact.value = results[6].data
  } catch (e) {
    error.value = true
    errorMsg.value = e?.message || '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ==================== 导出简历 ====================
const isExporting = ref(false)

function handleExport() {
  isExporting.value = true
  // 给 CSS 时间应用 print 样式，然后调用打印
  setTimeout(() => {
    window.print()
    isExporting.value = false
  }, 100)
}

// ==================== 联系我 ====================
function handleContact() {
  router.push('/contact')
}

onMounted(() => loadData())
</script>

<template>
  <div class="about-resume-page" :class="{ 'is-exporting': isExporting }">
    <!-- ===== 骨架屏 ===== -->
    <template v-if="loading">
      <div class="resume-container">
        <div class="skeleton-header">
          <div class="sk-circle"></div>
          <div class="sk-lines">
            <div class="sk-line w-40"></div>
            <div class="sk-line w-25"></div>
          </div>
        </div>
        <div class="sk-block"><div class="sk-line w-100"></div><div class="sk-line w-70"></div></div>
        <div class="sk-block"><div class="sk-line w-100"></div><div class="sk-line w-60"></div></div>
        <div class="sk-block"><div class="sk-line w-100"></div><div class="sk-line w-50"></div></div>
      </div>
    </template>

    <!-- ===== 错误状态 ===== -->
    <template v-else-if="error">
      <div class="resume-container">
        <div class="error-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="var(--color-border)" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <p>{{ errorMsg }}</p>
          <button class="resume-btn resume-btn-primary" @click="loadData">重新加载</button>
        </div>
      </div>
    </template>

    <!-- ===== 正常内容 ===== -->
    <template v-else>
      <div class="resume-container" id="resume-print-area">
        <!-- 个人信息头部 -->
        <ResumeHeader
          :profile="profile"
          @export="handleExport"
          @contact="handleContact"
        />

        <div class="resume-divider"></div>

        <!-- 个人简介 -->
        <ResumeIntroduction :profile="profile" />

        <!-- 基本信息 -->
        <ResumeBasicInfo :profile="profile" />

        <!-- 核心能力 -->
        <ResumeAbilities :abilities="abilities" />

        <!-- 专业技能 -->
        <ResumeSkills :skill-groups="skillGroups" />

        <!-- 代表项目 -->
        <ResumeProjects :projects="projects" />

        <!-- 证书资质 -->
        <ResumeCertificates :certificates="certificates" />

        <!-- 个人荣誉 -->
        <ResumeHonors :honors="honors" />

        <!-- 联系方式 -->
        <ResumeContact :contact="contact" />
      </div>
    </template>
  </div>
</template>

<style scoped>
/* ===== 背景遮罩（独立伪元素，不影响内容 opacity） ===== */
.about-resume-page {
  position: relative;
}
.about-resume-page::before {
  content: "";
  position: fixed;
  inset: 0;
  background: rgba(239, 245, 236, 0.58);
  pointer-events: none;
  z-index: 0;
}

/* ===== 主内容面板 ===== */
.resume-container {
  position: relative;
  z-index: 1;
  max-width: 1220px;
  margin: 24px auto 48px;
  padding: 40px 44px 56px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border: 1px solid #c8d6ca;
  border-radius: 18px;
  box-shadow: 0 2px 20px rgba(0,0,0,0.06);
}

.resume-divider {
  height: 1px;
  background: #d9e3da;
  margin: 20px 0;
}

/* ===== 骨架屏 ===== */
.skeleton-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl) 0;
}

.sk-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(90deg, #DDE8E0 25%, #CFDDD3 50%, #DDE8E0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
}

.sk-lines {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.sk-block {
  padding: var(--spacing-lg) 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-line {
  height: 14px;
  border-radius: 7px;
  background: linear-gradient(90deg, #DDE8E0 25%, #CFDDD3 50%, #DDE8E0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.sk-line.w-100 { width: 100%; }
.sk-line.w-70  { width: 70%; }
.sk-line.w-60  { width: 60%; }
.sk-line.w-50  { width: 50%; }
.sk-line.w-40  { width: 40%; }
.sk-line.w-25  { width: 25%; }

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* ===== 错误状态 ===== */
.error-state {
  text-align: center;
  padding: var(--spacing-3xl);
}

.error-state p {
  margin: var(--spacing-md) 0;
  color: var(--color-secondary);
}

.resume-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.resume-btn-primary {
  background: var(--color-primary);
  color: #fff;
}

.resume-btn-primary:hover {
  background: #3D8B47;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .resume-container {
    padding: 24px 14px 40px;
    margin: 12px 8px 24px;
    border-radius: 12px;
  }
}
</style>

<!-- ===== 统一卡片样式（穿透子组件 scoped 限制） ===== -->
<style>
.about-resume-page .resume-section {
  padding: 0 !important;
  background: rgba(255,255,255,0.88) !important;
  border-radius: 14px !important;
  border: 1px solid #d9e3da !important;
  padding: 24px 28px !important;
  margin-bottom: 24px !important;
}
.about-resume-page .resume-section .section-title {
  font-size: 18px !important;
  font-weight: 700 !important;
  color: #1f3025 !important;
  margin-bottom: 14px !important;
}
/* 修复按钮样式统一 */
.about-resume-page .resume-btn-primary {
  background: #2f9142 !important;
  color: #fff !important;
  border: none !important;
}
.about-resume-page .resume-btn-outline {
  background: rgba(255,255,255,0.9) !important;
  color: #2f9142 !important;
  border: 1.5px solid #2f9142 !important;
}
@media (max-width: 768px) {
  .about-resume-page .resume-section {
    padding: 18px 14px !important;
    margin-bottom: 16px !important;
  }
}
</style>

<!-- ===== 打印样式（全局生效） ===== -->
<style>
/* 打印时隐藏全局元素 */
@media print {
  /* 隐藏导航栏 */
  .app-header,
  /* 隐藏页脚 */
  .app-footer,
  /* 隐藏返回顶部 */
  .scroll-to-top,
  /* 隐藏按钮 */
  .header-actions-col,
  /* 隐藏证书操作区域 */
  .cert-view-link,
  .cert-card,
  /* 隐藏页面装饰 */
  .hero-bg-layer,
  .bg-radial-glow,
  .bg-dot-pattern,
  .bg-plant-line,
  /* 隐藏网站布局外层非打印区域 */
  .website-layout > *:not(.main-content) {
    display: none !important;
  }

  /* 去掉导航栏占位 */
  .main-content {
    padding-top: 0 !important;
  }

  /* 全宽 */
  .resume-container {
    max-width: 100% !important;
    padding: 0 !important;
  }

  /* A4 布局优化 */
  @page {
    size: A4;
    margin: 18mm 15mm;
  }

  body {
    background: #fff !important;
    color: #000 !important;
    font-size: 13px;
    line-height: 1.6;
  }

  .resume-section {
    padding: 12px 0 !important;
    page-break-inside: avoid;
  }

  .cert-card,
  .project-card,
  .honor-card,
  .ability-card {
    box-shadow: none !important;
    border: 1px solid var(--color-border) !important;
    page-break-inside: avoid;
  }

  .resume-divider {
    display: none;
  }
}
</style>
