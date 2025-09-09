import axios, { AxiosResponse, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// 定义后端响应数据类型
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 创建axios实例
const http = axios.create({
  // 基础URL - 开发环境使用相对路径，生产环境使用完整URL
  baseURL: import.meta.env.DEV ? '/api/v1' : 'http://localhost:8080/api/v1',
  // 请求超时时间（10秒）
  timeout: 10000,
  // 默认请求头
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 请求拦截器 - 在发送请求前执行
http.interceptors.request.use(
  (config) => {
    // 自动添加JWT token到请求头
    // 为什么在拦截器中添加？
    // 1. 自动化 - 每个需要认证的请求都会自动带上token
    // 2. 实时性 - 从Pinia store中获取最新的token状态
    // 3. 统一性 - 保证所有请求的认证格式一致
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    // 开发环境下打印请求信息，方便调试
    if (import.meta.env.DEV) {
      console.log('🚀 发送请求:', config.method?.toUpperCase(), config.url, config.data)
    }
    
    return config
  },
  (error) => {
    console.error('❌ 请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 在接收响应后执行
http.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    
    // 开发环境下打印响应信息
    if (import.meta.env.DEV) {
      console.log('📥 收到响应:', response.status, response.config.url, data)
    }
    
    // 处理业务状态码
    // 为什么要在这里处理业务错误？
    // 1. 统一性 - 所有组件都按照相同的方式处理业务错误
    // 2. 自动化 - 自动显示错误提示，组件中不需要重复处理
    // 3. 认证处理 - 统一处理token过期等认证相关错误
    if (data.code === 200) {
      // 业务成功，返回完整的响应对象
      return response
    } else {
      // 业务失败，根据你的业务错误码进行处理
      handleBusinessError(data.code, data.message)
      
      // 抛出业务错误，让调用方的catch可以捕获
      const businessError: any = new Error(data.message)
      businessError.code = data.code
      businessError.response = response
      throw businessError
    }
  },
  (error: AxiosError) => {
    // HTTP层面的错误处理
    // 这里处理的是网络错误、服务器崩溃、接口不存在等问题
    
    const { response } = error
    
    if (response) {
      // 服务器返回了HTTP错误状态码
      switch (response.status) {
        case 404:
          ElMessage.error('请求的接口不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误，请联系管理员')
          break
        case 502:
          ElMessage.error('网关错误')
          break
        case 503:
          ElMessage.error('服务暂时不可用')
          break
        default:
          ElMessage.error(`HTTP错误: ${response.status}`)
      }
    } else if (error.code === 'ECONNABORTED') {
      // 请求超时
      ElMessage.error('请求超时，请检查网络连接')
    } else if (error.code === 'ERR_NETWORK') {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      // 其他未知错误
      ElMessage.error('请求失败，请稍后重试')
    }
    
    console.error('❌ HTTP请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 处理业务错误码
 * 根据你的接口设计文档中定义的业务错误码进行处理
 */
function handleBusinessError(code: number, message: string) {
  const authStore = useAuthStore()
  
  switch (code) {
    case 401:
      // Token无效或已过期
      ElMessage.error('登录已过期，请重新登录')
      authStore.clearAuth()
      router.push('/login')
      break
    case 403:
      // 权限不足
      ElMessage.error('权限不足，无法执行该操作')
      break
    case 400:
      // 参数验证失败
      ElMessage.error(message || '请求参数有误')
      break
    case 429:
      // 请求频率超限
      ElMessage.error('操作过于频繁，请稍后再试')
      break
    default:
      // 其他业务错误
      ElMessage.error(message || `操作失败 (错误码: ${code})`)
  }
}

export default http

// 导出响应类型，方便其他地方使用
export type { ApiResponse }