<script setup>
import { ref, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import PreloaderOverlay from '@/components/common/PreloaderOverlay.vue'

const showPreloader = ref(false)
const appReady = ref(false)

onMounted(() => {
  const shown = sessionStorage.getItem('orange_preloader_shown')
  if (!shown) {
    showPreloader.value = true
  } else {
    appReady.value = true
  }
})

function onPreloaderComplete() {
  sessionStorage.setItem('orange_preloader_shown', 'true')
  showPreloader.value = false
  requestAnimationFrame(() => {
    appReady.value = true
  })
}
</script>

<template>
  <PreloaderOverlay
    v-if="showPreloader"
    @complete="onPreloaderComplete"
  />

  <router-view v-if="appReady" v-slot="{ Component, route }">
    <transition name="page" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>
