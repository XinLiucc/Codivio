import http, { ApiResponse } from '@/utils/http'

export interface FileTreeNode {
  id: number
  projectId: string
  name: string
  path: string
  type: 'FILE' | 'DIRECTORY'
  parentPath: string | null
  fileId: string | null
  createdAt: string
  updatedAt: string
  children?: FileTreeNode[]
}

export interface CreateNodeForm {
  name: string
  path: string
  type: 'FILE' | 'DIRECTORY'
  parentPath?: string
}

export const fileTreeAPI = {
  /**
   * 获取项目完整文件树
   * GET /api/v1/projects/{projectId}/files
   */
  getFileTree: (projectId: string) => {
    return http.get<ApiResponse<FileTreeNode[]>>(`/projects/${projectId}/files`)
  },

  /**
   * 获取指定目录的子节点
   * GET /api/v1/projects/{projectId}/files/children?parentPath=xxx
   */
  getChildren: (projectId: string, parentPath: string) => {
    return http.get<ApiResponse<FileTreeNode[]>>(`/projects/${projectId}/files/children`, {
      params: { parentPath }
    })
  },

  /**
   * 创建文件或目录节点
   * POST /api/v1/projects/{projectId}/files/nodes
   */
  createNode: (projectId: string, form: CreateNodeForm) => {
    return http.post<ApiResponse<FileTreeNode>>(`/projects/${projectId}/files/nodes`, form)
  },

  /**
   * 重命名节点
   * PUT /api/v1/projects/{projectId}/files/nodes
   */
  renameNode: (projectId: string, nodeId: number, newName: string) => {
    return http.put<ApiResponse<FileTreeNode>>(`/projects/${projectId}/files/nodes`, {
      nodeId,
      newName
    })
  },

  /**
   * 删除节点
   * DELETE /api/v1/projects/{projectId}/files/nodes
   */
  deleteNode: (projectId: string, nodeId: number) => {
    return http.delete<ApiResponse<void>>(`/projects/${projectId}/files/nodes`, {
      data: { nodeId }
    })
  },

  /**
   * 移动节点
   * PUT /api/v1/projects/{projectId}/files/nodes/move
   */
  moveNode: (projectId: string, nodeId: number, newParentPath: string) => {
    return http.put<ApiResponse<FileTreeNode>>(`/projects/${projectId}/files/nodes/move`, {
      nodeId,
      newParentPath
    })
  }
}
