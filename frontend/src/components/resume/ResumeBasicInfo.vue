<script setup>
defineProps({
  profile: { type: Object, required: true }
})
</script>

<template>
  <section class="resume-section">
    <h2 class="section-title">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>
      基本信息
    </h2>

    <div class="info-grid">
      <div class="info-item">
        <span class="info-label">姓名</span>
        <span class="info-value">{{ profile.name }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">学历</span>
        <span class="info-value">{{ profile.degree }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">专业</span>
        <span class="info-value">{{ profile.major }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">政治面貌</span>
        <span class="info-value">{{ profile.politicalStatus || '--' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">所在地</span>
        <span class="info-value">{{ profile.location }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">邮箱</span>
        <span class="info-value">{{ profile.email }}</span>
      </div>
      <div class="info-item" v-if="profile.github">
        <span class="info-label">GitHub</span>
        <a class="info-value info-link" :href="profile.github" target="_blank">{{ profile.github }}</a>
      </div>
      <div class="info-item" v-if="profile.csdn">
        <span class="info-label">CSDN</span>
        <a class="info-value info-link" :href="profile.csdn" target="_blank">访问博客</a>
      </div>
      <div class="info-item" v-if="profile.phone">
        <span class="info-label">电话</span>
        <span class="info-value">{{ profile.phone }}</span>
      </div>
    </div>
  </section>
</template>

<style scoped>
.resume-section {
  padding: 0;
  background: rgba(255,255,255,0.88);
  border-radius: 14px;
  border: 1px solid #d9e3da;
  padding: 24px 28px;
  margin-bottom: 24px;
}

.section-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 18px; margin-bottom: 14px; color: #1f3025; font-weight: 700;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border: 1px solid #d9e3da;
  border-radius: 10px;
  overflow: hidden;
}

.info-item {
  display: flex; flex-direction: column; gap: 4px;
  padding: 18px 20px;
  background: rgba(255,255,255,0.8);
  border-bottom: 1px solid #e8ede9;
  border-right: 1px solid #e8ede9;
}
.info-item:nth-child(3n) { border-right: none; }
.info-item:nth-last-child(-n+3) { border-bottom: none; }

.info-label { font-size: 12px; color: #718077; }
.info-value { font-size: 14px; color: #25352b; font-weight: 600; }
.info-link { color: #2f9142; text-decoration: none; }
.info-link:hover { text-decoration: underline; color: #247334; }

@media (max-width: 768px) {
  .info-grid { grid-template-columns: repeat(2, 1fr); }
  .info-item:nth-child(3n) { border-right: 1px solid #e8ede9; }
  .info-item:nth-child(2n) { border-right: none; }
  .info-item:nth-last-child(-n+2) { border-bottom: none; }
}
</style>
