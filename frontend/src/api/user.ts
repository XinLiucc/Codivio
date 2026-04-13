import http, { ApiResponse } from '@/utils/http'

export interface UserSearchResult {
  userId: number
  username: string
  email: string
  exists: boolean
}

export const userAPI = {
  /**
   * 根据用户名关键字搜索用户（最多10条）
   * 对应后端: GET /api/v1/users/search?keyword=xxx
   */
  searchUsers: (keyword: string) => {
    return http.get<ApiResponse<UserSearchResult[]>>('/users/search', { params: { keyword } })
  }
}
