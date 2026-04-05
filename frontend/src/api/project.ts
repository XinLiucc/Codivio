import http, { ApiResponse } from '@/utils/http'

// 项目相关的数据类型
export interface ProjectCreateForm {
  name: string
  description?: string
  language: string
  status?: number  // 项目状态，默认为1(进行中)
}

export interface ProjectUpdateForm {
  name?: string
  description?: string
  language?: string
  status?: number
}

export interface ProjectMember {
  id: number
  userId: number
  username: string
  email: string
  nickname?: string
  role: string
  joinedAt: string
}

export interface ProjectInfo {
  id: string
  name: string
  description?: string
  language: string
  status: number  // 0-已完成，1-进行中，2-已暂停，3-已取消
  ownerId: number
  createdAt: string
  updatedAt: string
}

export interface AddMemberForm {
  userId: number
  role: string
}

/**
 * 项目相关API服务
 */
export const projectAPI = {
  /**
   * 创建项目
   * 对应后端接口: POST /api/v1/projects
   */
  createProject: (projectData: ProjectCreateForm) => {
    return http.post<ApiResponse<ProjectInfo>>('/projects', projectData)
  },

  /**
   * 获取用户的项目列表
   * 对应后端接口: GET /api/v1/projects
   */
  getProjects: () => {
    return http.get<ApiResponse<ProjectInfo[]>>('/projects')
  },

  /**
   * 获取项目详情
   * 对应后端接口: GET /api/v1/projects/{projectId}
   */
  getProjectById: (projectId: string) => {
    return http.get<ApiResponse<ProjectInfo>>(`/projects/${projectId}`)
  },

  /**
   * 更新项目信息
   * 对应后端接口: PUT /api/v1/projects/{projectId}
   */
  updateProject: (projectId: string, projectData: ProjectUpdateForm) => {
    return http.put<ApiResponse<ProjectInfo>>(`/projects/${projectId}`, projectData)
  },

  /**
   * 删除项目
   * 对应后端接口: DELETE /api/v1/projects/{projectId}
   */
  deleteProject: (projectId: string) => {
    return http.delete<ApiResponse<null>>(`/projects/${projectId}`)
  },

  /**
   * 获取项目成员列表
   * 对应后端接口: GET /api/v1/projects/{projectId}/members
   */
  getProjectMembers: (projectId: string) => {
    return http.get<ApiResponse<ProjectMember[]>>(`/projects/${projectId}/members`)
  },

  /**
   * 添加项目成员
   * 对应后端接口: POST /api/v1/projects/{projectId}/members
   */
  addProjectMember: (projectId: string, memberData: AddMemberForm) => {
    return http.post<ApiResponse<null>>(`/projects/${projectId}/members`, memberData)
  },

  /**
   * 更新成员角色
   * 对应后端接口: PUT /api/v1/projects/{projectId}/members/{userId}
   */
  updateMemberRole: (projectId: string, userId: number, role: string) => {
    return http.put<ApiResponse<null>>(`/projects/${projectId}/members/${userId}`, { 
      userId: userId,
      role: role 
    })
  },

  /**
   * 移除项目成员
   * 对应后端接口: DELETE /api/v1/projects/{projectId}/members/{userId}
   */
  removeMember: (projectId: string, userId: number) => {
    return http.delete<ApiResponse<null>>(`/projects/${projectId}/members/${userId}`)
  }
}