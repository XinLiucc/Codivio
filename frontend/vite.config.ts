import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue'],
      dts: true,
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    host: '0.0.0.0',
    // 配置代理解决CORS跨域问题
    proxy: {
      // 所有以 /api 开头的请求都代理到后端
      '/api': {
        target: 'http://localhost:8080',  // 后端API网关地址
        changeOrigin: true,               // 改变origin头
        secure: false,                    // 如果是https，需要设置为true
        rewrite: (path) => {
          console.log(`代理请求: ${path} -> http://localhost:8080${path}`)
          return path
        }
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
})
