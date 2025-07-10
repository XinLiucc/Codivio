<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-4xl mx-auto">
      <div class="bg-white rounded-lg shadow p-6 mb-6">
        <h1 class="text-2xl font-bold text-gray-900 mb-4">API测试页面</h1>
        <p class="text-gray-600 mb-6">测试后端API接口的连通性和功能</p>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 健康检查 -->
          <el-card header="系统健康检查">
            <div class="space-y-4">
              <el-button @click="testHealth" :loading="testing.health">
                测试系统状态
              </el-button>
              <el-button @click="testAuthTest" :loading="testing.auth">
                测试认证接口
              </el-button>
              <div v-if="results.health" class="p-4 bg-green-50 rounded-lg">
                <pre class="text-sm">{{ JSON.stringify(results.health, null, 2) }}</pre>
              </div>
            </div>
          </el-card>

          <!-- 用户测试 -->
          <el-card header="用户API测试">
            <div class="space-y-4">
              <el-button @click="testCurrentUser" :loading="testing.user">
                获取当前用户
              </el-button>
              <el-button @click="testDebugDB" :loading="testing.db">
                测试数据库
              </el-button>
              <div v-if="results.user" class="p-4 bg-blue-50 rounded-lg">
                <pre class="text-sm">{{ JSON.stringify(results.user, null, 2) }}</pre>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 错误日志 -->
        <div v-if="errors.length > 0" class="mt-6">
          <h3 class="text-lg font-semibold text-red-600 mb-2">错误日志</h3>
          <div class="space-y-2">
            <div 
              v-for="(error, index) in errors" 
              :key="index"
              class="p-4 bg-red-50 border border-red-200 rounded-lg"
            >
              <div class="text-sm text-red-800">{{ error }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { authAPI, healthAPI } from '@/api/auth'

// 定义测试状态类型
interface TestingState {
  health: boolean
  auth: boolean
  user: boolean
  db: boolean
}

// 定义结果类型 - 修复类型错误
interface TestResults {
  health: any | null
  user: any | null
}

const testing = reactive<TestingState>({
  health: false,
  auth: false,
  user: false,
  db: false
})

// 使用类型注解修复类型错误
const results = reactive<TestResults>({
  health: null,
  user: null
})

const errors = ref<string[]>([])

// 测试健康检查
const testHealth = async () => {
  testing.health = true
  try {
    const response = await healthAPI.checkHealth()
    results.health = response
    ElMessage.success('健康检查成功')
  } catch (error: any) {
    const message = error?.message || String(error)
    errors.value.push(`健康检查失败: ${message}`)
    ElMessage.error('健康检查失败')
  } finally {
    testing.health = false
  }
}

// 测试认证接口
const testAuthTest = async () => {
  testing.auth = true
  try {
    const response = await healthAPI.authTest()
    ElMessage.success('认证测试成功')
    console.log('Auth test result:', response)
  } catch (error: any) {
    const message = error?.message || String(error)
    errors.value.push(`认证测试失败: ${message}`)
    ElMessage.error('认证测试失败，请先登录')
  } finally {
    testing.auth = false
  }
}

// 测试获取当前用户
const testCurrentUser = async () => {
  testing.user = true
  try {
    const response = await authAPI.getCurrentUser()
    results.user = response
    ElMessage.success('获取用户信息成功')
  } catch (error: any) {
    const message = error?.message || String(error)
    errors.value.push(`获取用户信息失败: ${message}`)
    ElMessage.error('获取用户信息失败，请先登录')
  } finally {
    testing.user = false
  }
}

// 测试数据库连接
const testDebugDB = async () => {
  testing.db = true
  try {
    // 直接请求调试接口
    const response = await fetch('/api/v1/debug/test-db')
    const result = await response.json()
    ElMessage.success('数据库测试成功')
    console.log('DB test result:', result)
  } catch (error: any) {
    const message = error?.message || String(error)
    errors.value.push(`数据库测试失败: ${message}`)
    ElMessage.error('数据库测试失败')
  } finally {
    testing.db = false
  }
}
</script>

<style scoped>
/* 组件样式 */
</style>