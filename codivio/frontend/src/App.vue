<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 应用启动时初始化用户状态
onMounted(() => {
  userStore.initUserState()
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style>