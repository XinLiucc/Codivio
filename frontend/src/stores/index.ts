import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 创建 pinia 实例
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

export default pinia

// 导出所有 stores
export { useAuthStore } from './auth'
export { useProjectStore } from './project'
export { useAppStore } from './app'

// 导出类型
export type { User } from './auth'
export type { Project, ProjectMember } from './project'
export type { ThemeMode, Language } from './app'
