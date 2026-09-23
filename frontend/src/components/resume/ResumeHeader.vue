<script setup>
defineProps({
  profile: { type: Object, required: true }
})

defineEmits(['export', 'contact'])

function handleImgError(e) {
  e.target.style.display = 'none'
}
</script>

<template>
  <div class="resume-header">
    <div class="header-avatar-col">
      <div class="header-avatar">
        <img
          v-if="profile.avatar"
          :src="profile.avatar"
          :alt="profile.name"
          @error="handleImgError"
        />
        <span v-else class="avatar-text">{{ (profile.name || 'Y').charAt(0) }}</span>
      </div>
    </div>

    <div class="header-info-col">
      <h1 class="header-name">{{ profile.name }}</h1>
      <p class="header-position">{{ profile.position }}</p>

      <div class="header-tags">
        <span class="header-tag degree-tag">{{ profile.degree }}</span>
        <span class="header-tag major-tag">{{ profile.major }}</span>
        <span
          v-if="profile.politicalStatus"
          class="header-tag political-tag"
        >{{ profile.politicalStatus }}</span>
      </div>

      <p class="header-location">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
        {{ profile.location }}
      </p>
    </div>

    <div class="header-actions-col">
      <button class="resume-btn resume-btn-primary" @click="$emit('export')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
        导出个人简历
      </button>
      <button class="resume-btn resume-btn-outline" @click="$emit('contact')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
        联系我
      </button>
    </div>
  </div>
</template>

<style scoped>
.resume-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px 28px;
  flex-wrap: wrap;
  background: rgba(255,255,255,0.88);
  border-radius: 14px;
  border: 1px solid #d9e3da;
  margin-bottom: 24px;
}

.header-avatar-col { flex-shrink: 0; }
.header-avatar {
  width: 100px; height: 100px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.header-avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-text { font-size: 40px; font-weight: 700; color: #fff; }

.header-info-col { flex: 1; min-width: 200px; }
.header-name { font-size: 30px; font-weight: 700; color: #17231b; margin-bottom: 2px; }
.header-position { font-size: 15px; color: #2f9142; font-weight: 500; margin-bottom: 8px; }

.header-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 6px; }
.header-tag { padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.degree-tag { background: var(--color-primary-light); color: #2f6b36; }
.major-tag { background: #E8F4FD; color: #2B6CB0; }
.political-tag { background: #FFF0F0; color: #C53030; }

.header-location { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #53675a; }

.header-actions-col { flex-shrink: 0; display: flex; flex-direction: column; gap: 8px; }
.resume-btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 10px 22px; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.15s; white-space: nowrap; }
.resume-btn-primary { background: #2f9142; color: #fff; border: none; }
.resume-btn-primary:hover { background: #247334; }
.resume-btn-outline { background: rgba(255,255,255,0.9); color: #2f9142; border: 1.5px solid #2f9142; }
.resume-btn-outline:hover { background: #eaf5ec; }

@media (max-width: 768px) {
  .resume-header { flex-direction: column; text-align: center; padding: 18px; }
  .header-actions-col { flex-direction: row; width: 100%; }
  .resume-btn { flex: 1; }
  .header-tags { justify-content: center; }
  .header-location { justify-content: center; }
}
</style>
