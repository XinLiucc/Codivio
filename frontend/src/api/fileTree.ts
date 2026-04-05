import http, { ApiResponse } from '@/utils/http'

export interface FileTreeNode {
  id: number
  projectId: string
  name: string
  path: string
  type: 'file' | 'directory'
  parentPath: string | null
  fileId: string | null
  children?: FileTreeNode[]
  hasChildren?: boolean
  lastModified?: string
  createdAt?: string
}

export interface FileTreeResponse {
  tree: FileTreeNode[]
  totalFiles: number
  totalDirectories: number
  totalNodes: number
  lastModified: string | null
}

export const fileTreeAPI = {
  /**
   * 获取项目完整文件树
   * GET /api/v1/projects/{projectId}/files
   * 返回 { tree: [...], totalFiles, ... }
   */
  getFileTree: (projectId: string) => {
    return http.get<ApiResponse<FileTreeResponse>>(`/projects/${projectId}/files`)
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
   * 后端期望: { filePath, fileName, parentPath, type }
   */
  createNode: (projectId: string, params: {
    name: string
    parentPath: string
    type: 'FILE' | 'DIRECTORY'
  }) => {
    const parentPath = params.parentPath || '/'
    const filePath = parentPath.endsWith('/')
      ? `${parentPath}${params.name}`
      : `${parentPath}/${params.name}`
    return http.post<ApiResponse<FileTreeNode>>(`/projects/${projectId}/files/nodes`, {
      filePath,
      fileName: params.name,
      parentPath: params.parentPath || null,
      type: params.type
    })
  },

  /**
   * 重命名节点
   * PUT /api/v1/projects/{projectId}/files/nodes
   * 后端期望: { filePath, newFileName }
   */
  renameNode: (projectId: string, filePath: string, newFileName: string) => {
    return http.put<ApiResponse<FileTreeNode>>(`/projects/${projectId}/files/nodes`, {
      filePath,
      newFileName
    })
  },

  /**
   * 删除节点
   * DELETE /api/v1/projects/{projectId}/files/nodes?filePath=xxx
   * 后端用 @RequestParam 接收 filePath
   */
  deleteNode: (projectId: string, filePath: string) => {
    return http.delete<ApiResponse<void>>(`/projects/${projectId}/files/nodes`, {
      params: { filePath }
    })
  },

  /**
   * 移动节点
   * PUT /api/v1/projects/{projectId}/files/nodes/move
   * 后端期望: { sourcePath, targetParentPath }
   */
  moveNode: (projectId: string, sourcePath: string, targetParentPath: string) => {
    return http.put<ApiResponse<FileTreeNode>>(`/projects/${projectId}/files/nodes/move`, {
      sourcePath,
      targetParentPath
    })
  }
}
