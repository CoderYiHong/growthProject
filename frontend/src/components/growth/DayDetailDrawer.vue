<script setup>
import { computed } from 'vue'
import { useStudyPlanStore } from '@/stores/studyPlan'

const props = defineProps({ visible: Boolean, day: Object })
const emit = defineEmits(['update:visible'])
const store = useStudyPlanStore()

const grouped = computed(() => {
  if (!props.day?.records) return {}
  const map = {}
  for (const rec of props.day.records) {
    const plan = store.getPlanById(rec.planId)
    const cat = plan?.category || '其他'
    if (!map[cat]) map[cat] = { totalMins: 0, plans: {} }
    map[cat].totalMins += (rec.duration || 0)
    const pkey = rec.planId
    if (!map[cat].plans[pkey]) map[cat].plans[pkey] = { plan, totalMins: 0, groups: {} }
    map[cat].plans[pkey].totalMins += (rec.duration || 0)
    const gkey = rec.groupId || '__none'
    if (!map[cat].plans[pkey].groups[gkey]) map[cat].plans[pkey].groups[gkey] = { group: plan?.groups?.find(g => g.id === rec.groupId), totalMins: 0, items: {} }
    map[cat].plans[pkey].groups[gkey].totalMins += (rec.duration || 0)
    const ikey = rec.itemId || '__none'
    if (!map[cat].plans[pkey].groups[gkey].items[ikey]) map[cat].plans[pkey].groups[gkey].items[ikey] = { item: store.getItemById(rec.planId, rec.itemId), totalMins: 0, records: [] }
    map[cat].plans[pkey].groups[gkey].items[ikey].totalMins += (rec.duration || 0)
    map[cat].plans[pkey].groups[gkey].items[ikey].records.push(rec)
  }
  return map
})

function fmt(m) { return m >= 60 ? `${Math.floor(m/60)}h${m%60 > 0 ? m%60+'min' : ''}` : `${m}min` }
</script>

<template>
  <Teleport to="body">
    <Transition name="drawer">
      <div v-if="visible" class="drawer-overlay" @click.self="emit('update:visible', false)">
        <div class="drawer-panel">
          <button class="close" @click="emit('update:visible', false)">✕</button>
          <h3>{{ day?.date }}</h3>
          <div v-if="day?.records?.length" style="margin-top:12px">
            <p style="font-size:13px;color:var(--color-secondary);margin-bottom:12px">总专注 {{ fmt(day.records.reduce((s,c)=>s+(c.duration||0),0)) }} · {{ day.records.length }} 条记录</p>
            <div v-for="(catData, cat) in grouped" :key="cat" style="margin-bottom:16px">
              <h4 style="font-size:14px;color:var(--color-title);margin-bottom:6px">{{ cat }}，共 {{ fmt(catData.totalMins) }}</h4>
              <div v-for="(pData, pId) in catData.plans" :key="pId" style="margin-left:8px;margin-bottom:8px">
                <div style="font-weight:600;font-size:13px">{{ pData.plan?.name || '未知计划' }}</div>
                <div v-for="(gData, gId) in pData.groups" :key="gId" style="margin-left:12px;font-size:12px;color:var(--color-secondary)">
                  <div v-if="gId !== '__none'" style="margin-bottom:2px">{{ gData.group?.name || '未分组' }}</div>
                  <div v-for="(iData, iId) in gData.items" :key="iId" style="margin-left:12px;font-size:12px;display:flex;justify-content:space-between;padding:2px 0">
                    <span>{{ iData.item?.title || '自由专注' }}</span>
                    <span style="color:var(--color-primary);font-weight:600">{{ fmt(iData.totalMins) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else style="color:var(--color-muted);font-size:13px;text-align:center;padding:32px">当天没有学习记录</div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.drawer-overlay { position: fixed; inset: 0; z-index: 2000; background: rgba(0,0,0,0.25); display: flex; justify-content: flex-end; }
.drawer-panel { width: 420px; max-width: 100vw; height: 100vh; overflow-y: auto; background: rgba(255,255,255,0.97); padding: 24px; box-shadow: var(--shadow-xl); }
.close { position: absolute; top: 16px; right: 16px; border: none; background: rgba(0,0,0,0.05); width: 32px; height: 32px; border-radius: 50%; cursor: pointer; font-size: 14px; }
.drawer-enter-active, .drawer-leave-active { transition: all 0.3s; }
.drawer-enter-from, .drawer-leave-to { opacity: 0; }
.drawer-enter-from .drawer-panel, .drawer-leave-to .drawer-panel { transform: translateX(100%); }
.drawer-enter-active .drawer-panel, .drawer-leave-active .drawer-panel { transition: transform 0.3s; }
</style>
