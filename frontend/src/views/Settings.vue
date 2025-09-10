<template>
  <div class="settings-container">
    <div class="settings-header">
      <h2>账户设置</h2>
      <p class="settings-subtitle">个性化您的Codivio体验</p>
    </div>

    <el-row :gutter="24">
      <!-- 设置菜单 -->
      <el-col :span="6">
        <el-card>
          <el-menu
            v-model:default-active="activeTab"
            class="settings-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="appearance">
              <el-icon><Monitor /></el-icon>
              <span>外观主题</span>
            </el-menu-item>
            <el-menu-item index="notifications">
              <el-icon><Bell /></el-icon>
              <span>通知设置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全与隐私</span>
            </el-menu-item>
            <el-menu-item index="language">
              <el-icon><Translation /></el-icon>
              <span>语言与区域</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 设置内容 -->
      <el-col :span="18">
        <!-- 外观主题设置 -->
        <el-card v-show="activeTab === 'appearance'" title="外观主题">
          <template #header>
            <div class="card-header">
              <span>外观主题</span>
              <el-icon><Monitor /></el-icon>
            </div>
          </template>

          <el-form label-width="120px">
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
                  @click="handleColorChange(color.value)"
                >
                  <div class="color-preview" :style="{ backgroundColor: color.value }"></div>
                  <span>{{ color.name }}</span>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="紧凑模式">
              <el-switch
                v-model="settings.compact"
                @change="handleCompactChange"
              />
              <div class="form-tip">启用紧凑模式可以在屏幕上显示更多内容</div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 通知设置 -->
        <el-card v-show="activeTab === 'notifications'" title="通知设置">
          <template #header>
            <div class="card-header">
              <span>通知设置</span>
              <el-icon><Bell /></el-icon>
            </div>
          </template>

          <el-form label-width="120px">
            <el-form-item label="桌面通知">
              <el-switch v-model="settings.notifications.desktop" />
              <div class="form-tip">允许在桌面上显示通知</div>
            </el-form-item>

            <el-form-item label="邮件通知">
              <el-switch v-model="settings.notifications.email" />
              <div class="form-tip">重要事件将通过邮件通知</div>
            </el-form-item>

            <el-form-item label="项目活动">
              <el-checkbox-group v-model="settings.notifications.projectEvents">
                <el-checkbox value="new_member">新成员加入</el-checkbox>
                <el-checkbox value="file_change">文件变更</el-checkbox>
                <el-checkbox value="comment">评论回复</el-checkbox>
                <el-checkbox value="mention">@我的消息</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="系统通知">
              <el-checkbox-group v-model="settings.notifications.systemEvents">
                <el-checkbox value="maintenance">系统维护</el-checkbox>
                <el-checkbox value="updates">功能更新</el-checkbox>
                <el-checkbox value="security">安全提醒</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item>
              <el-button @click="testNotification">
                <el-icon><Bell /></el-icon>
                测试通知
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 安全设置 -->
        <el-card v-show="activeTab === 'security'" title="安全与隐私">
          <template #header>
            <div class="card-header">
              <span>安全与隐私</span>
              <el-icon><Lock /></el-icon>
            </div>
          </template>

          <el-form label-width="120px">
            <el-form-item label="两步验证">
              <el-switch v-model="settings.security.twoFactor" />
              <div class="form-tip">为您的账户添加额外的安全保护</div>
            </el-form-item>

            <el-form-item label="登录提醒">
              <el-switch v-model="settings.security.loginNotification" />
              <div class="form-tip">异常登录时发送通知</div>
            </el-form-item>

            <el-form-item label="会话管理">
              <el-button @click="viewActiveSessions">
                <el-icon><Connection /></el-icon>
                查看活动会话
              </el-button>
              <div class="form-tip">管理您在其他设备上的登录会话</div>
            </el-form-item>

            <el-form-item label="数据导出">
              <el-button @click="exportData">
                <el-icon><Download /></el-icon>
                导出我的数据
              </el-button>
              <div class="form-tip">下载您在Codivio上的所有数据</div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 语言设置 -->
        <el-card v-show="activeTab === 'language'" title="语言与区域">
          <template #header>
            <div class="card-header">
              <span>语言与区域</span>
              <el-icon><Translation /></el-icon>
            </div>
          </template>

          <el-form label-width="120px">
            <el-form-item label="界面语言">
              <el-select v-model="settings.language" @change="handleLanguageChange">
                <el-option value="zh-CN" label="简体中文">
                  <span>🇨🇳 简体中文</span>
                </el-option>
                <el-option value="en-US" label="English">
                  <span>🇺🇸 English</span>
                </el-option>
                <el-option value="ja-JP" label="日本語">
                  <span>🇯🇵 日本語</span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="时区">
              <el-select v-model="settings.timezone">
                <el-option value="Asia/Shanghai" label="北京时间 (UTC+8)" />
                <el-option value="America/New_York" label="纽约时间 (UTC-5)" />
                <el-option value="Europe/London" label="伦敦时间 (UTC+0)" />
                <el-option value="Asia/Tokyo" label="东京时间 (UTC+9)" />
              </el-select>
            </el-form-item>

            <el-form-item label="日期格式">
              <el-radio-group v-model="settings.dateFormat">
                <el-radio value="YYYY-MM-DD">2025-09-06</el-radio>
                <el-radio value="MM/DD/YYYY">09/06/2025</el-radio>
                <el-radio value="DD/MM/YYYY">06/09/2025</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 保存按钮 -->
    <div class="settings-footer">
      <el-button @click="resetSettings">重置为默认</el-button>
      <el-button type="primary" :loading="saving" @click="saveSettings">
        保存设置
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import {
  Monitor, Bell, Lock, Translation, Sunny, Moon,
  Connection, Download
} from '@element-plus/icons-vue'

// 当前激活的标签
const activeTab = ref('appearance')

// 保存状态
const saving = ref(false)

// 用户设置
const settings = reactive({
  // 外观设置
  theme: 'light',
  primaryColor: '#409eff',
  compact: false,
  
  // 通知设置
  notifications: {
    desktop: true,
    email: true,
    projectEvents: ['new_member', 'file_change', 'mention'],
    systemEvents: ['security', 'updates']
  },
  
  // 安全设置
  security: {
    twoFactor: false,
    loginNotification: true
  },
  
  // 语言设置
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  dateFormat: 'YYYY-MM-DD'
})

// 主题色选项
const themeColors = [
  { name: '蓝色', value: '#409eff' },
  { name: '绿色', value: '#67c23a' },
  { name: '橙色', value: '#e6a23c' },
  { name: '红色', value: '#f56c6c' },
  { name: '紫色', value: '#9c88ff' },
  { name: '粉色', value: '#f093fb' }
]

// 菜单选择处理
const handleMenuSelect = (key: string) => {
  activeTab.value = key
}

// 主题变更
const handleThemeChange = (theme: string) => {
  ElMessage.success(`已切换到${theme === 'light' ? '浅色' : theme === 'dark' ? '深色' : '自动'}模式`)
  applyTheme(theme)
}

// 主题色变更
const handleColorChange = (color: string) => {
  settings.primaryColor = color
  applyPrimaryColor(color)
  ElMessage.success('主题色已更新')
}

// 紧凑模式切换
const handleCompactChange = (compact: boolean) => {
  ElMessage.success(`已${compact ? '启用' : '禁用'}紧凑模式`)
  applyCompactMode(compact)
}

// 语言变更
const handleLanguageChange = (lang: string) => {
  ElMessage.success('语言设置已更新，重新加载页面生效')
}

// 应用主题
const applyTheme = (theme: string) => {
  const html = document.documentElement
  if (theme === 'dark') {
    html.classList.add('dark')
  } else if (theme === 'light') {
    html.classList.remove('dark')
  } else {
    // 自动模式，根据系统偏好
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    html.classList.toggle('dark', prefersDark)
  }
}

// 应用主题色
const applyPrimaryColor = (color: string) => {
  document.documentElement.style.setProperty('--el-color-primary', color)
}

// 应用紧凑模式
const applyCompactMode = (compact: boolean) => {
  document.documentElement.classList.toggle('compact', compact)
}

// 测试通知
const testNotification = () => {
  if (settings.notifications.desktop) {
    ElNotification({
      title: '测试通知',
      message: '这是一条测试通知消息',
      type: 'info',
      duration: 3000
    })
  } else {
    ElMessage.info('请先启用桌面通知')
  }
}

// 查看活动会话
const viewActiveSessions = () => {
  ElMessage.info('会话管理功能开发中...')
}

// 导出数据
const exportData = () => {
  ElMessage.info('数据导出功能开发中...')
}

// 保存设置
const saveSettings = async () => {
  saving.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 保存到本地存储
    localStorage.setItem('codivio-settings', JSON.stringify(settings))
    
    ElMessage.success('设置已保存')
    
  } catch (error: any) {
    ElMessage.error('保存设置失败')
    console.error('Save settings error:', error)
  } finally {
    saving.value = false
  }
}

// 重置设置
const resetSettings = () => {
  Object.assign(settings, {
    theme: 'light',
    primaryColor: '#409eff',
    compact: false,
    notifications: {
      desktop: true,
      email: true,
      projectEvents: ['new_member', 'file_change', 'mention'],
      systemEvents: ['security', 'updates']
    },
    security: {
      twoFactor: false,
      loginNotification: true
    },
    language: 'zh-CN',
    timezone: 'Asia/Shanghai',
    dateFormat: 'YYYY-MM-DD'
  })
  
  // 应用默认设置
  applyTheme('light')
  applyPrimaryColor('#409eff')
  applyCompactMode(false)
  
  ElMessage.success('设置已重置为默认值')
}

// 加载设置
const loadSettings = () => {
  try {
    const saved = localStorage.getItem('codivio-settings')
    if (saved) {
      const parsedSettings = JSON.parse(saved)
      Object.assign(settings, parsedSettings)
      
      // 应用已保存的设置
      applyTheme(settings.theme)
      applyPrimaryColor(settings.primaryColor)
      applyCompactMode(settings.compact)
    }
  } catch (error) {
    console.error('Load settings error:', error)
  }
}

// 组件挂载时加载设置
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.settings-header {
  margin-bottom: 24px;
}

.settings-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.settings-subtitle {
  color: #606266;
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-menu {
  border: none;
}

.settings-menu .el-menu-item {
  border-radius: 6px;
  margin-bottom: 4px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.color-picker-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.color-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.color-option:hover {
  border-color: #409eff;
}

.color-option.active {
  border-color: #409eff;
  background: #f0f9ff;
}

.color-preview {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.settings-footer {
  margin-top: 24px;
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.settings-footer .el-button {
  margin-left: 12px;
}

/* 深色主题支持 */
:global(.dark) .settings-container {
  background-color: #1a1a1a;
}

:global(.dark) .settings-header h2 {
  color: #e5eaf3;
}

:global(.dark) .settings-subtitle {
  color: #a3a6ad;
}

/* 紧凑模式支持 */
:global(.compact) .el-form-item {
  margin-bottom: 16px;
}

:global(.compact) .el-card {
  margin-bottom: 16px;
}
</style>