<script setup>
import { ref, computed } from 'vue'
import { usePlantStore } from '@/stores/plantStore'

const pstore = usePlantStore()
const showSelector = ref(false)
const tab = ref('current') // 'current' | 'garden'

function selectPlant(type) { pstore.initPlant(type, 'all', null); showSelector.value = false }

// 植物 SVG（更像真实植物）
const plantSvgs = {
  orange: '<circle cx="40" cy="115" r="30" fill="#4CAF50" opacity="0.9"/><circle cx="30" cy="100" r="20" fill="#66BB6A"/><circle cx="50" cy="105" r="18" fill="#43A047"/><circle cx="40" cy="90" r="15" fill="#81C784"/><rect x="37" y="145" width="6" height="40" rx="3" fill="#795548"/><circle cx="25" cy="140" r="5" fill="#FF9800"/><circle cx="55" cy="135" r="4" fill="#FF9800"/><circle cx="48" cy="142" r="4.5" fill="#FFB74D"/>',
  sunflower: '<rect x="37" y="120" width="6" height="50" rx="3" fill="#558B2F"/><circle cx="40" cy="110" r="22" fill="#FFC107"/><circle cx="40" cy="110" r="10" fill="#795548"/><ellipse cx="40" cy="65" rx="8" ry="16" fill="#66BB6A" transform="rotate(-15,40,65)"/><ellipse cx="40" cy="70" rx="7" ry="14" fill="#81C784" transform="rotate(10,40,70)"/>',
  sakura: '<rect x="37" y="125" width="5" height="40" rx="2" fill="#8D6E63"/><circle cx="40" cy="110" r="18" fill="#F8BBD0"/><circle cx="30" cy="100" r="14" fill="#F48FB1"/><circle cx="50" cy="98" r="12" fill="#F06292"/><circle cx="40" cy="95" r="10" fill="#EC407A" opacity="0.7"/>',
  bamboo: '<rect x="28" y="100" width="5" height="60" rx="2" fill="#66BB6A"/><rect x="46" y="90" width="5" height="65" rx="2" fill="#4CAF50"/><rect x="37" y="95" width="4" height="55" rx="2" fill="#81C784"/><line x1="28" y1="125" x2="33" y2="132" stroke="#81C784" stroke-width="3" rx="1"/><line x1="46" y1="115" x2="51" y2="122" stroke="#81C784" stroke-width="3" rx="1"/>',
  succulent: '<circle cx="40" cy="140" r="10" fill="#A5D6A7"/><circle cx="28" cy="135" r="9" fill="#81C784"/><circle cx="52" cy="135" r="9" fill="#81C784"/><circle cx="34" cy="128" r="8" fill="#66BB6A"/><circle cx="46" cy="128" r="8" fill="#66BB6A"/><circle cx="40" cy="124" r="7" fill="#4CAF50"/><circle cx="40" cy="136" r="5" fill="#C8E6C9"/>'
}

const plantColors = {
  orange: { bg: '#FFF3E0', border: '#FFE0B2', color: '#E65100' },
  sunflower: { bg: '#FFFDE7', border: '#FFF9C4', color: '#F9A825' },
  sakura: { bg: '#FCE4EC', border: '#F8BBD0', color: '#C2185B' },
  bamboo: { bg: '#E8F5E9', border: '#C8E6C9', color: '#2E7D32' },
  succulent: { bg: '#F1F8E9', border: '#DCEDC8', color: '#558B2F' }
}

const stages = pstore.PLANTS.orange.stages

const gardenPlants = computed(() => pstore.plant?.garden || [])

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}
</script>

<template>
  <div class="plant-card">
    <h4>🌱 学习花园</h4>

    <!-- 未选择植物 -->
    <template v-if="!pstore.plant">
      <div class="plant-empty" v-if="!showSelector">
        <p>选择一颗种子，开始你的学习养成之旅</p>
        <button class="btn-start" @click="showSelector = true">🎋 选择种子</button>
      </div>
      <div v-else class="plant-options">
        <div v-for="(cfg, key) in pstore.PLANTS" :key="key" class="po-item" @click="selectPlant(key)">
          <div class="po-svg" v-html="'<svg viewBox=\\'0 0 80 180\\'>' + (plantSvgs[key] || '') + '</svg>'"></div>
          <div class="po-name">{{ cfg.name }}</div>
          <div class="po-desc">{{ cfg.desc }}</div>
        </div>
        <button class="btn-cancel" @click="showSelector = false">取消</button>
      </div>
    </template>

    <!-- 已有植物 -->
    <template v-else>
      <!-- 标签切换 -->
      <div class="garden-tabs">
        <button :class="{ active: tab === 'current' }" @click="tab = 'current'">🌿 当前植物</button>
        <button :class="{ active: tab === 'garden' }" @click="tab = 'garden'">
          🏡 花园
          <span v-if="gardenPlants.length" class="tab-count">{{ gardenPlants.length }}</span>
        </button>
      </div>

      <!-- 当前植物视图 -->
      <div v-if="tab === 'current'" class="current-plant">
        <div class="cp-stage-badge" :style="{ background: (plantColors[pstore.plant.type] || plantColors.orange).bg, color: (plantColors[pstore.plant.type] || plantColors.orange).color }">
          {{ stages[pstore.currentStage] || '种子' }}
        </div>

        <div class="cp-visual">
          <div class="cp-svg" v-html="'<svg viewBox=\\'0 0 80 180\\'>' + (plantSvgs[pstore.plant.type] || plantSvgs.orange) + '</svg>'"></div>
        </div>

        <div class="cp-name">{{ pstore.PLANTS[pstore.plant.type]?.name || '植物' }}</div>
        <div class="cp-status" :class="pstore.plantStatus()">
          {{ pstore.plantStatus() === 'matured' ? '已成熟 🌟' : pstore.plantStatus() === 'dormant' ? '休眠中 💤' : pstore.plantStatus() === 'watered' ? '今日已浇水 ✅' : '正在生长 🌿' }}
        </div>

        <div class="cp-stats">
          <div class="cp-stat"><span class="cps-num">{{ (pstore.getStats().totalMins / 60).toFixed(1) }}h</span><span>累计专注</span></div>
          <div class="cp-stat"><span class="cps-num">{{ pstore.getStats().activeDays }}</span><span>有效天数</span></div>
        </div>

        <!-- 进度条 -->
        <div v-if="pstore.nextStageInfo()" class="cp-progress">
          <div class="cpp-row">
            <span>养分</span>
            <div class="cpp-bar"><div class="cpp-fill" :style="{ width: pstore.nextStageInfo().timePct + '%' }"></div></div>
            <span>{{ pstore.nextStageInfo().needMins }}min</span>
          </div>
          <div class="cpp-row">
            <span>阳光</span>
            <div class="cpp-bar"><div class="cpp-fill" :style="{ width: pstore.nextStageInfo().dayPct + '%' }"></div></div>
            <span>{{ pstore.nextStageInfo().needDays }}天</span>
          </div>
          <div class="cpp-target">下一阶段：{{ pstore.nextStageInfo().name }}</div>
        </div>
        <div v-else class="cp-matured">🎉 已完全成熟！可以收获并种植新的植物</div>
      </div>

      <!-- 花园视图 -->
      <div v-else class="garden-view">
        <!-- 当前植物（如已成熟则在花园中展示） -->
        <div v-if="gardenPlants.length === 0 && pstore.currentStage < 6" class="garden-empty">
          <p>还没有成熟的植物</p>
          <p class="garden-hint">当植物完全成熟后可以收获，新植物会加入花园</p>
        </div>

        <div v-if="pstore.currentStage >= 6 || gardenPlants.length > 0" class="garden-grid">
          <!-- 当前成熟植物 -->
          <div v-if="pstore.currentStage >= 6" class="garden-plant current" :style="{ borderColor: (plantColors[pstore.plant.type] || plantColors.orange).border }">
            <div class="gp-svg" v-html="'<svg viewBox=\\'0 0 80 150\\'>' + (plantSvgs[pstore.plant.type] || plantSvgs.orange) + '</svg>'"></div>
            <div class="gp-name">{{ pstore.PLANTS[pstore.plant.type]?.name }}</div>
            <div class="gp-badge current">正在种植</div>
            <div class="gp-date">开始于 {{ formatDate(pstore.plant.startedAt) }}</div>
          </div>

          <!-- 已收获的植物 -->
          <div v-for="(gp, idx) in gardenPlants" :key="idx" class="garden-plant harvested" :style="{ borderColor: (plantColors[gp.type] || plantColors.orange).border }">
            <div class="gp-svg" v-html="'<svg viewBox=\\'0 0 80 150\\'>' + (plantSvgs[gp.type] || plantSvgs.orange) + '</svg>'"></div>
            <div class="gp-name">{{ pstore.PLANTS[gp.type]?.name }}</div>
            <div class="gp-badge harvested">已收获</div>
            <div class="gp-stats-row">
              <span>{{ (gp.totalMins / 60).toFixed(1) }}h</span>
              <span>{{ gp.activeDays }}天</span>
            </div>
            <div class="gp-date">{{ formatDate(gp.maturedAt) }}</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.plant-card { background: rgba(255,255,255,0.94); border-radius: 12px; padding: 18px; border: 1px solid #d9e3da; }
.plant-card h4 { font-size: 15px; font-weight: 700; color: #17231b; margin-bottom: 12px; }

/* 空状态 */
.plant-empty { text-align: center; padding: 16px 0; }
.plant-empty p { font-size: 13px; color: #65766b; margin-bottom: 12px; }
.btn-start { padding: 8px 20px; background: #2f9142; color: #fff; border: none; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; }
.btn-start:hover { background: #277a37; }
.btn-cancel { display: block; margin: 8px auto 0; padding: 4px 12px; border: none; background: transparent; color: #909399; font-size: 12px; cursor: pointer; }

/* 种子选择 */
.plant-options { display: grid; grid-template-columns: repeat(auto-fill, minmax(90px, 1fr)); gap: 6px; }
.po-item { padding: 8px 6px; border: 1px solid #d9e3da; border-radius: 8px; cursor: pointer; text-align: center; transition: all 0.15s; }
.po-item:hover { border-color: #2f9142; background: #eaf5ec; }
.po-svg { width: 50px; height: 60px; margin: 0 auto 4px; overflow: hidden; }
.po-svg :deep(svg) { width: 100%; height: 100%; }
.po-name { font-size: 12px; font-weight: 600; color: #17231b; }
.po-desc { font-size: 10px; color: #718077; margin-top: 2px; }

/* Tabs */
.garden-tabs { display: flex; gap: 4px; margin-bottom: 12px; }
.garden-tabs button { flex: 1; padding: 6px; border: 1px solid #d9e3da; border-radius: 6px; font-size: 12px; font-weight: 600; background: #fff; color: #65766b; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 4px; }
.garden-tabs button.active { background: #eaf5ec; color: #2f9142; border-color: #2f9142; }
.tab-count { font-size: 10px; background: #2f9142; color: #fff; border-radius: 10px; padding: 1px 6px; min-width: 16px; text-align: center; }

/* 当前植物 */
.current-plant { text-align: center; }
.cp-stage-badge { display: inline-block; padding: 3px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; margin-bottom: 8px; }
.cp-visual { margin: 0 auto; width: 100px; height: 130px; overflow: hidden; }
.cp-svg :deep(svg) { width: 100%; height: 100%; }
.cp-name { font-size: 15px; font-weight: 700; color: #17231b; }
.cp-status { font-size: 12px; padding: 2px 10px; border-radius: 10px; display: inline-block; margin: 4px 0; }
.cp-status.watered { background: #eaf5ec; color: #2f9142; }
.cp-status.dormant { background: #F4F4F5; color: #909399; }
.cp-status.growing { background: #FDF6EC; color: #E6A23C; }
.cp-status.matured { background: #D4EDDA; color: #155724; }

.cp-stats { display: flex; gap: 16px; justify-content: center; margin: 8px 0; }
.cp-stat { display: flex; flex-direction: column; font-size: 11px; color: #65766b; }
.cps-num { font-size: 16px; font-weight: 700; color: #2f9142; }

.cp-progress { font-size: 12px; margin-top: 8px; }
.cpp-row { display: flex; align-items: center; gap: 6px; margin: 4px 0; }
.cpp-row span:first-child { width: 28px; color: #718077; font-size: 11px; text-align: right; }
.cpp-row span:last-child { color: #2f9142; font-weight: 600; font-size: 11px; min-width: 40px; }
.cpp-bar { flex: 1; height: 6px; background: #e6ebe6; border-radius: 3px; overflow: hidden; }
.cpp-fill { height: 100%; background: linear-gradient(90deg, #66BB6A, #2f9142); border-radius: 3px; transition: width 0.5s; }
.cpp-target { color: #2f9142; font-weight: 600; margin-top: 4px; }
.cp-matured { font-size: 13px; font-weight: 600; color: #2f9142; padding: 12px; }

/* 花园 */
.garden-empty { text-align: center; padding: 24px 8px; }
.garden-empty p { font-size: 13px; color: #65766b; }
.garden-empty .garden-hint { font-size: 11px; color: #909399; margin-top: 4px; }
.garden-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 8px; }
.garden-plant { border: 2px solid #d9e3da; border-radius: 10px; padding: 8px; text-align: center; background: #fafcfa; }
.garden-plant.current { border-style: solid; background: #f8fdf8; }
.garden-plant.harvested { opacity: 0.85; }
.gp-svg { width: 55px; height: 75px; margin: 0 auto; overflow: hidden; }
.gp-svg :deep(svg) { width: 100%; height: 100%; }
.gp-name { font-size: 11px; font-weight: 600; color: #17231b; margin-top: 4px; }
.gp-badge { font-size: 10px; padding: 1px 8px; border-radius: 8px; display: inline-block; margin-top: 3px; }
.gp-badge.current { background: #eaf5ec; color: #2f9142; }
.gp-badge.harvested { background: #F4F4F5; color: #909399; }
.gp-stats-row { display: flex; gap: 8px; justify-content: center; font-size: 10px; color: #718077; margin-top: 3px; }
.gp-date { font-size: 10px; color: #b0b8b1; margin-top: 2px; }
</style>
