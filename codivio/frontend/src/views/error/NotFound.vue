<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center px-4">
    <div class="max-w-md w-full text-center">
      <div class="mb-8">
        <el-icon :size="120" color="#9ca3af">
          <Warning />
        </el-icon>
      </div>
      <h1 class="text-6xl font-bold text-gray-900 mb-4">404</h1>
      <h2 class="text-2xl font-semibold text-gray-700 mb-4">页面未找到</h2>
      <p class="text-gray-600 mb-8">
        抱歉，您访问的页面不存在或已被移动。
      </p>
      <div class="space-y-4">
        <el-button type="primary" @click="$router.push('/')">
          返回首页
        </el-button>
        <el-button @click="$router.go(-1)">
          返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Warning } from '@element-plus/icons-vue'
</script>