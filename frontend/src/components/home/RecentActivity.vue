<script setup>
import { ref, computed } from 'vue'
import { formatDate } from '@/utils'

const props = defineProps({
  records: { type: Array, required: true }
})

const activeType = ref('all')

const typeLabels = {
  all: '全部',
  learning: '学习',
  project: '项目',
  problem: '解决',
  article: '文章'
}

const typeIconMap = {
  learning: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253',
  project: 'M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z',
  problem: 'M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z M12 9v4 M12 17h.01',
  article: 'M4 19.5A2.5 2.5 0 016.5 17H20 M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z'
}

const filteredRecords = computed(() => {
  if (activeType.value === 'all') return props.records
  return props.records.filter(r => r.type === activeType.value)
})
</script>

<template>
  <section class="section">
    <div class="container-wide">
      <div class="section-head">
        <div>
          <h2 class="section-title">最近成长记录</h2>
          <p class="section-sub">记录学习过程，看见每一步成长</p>
        </div>
      </div>

      <div class="type-filter">
        <button
          v-for="(label, key) in typeLabels"
          :key="key"
          class="type-btn"
          :class="{ active: activeType === key }"
          @click="activeType = key"
        >{{ label }}</button>
      </div>

      <div v-if="filteredRecords.length === 0" class="empty">
        <p>暂无记录</p>
      </div>

      <div v-else class="timeline">
        <div v-for="record in filteredRecords" :key="record.id" class="tl-item">
          <div class="tl-dot" :class="record.type"></div>
          <div class="tl-card">
            <div class="tl-meta">
              <span class="tl-date">{{ formatDate(record.date, 'MM-DD') }}</span>
              <span class="tl-type-tag" :class="record.type">{{ typeLabels[record.type] || record.type }}</span>
              <span class="tl-duration">{{ record.duration }} 分钟</span>
            </div>
            <h4 class="tl-title">{{ record.title }}</h4>
            <p class="tl-desc">{{ record.description }}</p>
            <p v-if="record.outcome" class="tl-outcome">{{ record.outcome }}</p>
            <div class="tl-tags">
              <span v-for="t in record.tags" :key="t" class="tl-tag">{{ t }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-title);
  margin-bottom: 4px;
}

.section-sub {
  font-size: 14px;
  color: var(--color-secondary);
  margin-bottom: 20px;
}

.type-filter {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.type-btn {
  padding: 5px 14px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 13px;
  color: var(--color-secondary);
  background: transparent;
  cursor: pointer;
  transition: all 0.15s ease;
}

.type-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.type-btn.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }

.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: var(--color-border);
}

.tl-item {
  position: relative;
  margin-bottom: 20px;
}

.tl-dot {
  position: absolute;
  left: -20px;
  top: 8px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-border);
  border: 2px solid #fff;
}

.tl-dot.learning { background: var(--color-primary); }
.tl-dot.project { background: var(--color-accent); }
.tl-dot.problem { background: #E05353; }
.tl-dot.article { background: #6CB4EE; }

.tl-card {
  padding: 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(4px);
}

.tl-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.tl-date {
  font-size: 12px;
  color: var(--color-muted);
  font-weight: 500;
}

.tl-type-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 500;
}

.tl-type-tag.learning { background: var(--color-primary-light); color: var(--color-primary); }
.tl-type-tag.project { background: var(--color-accent-light); color: #B8860B; }
.tl-type-tag.problem { background: #FDE8E8; color: #C53030; }
.tl-type-tag.article { background: #E8F4FD; color: #2B6CB0; }

.tl-duration {
  font-size: 11px;
  color: var(--color-muted);
}

.tl-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-title);
  margin-bottom: 4px;
}

.tl-desc {
  font-size: 13px;
  color: var(--color-secondary);
  line-height: 1.6;
  margin-bottom: 4px;
}

.tl-outcome {
  font-size: 13px;
  color: var(--color-body);
  line-height: 1.5;
  padding: 6px 10px;
  background: var(--color-bg);
  border-radius: 3px;
  margin-bottom: 8px;
  border-left: 3px solid var(--color-primary);
}

.tl-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.tl-tag {
  font-size: 11px;
  padding: 1px 6px;
  background: var(--color-bg);
  color: var(--color-muted);
  border-radius: 3px;
}

.empty {
  text-align: center;
  padding: 48px;
  color: var(--color-muted);
  font-size: 14px;
}

@media (max-width: 768px) {
  .timeline { padding-left: 18px; }
  .tl-dot { left: -15px; }
  .tl-card { padding: 12px; }
}
</style>
