<script setup>
import { ref } from 'vue'

defineProps({
  certificates: { type: Array, required: true }
})

const previewVisible = ref(false)
const previewCert = ref(null)
const failedImages = ref(new Set())
const previewImageFailed = ref(false)

function openPreview(cert) {
  previewCert.value = cert
  previewImageFailed.value = false
  previewVisible.value = true
}

function imageKey(cert) {
  return cert.id ?? cert.imageUrl
}

function hasImage(cert) {
  return Boolean(cert.imageUrl) && !failedImages.value.has(imageKey(cert))
}

function handleCardImgError(cert) {
  const next = new Set(failedImages.value)
  next.add(imageKey(cert))
  failedImages.value = next
}

function handlePreviewImgError() {
  previewImageFailed.value = true
}
</script>

<template>
  <section class="resume-section" v-if="certificates && certificates.length > 0">
    <h2 class="section-title">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
      证书资质
    </h2>

    <div class="certs-grid">
      <div v-for="cert in certificates" :key="cert.id" class="cert-card">
        <div class="cert-image">
          <img
            v-if="hasImage(cert)"
            :src="cert.imageUrl"
            :alt="cert.name"
            @error="handleCardImgError(cert)"
          />
          <div v-else class="cert-fallback">
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="1.5" opacity="0.3">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
        </div>
        <div class="cert-info">
          <h4 class="cert-name">{{ cert.name }}</h4>
          <span class="cert-category-tag">{{ cert.category }}</span>
          <div class="cert-meta">
            <span v-if="cert.issuer">{{ cert.issuer }}</span>
            <span>{{ cert.issueDate }}</span>
          </div>
          <p class="cert-desc">{{ cert.description }}</p>
          <button
            type="button"
            class="cert-view-link"
            :disabled="!hasImage(cert)"
            @click="openPreview(cert)"
          >
            {{ hasImage(cert) ? '查看证书 →' : '暂无证书图片' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewVisible"
      :title="previewCert?.name"
      width="min(1240px, 94vw)"
      top="3vh"
      class="cert-preview-dialog"
      append-to-body
      destroy-on-close
    >
      <div class="cert-preview" v-if="previewCert">
        <div class="cert-preview-image">
          <el-image
            v-if="previewCert.imageUrl && !previewImageFailed"
            class="cert-preview-large-image"
            :src="previewCert.imageUrl"
            :preview-src-list="[previewCert.imageUrl]"
            :alt="previewCert.name"
            fit="contain"
            preview-teleported
            hide-on-click-modal
            @error="handlePreviewImgError"
          />
          <div v-else class="cert-preview-fallback">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="1" opacity="0.2">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/>
            </svg>
            <p>证书图片</p>
          </div>
          <span v-if="previewCert.imageUrl && !previewImageFailed" class="image-zoom-hint">点击图片可全屏查看</span>
        </div>
        <div class="cert-preview-info">
          <div class="preview-row">
            <span class="preview-label">证书名称</span>
            <span class="preview-value">{{ previewCert.name }}</span>
          </div>
          <div class="preview-row">
            <span class="preview-label">分类</span>
            <span class="preview-value">{{ previewCert.category }}</span>
          </div>
          <div class="preview-row" v-if="previewCert.issuer">
            <span class="preview-label">颁发机构</span>
            <span class="preview-value">{{ previewCert.issuer }}</span>
          </div>
          <div class="preview-row">
            <span class="preview-label">获得时间</span>
            <span class="preview-value">{{ previewCert.issueDate }}</span>
          </div>
          <div class="preview-row" v-if="previewCert.credentialNo">
            <span class="preview-label">证书编号</span>
            <span class="preview-value">{{ previewCert.credentialNo }}</span>
          </div>
          <div class="preview-row">
            <span class="preview-label">介绍</span>
            <span class="preview-value">{{ previewCert.description }}</span>
          </div>
          <div class="preview-row" v-if="previewCert.verificationUrl">
            <span class="preview-label">验证地址</span>
            <a class="preview-link" :href="previewCert.verificationUrl" target="_blank" rel="noopener noreferrer">打开验证页面</a>
          </div>
        </div>
      </div>
    </el-dialog>
  </section>
</template>

<style scoped>
.resume-section {
  padding: var(--spacing-lg) 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-xl);
  margin-bottom: var(--spacing-md);
  color: var(--color-title);
}

.certs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.cert-card {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.cert-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--color-primary-light);
}

.cert-image {
  width: 90px;
  height: 70px;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-bg));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cert-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cert-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cert-info {
  flex: 1;
  min-width: 0;
}

.cert-name {
  font-size: var(--font-size-sm);
  font-weight: 600;
  margin-bottom: 4px;
  line-height: 1.4;
}

.cert-category-tag {
  display: inline-block;
  padding: 1px 8px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: 10px;
  font-size: 11px;
  margin-bottom: 6px;
}

.cert-meta {
  font-size: 11px;
  color: var(--color-muted);
  margin-bottom: 4px;
  display: flex;
  gap: 8px;
}

.cert-desc {
  font-size: 12px;
  color: var(--color-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 4px;
}

.cert-view-link {
  display: inline-block;
  padding: 0;
  border: 0;
  background: transparent;
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 500;
  cursor: pointer;
}

.cert-view-link:hover:not(:disabled) {
  text-decoration: underline;
}

.cert-view-link:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 3px;
  border-radius: 2px;
}

.cert-view-link:disabled {
  color: var(--color-muted);
  cursor: not-allowed;
}

/* 预览 */
.cert-preview {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: var(--spacing-lg);
  align-items: start;
}

.cert-preview-image {
  width: 100%;
  height: min(76vh, 800px);
  min-height: 520px;
  position: relative;
  background: #151a17;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cert-preview-large-image {
  width: 100%;
  height: 100%;
  cursor: zoom-in;
}

.cert-preview-large-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.cert-preview-fallback {
  text-align: center;
  color: var(--color-muted);
}

.cert-preview-fallback p {
  margin-top: 8px;
  font-size: var(--font-size-sm);
}

.image-zoom-hint {
  position: absolute;
  right: 12px;
  bottom: 10px;
  padding: 5px 10px;
  border-radius: 14px;
  color: #fff;
  background: rgba(0, 0, 0, 0.58);
  font-size: 12px;
  pointer-events: none;
}

.cert-preview-info {
  max-height: min(76vh, 800px);
  overflow-y: auto;
  padding-right: 4px;
}

.preview-row {
  display: block;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.preview-label {
  display: block;
  margin-bottom: 5px;
  font-size: var(--font-size-sm);
  color: var(--color-muted);
}

.preview-value {
  display: block;
  font-size: var(--font-size-sm);
  color: var(--color-body);
  line-height: 1.6;
}

.preview-link {
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  text-decoration: none;
}

.preview-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .certs-grid {
    grid-template-columns: 1fr;
  }

  .cert-preview {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .cert-preview-image {
    height: 62vh;
    min-height: 360px;
  }

  .cert-preview-info {
    max-height: none;
    overflow: visible;
  }
}
</style>
