/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_BASE_URL: string
  readonly VITE_APP_API_BASE_URL: string
  readonly VITE_APP_WS_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}