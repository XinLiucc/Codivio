<template>
  <div class="home">
    <h1>欢迎来到 Codivio</h1>
    <p>代码协作平台</p>
    <el-button type="primary" @click="$router.push('/login')">
      登录
    </el-button>
  </div>
</template>

<script setup lang="ts">

</script>

<style scoped>
.home {
  text-align: center;
  padding: 100px 20px;
}
</style>