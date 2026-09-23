<script setup>
defineProps({
  profile: { type: Object, required: true }
})
</script>

<template>
  <section class="resume-section">
    <h2 class="section-title">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
      个人简介
    </h2>
    <p class="intro-text">{{ profile.introduction }}</p>

    <div class="direction-tags" v-if="profile.technicalDirection?.length">
      <span class="direction-label">技术方向：</span>
      <span
        v-for="(dir, i) in profile.technicalDirection"
        :key="i"
        class="direction-tag"
      >{{ dir }}</span>
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

.intro-text {
  font-size: 16px; line-height: 1.8; color: #34463a;
  margin-bottom: 14px; max-width: 880px;
}

.direction-tags { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.direction-label { font-size: 13px; color: #65766b; }
.direction-tag {
  padding: 5px 16px; background: #eaf5ec; color: #2f6b36;
  border-radius: 20px; font-size: 13px; font-weight: 500; border: 1px solid #c8dccb;
}
</style>
