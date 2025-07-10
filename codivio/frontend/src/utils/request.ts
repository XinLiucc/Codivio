import axios, { type AxiosInstance, type AxiosResponse, type AxiosError } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import Cookies from 'js-cookie'

// 响应数据类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加JWT token
    const token = Cookies.get('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error: AxiosError) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>): any => {
    const { code, message, data } = response.data
    
    // 成功响应
    if (code === 200) {
      return response.data
    }
    
    // 业务错误
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  async (error: AxiosError<ApiResponse>) => {
    console.error('Response error:', error)
    
    // 处理网络错误
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络设置')
      return Promise.reject(error)
    }
    
    const { status, data } = error.response
    const message = data?.message || '请求失败'
    
    switch (status) {
      case 401:
        // Token过期或无效
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
        break
        
      case 403:
        ElMessage.error('权限不足，无法访问该资源')
        break
        
      case 404:
        ElMessage.error('请求的资源不存在')
        break
        
      case 409:
        // 资源冲突，如用户名已存在
        ElMessage.error(message)
        break
        
      case 429:
        ElMessage.error('请求过于频繁，请稍后再试')
        break
        
      case 500:
        ElMessage.error('服务器内部错误，请稍后再试')
        break
        
      default:
        ElMessage.error(message)
    }
    
    return Promise.reject(error)
  }
)

// 带loading的请求函数
export const requestWithLoading = async <T = any>(
  requestFn: () => Promise<ApiResponse<T>>,
  loadingText = '请求中...'
): Promise<T> => {
  const loading = ElLoading.service({
    lock: true,
    text: loadingText,
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const result = await requestFn()
    return result.data
  } finally {
    loading.close()
  }
}

export default request