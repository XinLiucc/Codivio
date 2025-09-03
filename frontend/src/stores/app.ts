import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'auto'
export type Language = 'zh-CN' | 'en-US'

export const useAppStore = defineStore(
  'app',
  () => {
    // 状态
    const theme = ref<ThemeMode>('light')
    const language = ref<Language>('zh-CN')
    const sidebarCollapsed = ref<boolean>(false)
    const loading = ref<boolean>(false)
    const loadingText = ref<string>('')

    // 操作
    const setTheme = (newTheme: ThemeMode) => {
      theme.value = newTheme
    }

    const setLanguage = (newLanguage: Language) => {
      language.value = newLanguage
    }

    const toggleSidebar = () => {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }

    const setSidebarCollapsed = (collapsed: boolean) => {
      sidebarCollapsed.value = collapsed
    }

    const setLoading = (isLoading: boolean, text: string = '') => {
      loading.value = isLoading
      loadingText.value = text
    }

    const showLoading = (text: string = '加载中...') => {
      loading.value = true
      loadingText.value = text
    }

    const hideLoading = () => {
      loading.value = false
      loadingText.value = ''
    }

    return {
      // 状态
      theme,
      language,
      sidebarCollapsed,
      loading,
      loadingText,
      // 操作
      setTheme,
      setLanguage,
      toggleSidebar,
      setSidebarCollapsed,
      setLoading,
      showLoading,
      hideLoading,
    }
  },
  {
    persist: true,
  }
)
