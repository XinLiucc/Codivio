<template>
  <div class="settings-container">
    <div class="settings-header">
      <h2>偏好设置</h2>
      <p class="settings-subtitle">个性化您的 Codivio 体验</p>
    </div>

    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>外观主题</span>
        </div>
      </template>

      <el-form label-width="100px" class="settings-form">
        <el-form-item label="主题模式">
          <el-radio-group v-model="settings.theme" @change="handleThemeChange">
            <el-radio value="light">
              <el-icon><Sunny /></el-icon>
              浅色模式
            </el-radio>
            <el-radio value="dark">
              <el-icon><Moon /></el-icon>
              深色模式
            </el-radio>
            <el-radio value="auto">
              <el-icon><Monitor /></el-icon>
              跟随系统
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="主题色">
          <div class="color-picker-group">
            <div
              v-for="color in themeColors"
              :key="color.value"
              class="color-option"
              :class="{ active: settings.primaryColor === color.value }"
              :title="color.name"
              @click="handleColorChange(color.value)"
            >
              <div class="color-preview" :style="{ backgroundColor: color.value }">
                <el-icon v-if="settings.primaryColor === color.value" class="check-icon"><Check /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="紧凑模式">
          <el-switch v-model="settings.compact" @change="handleCompactChange" />
          <span class="form-tip">启用后界面元素间距更小，可显示更多内容</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveSettings">保存设置</el-button>
          <el-button @click="resetSettings">恢复默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor, Sunny, Moon, Check } from '@element-plus/icons-vue'

const saving = ref(false)

const settings = reactive({
  theme: 'light',
  primaryColor: '#409eff',
  compact: false,
})

const themeColors = [
  { name: '蓝色', value: '#409eff' },
  { name: '绿色', value: '#67c23a' },
  { name: '橙色', value: '#e6a23c' },
  { name: '红色', value: '#f56c6c' },
  { name: '紫色', value: '#9c88ff' },
  { name: '粉色', value: '#f093fb' },
]

const handleThemeChange = (theme: string) => {
  applyTheme(theme)
  ElMessage.success(`已切换到${theme === 'light' ? '浅色' : theme === 'dark' ? '深色' : '跟随系统'}模式`)
}

const handleColorChange = (color: string) => {
  settings.primaryColor = color
  document.documentElement.style.setProperty('--el-color-primary', color)
  ElMessage.success('主题色已更新')
}

const handleCompactChange = (compact: boolean) => {
  document.documentElement.classList.toggle('compact', compact)
  ElMessage.success(`已${compact ? '启用' : '禁用'}紧凑模式`)
}

const applyTheme = (theme: string) => {
  const html = document.documentElement
  if (theme === 'dark') {
    html.classList.add('dark')
  } else if (theme === 'light') {
    html.classList.remove('dark')
  } else {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    html.classList.toggle('dark', prefersDark)
  }
}

const saveSettings = async () => {
  saving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    localStorage.setItem('codivio-settings', JSON.stringify(settings))
    ElMessage.success('设置已保存')
  } finally {
    saving.value = false
  }
}

const resetSettings = () => {
  Object.assign(settings, { theme: 'light', primaryColor: '#409eff', compact: false })
  applyTheme('light')
  document.documentElement.style.setProperty('--el-color-primary', '#409eff')
  document.documentElement.classList.remove('compact')
  localStorage.removeItem('codivio-settings')
  ElMessage.success('已恢复默认设置')
}

onMounted(() => {
  const saved = localStorage.getItem('codivio-settings')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      Object.assign(settings, parsed)
      applyTheme(settings.theme)
      document.documentElement.style.setProperty('--el-color-primary', settings.primaryColor)
      document.documentElement.classList.toggle('compact', settings.compact)
    } catch {
      // 忽略解析错误
    }
  }
})
</script>

<style scoped>
.settings-container {
  padding: 32px;
  max-width: 700px;
}

.settings-header {
  margin-bottom: 24px;
}

.settings-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #1a1a2e;
}

.settings-subtitle {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.settings-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.settings-form {
  padding: 8px 0;
}

.color-picker-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.color-option {
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: border-color 0.15s;
}

.color-option.active {
  border-color: #303133;
}

.color-preview {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  color: #fff;
  font-size: 16px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 13px;
}
</style>
