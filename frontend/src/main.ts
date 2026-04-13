import { createApp } from 'vue'
import App from './App.vue'
import pinia from './stores'
import router from "@/router"
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'

// 启动时应用已保存的外观设置
const savedSettings = localStorage.getItem('codivio-settings')
if (savedSettings) {
  try {
    const { theme } = JSON.parse(savedSettings)
    if (theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else if (theme === 'auto') {
      if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
        document.documentElement.classList.add('dark')
      }
    }
  } catch {}
}

const app = createApp(App)

// 使用 Pinia
app.use(pinia)
app.use(router)

app.mount('#app')
