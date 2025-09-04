<template>
  <div class="dashboard">
    <h1>仪表板</h1>
    <p>欢迎来到项目管理界面</p>
    <el-button @click="handleLogout">退出登录</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const handleLogout = () => {
  // 暂时简单处理
  router.push('/login')
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}
</style>
