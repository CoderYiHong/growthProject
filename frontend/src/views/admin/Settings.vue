<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSiteSettings, saveSiteSettings, uploadFile } from '@/api/admin'
import { ElMessage } from 'element-plus'

const form = reactive({
  siteName: 'Orange.',
  siteDescription: '',
  siteUrl: '',
  icp: '',
  logo: '',
  favicon: ''
})

const switches = reactive({
  enableComments: true,
  enableRegister: false,
  maintenanceMode: false,
  enableRSS: true
})

const saving = ref(false)

/** 兼容历史数据：字符串 'true'/'false' 和真实 boolean 统一转为 boolean */
function toBool(v, fallback) {
  if (typeof v === 'boolean') return v
  if (v === 'true') return true
  if (v === 'false') return false
  return fallback
}

onMounted(async () => {
  try {
    const res = await getSiteSettings()
    if (res.code === 200 && res.data) {
      const s = res.data
      if (s.siteName) form.siteName = s.siteName
      if (s.siteDescription) form.siteDescription = s.siteDescription
      if (s.siteUrl) form.siteUrl = s.siteUrl
      if (s.icp) form.icp = s.icp
      if (s.logo) form.logo = s.logo
      if (s.favicon) form.favicon = s.favicon
      // 兼容历史字符串 'true'/'false' 与真实 boolean
      switches.enableComments = toBool(s.enableComments, true)
      switches.enableRegister = toBool(s.enableRegister, false)
      switches.maintenanceMode = toBool(s.maintenanceMode, false)
      switches.enableRSS = toBool(s.enableRSS, true)
    }
  } catch (e) {
    console.error('获取设置失败', e)
  }
})

async function handleSave() {
  saving.value = true
  try {
    const res = await saveSiteSettings({
      siteName: form.siteName,
      siteDescription: form.siteDescription,
      siteUrl: form.siteUrl,
      icp: form.icp,
      logo: form.logo,
      favicon: form.favicon,
      enableComments: switches.enableComments,
      enableRegister: switches.enableRegister,
      maintenanceMode: switches.maintenanceMode,
      enableRSS: switches.enableRSS
    })
    if (res.code === 200) ElMessage.success('网站设置保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function triggerUpload(type) {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.jpg,.jpeg,.png,.gif,.webp'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('文件不能超过 5MB')
      return
    }
    try {
      const res = await uploadFile(file)
      if (res.code === 200 && res.data) {
        form[type] = res.data.url
        ElMessage.success('上传成功')
      }
    } catch (err) {
      ElMessage.error('上传失败')
    }
  }
  input.click()
}
</script>

<template>
  <div class="admin-settings">
    <div class="settings-grid">
      <!-- 基本信息 -->
      <div class="card">
        <h4 class="card-title">基本信息</h4>
        <el-form :model="form" label-position="top">
          <el-form-item label="网站名称">
            <el-input v-model="form.siteName" />
          </el-form-item>
          <el-form-item label="网站描述">
            <el-input v-model="form.siteDescription" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="网站地址">
            <el-input v-model="form.siteUrl" />
          </el-form-item>
          <el-form-item label="ICP 备案号">
            <el-input v-model="form.icp" placeholder="如：京ICP备XXXXXXXX号" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 图片上传 -->
      <div class="card">
        <h4 class="card-title">网站图片</h4>
        <div class="upload-areas">
          <div class="upload-item">
            <span class="upload-label">网站 Logo</span>
            <div class="upload-box" @click="triggerUpload('logo')">
              <img v-if="form.logo" :src="form.logo" class="upload-preview" />
              <template v-else>
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--color-border)" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                <span>点击上传</span>
              </template>
            </div>
          </div>
          <div class="upload-item">
            <span class="upload-label">Favicon</span>
            <div class="upload-box" @click="triggerUpload('favicon')">
              <img v-if="form.favicon" :src="form.favicon" class="upload-preview" />
              <template v-else>
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--color-border)" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                <span>点击上传</span>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 功能开关 -->
      <div class="card">
        <h4 class="card-title">功能开关</h4>
        <div class="switch-list">
          <div class="switch-item">
            <div>
              <span class="switch-name">启用评论</span>
              <p class="switch-desc">允许访客在文章下方发表评论</p>
            </div>
            <el-switch v-model="switches.enableComments" />
          </div>
          <div class="switch-item">
            <div>
              <span class="switch-name">开放注册</span>
              <p class="switch-desc">允许用户注册网站账号</p>
            </div>
            <el-switch v-model="switches.enableRegister" />
          </div>
          <div class="switch-item">
            <div>
              <span class="switch-name">维护模式</span>
              <p class="switch-desc">开启后仅管理员可访问网站</p>
            </div>
            <el-switch v-model="switches.maintenanceMode" />
          </div>
          <div class="switch-item">
            <div>
              <span class="switch-name">RSS 订阅</span>
              <p class="switch-desc">启用 RSS 文章订阅功能</p>
            </div>
            <el-switch v-model="switches.enableRSS" />
          </div>
        </div>
      </div>
    </div>

    <div style="margin-top: var(--spacing-lg);">
      <el-button type="success" size="large" :loading="saving" @click="handleSave">
        {{ saving ? '保存中...' : '保存设置' }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.settings-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-md); margin-bottom: var(--spacing-md); }
.card { background: #fff; border-radius: var(--radius-md); padding: var(--spacing-lg); box-shadow: var(--shadow-sm); }
.card-title { font-size: var(--font-size-md); margin-bottom: var(--spacing-md); }
.upload-areas { display: flex; gap: var(--spacing-lg); }
.upload-item { display: flex; flex-direction: column; gap: var(--spacing-xs); }
.upload-label { font-size: var(--font-size-sm); color: var(--color-secondary); }
.upload-box { width: 120px; height: 120px; border: 1.5px dashed var(--color-border); border-radius: var(--radius-md); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; cursor: pointer; transition: all var(--transition-fast); overflow: hidden; }
.upload-box:hover { border-color: var(--color-primary); background: var(--color-primary-light); }
.upload-box span { font-size: var(--font-size-xs); color: var(--color-muted); }
.upload-preview { width: 100%; height: 100%; object-fit: contain; }
.switch-list { display: flex; flex-direction: column; gap: var(--spacing-md); }
.switch-item { display: flex; align-items: center; justify-content: space-between; padding: var(--spacing-sm) 0; }
.switch-item:not(:last-child) { border-bottom: 1px solid var(--color-border-light); }
.switch-name { font-size: var(--font-size-sm); font-weight: 500; }
.switch-desc { font-size: var(--font-size-xs); color: var(--color-secondary); margin-top: 2px; }
@media (max-width: 768px) { .settings-grid { grid-template-columns: 1fr; } }
</style>
