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
          <div class="theme-options">
            <div
              v-for="opt in themeOptions"
              :key="opt.value"
              class="theme-option"
              :class="{ active: theme === opt.value }"
              @click="selectTheme(opt.value)"
            >
              <el-icon class="theme-icon"><component :is="opt.icon" /></el-icon>
              <span>{{ opt.label }}</span>
            </div>
          </div>
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor, Sunny, Moon } from '@element-plus/icons-vue'

const saving = ref(false)
const theme = ref('light')

const themeOptions = [
  { value: 'light', label: '浅色模式', icon: Sunny },
  { value: 'dark',  label: '深色模式', icon: Moon },
  { value: 'auto',  label: '跟随系统', icon: Monitor },
]

const applyTheme = (val: string) => {
  const html = document.documentElement
  if (val === 'dark') {
    html.classList.add('dark')
  } else if (val === 'light') {
    html.classList.remove('dark')
  } else {
    html.classList.toggle('dark', window.matchMedia('(prefers-color-scheme: dark)').matches)
  }
}

const selectTheme = (val: string) => {
  theme.value = val
  applyTheme(val)
}

const saveSettings = async () => {
  saving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    localStorage.setItem('codivio-settings', JSON.stringify({ theme: theme.value }))
    ElMessage.success('设置已保存')
  } finally {
    saving.value = false
  }
}

const resetSettings = () => {
  theme.value = 'light'
  applyTheme('light')
  localStorage.removeItem('codivio-settings')
  ElMessage.success('已恢复默认设置')
}

onMounted(() => {
  const saved = localStorage.getItem('codivio-settings')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      theme.value = parsed.theme || 'light'
      applyTheme(theme.value)
    } catch {}
  }
})
</script>

<style scoped>
.settings-container {
  padding: 32px;
  max-width: 700px;
  min-height: 100vh;
  background: var(--app-bg);
}

.settings-header {
  margin-bottom: 24px;
}

.settings-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: var(--logo-text-color);
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

/* 主题选择卡片 */
.theme-options {
  display: flex;
  gap: 12px;
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  border: 2px solid var(--sidebar-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
  color: var(--nav-text);
  background: var(--sidebar-bg);
  font-size: 13px;
}

.theme-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.theme-option.active {
  border-color: #409eff;
  color: #409eff;
  background: var(--nav-active-bg);
}

.theme-icon {
  font-size: 24px;
}
</style>
