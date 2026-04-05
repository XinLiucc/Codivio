import http, { ApiResponse } from '@/utils/http'

// 文件相关的数据类型
export interface FileUploadForm {
  projectId: string
  filePath: string
  content: string
  mimeType?: string
}

export interface FileInfo {
  id: string
  projectId: string
  filePath: string
  originalName: string
  mimeType: string
  content: string
  size: number
  version: number
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface FileStats {
  projectId: string
  fileCount: number
  totalSize: number
  totalSizeFormatted: string
}

/**
 * 文件相关API服务
 */
export const fileAPI = {
  /**
   * 上传文件（JSON方式）
   * 对应后端接口: POST /api/v1/files/upload
   */
  uploadFile: (fileData: FileUploadForm) => {
    return http.post<ApiResponse<FileInfo>>('/files/upload', fileData)
  },

  /**
   * 上传文件（MultipartFile方式）
   * 对应后端接口: POST /api/v1/files/upload-multipart
   */
  uploadMultipartFile: (file: File, projectId: string, filePath: string) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('projectId', projectId)
    formData.append('filePath', filePath)
    
    return http.post<ApiResponse<FileInfo>>('/files/upload-multipart', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 根据文件ID获取文件信息
   * 对应后端接口: GET /api/v1/files/{fileId}
   */
  getFileById: (fileId: string) => {
    return http.get<ApiResponse<FileInfo>>(`/files/${fileId}`)
  },

  /**
   * 根据项目ID获取文件列表
   * 对应后端接口: GET /api/v1/files/project/{projectId}
   */
  getFilesByProject: (projectId: string) => {
    return http.get<ApiResponse<FileInfo[]>>(`/files/project/${projectId}`)
  },

  /**
   * 更新文件内容
   * 对应后端接口: PUT /api/v1/files/{fileId}/content
   */
  updateFileContent: (fileId: string, content: string) => {
    return http.put<ApiResponse<FileInfo>>(`/files/${fileId}/content`, content, {
      headers: {
        'Content-Type': 'text/plain'
      }
    })
  },

  /**
   * 删除文件
   * 对应后端接口: DELETE /api/v1/files/{fileId}
   */
  deleteFile: (fileId: string) => {
    return http.delete<ApiResponse<boolean>>(`/files/${fileId}`)
  },

  /**
   * 下载文件内容
   * 对应后端接口: GET /api/v1/files/{fileId}/download
   */
  downloadFile: (fileId: string) => {
    return http.get(`/files/${fileId}/download`, {
      responseType: 'text'
    })
  },

  /**
   * 获取项目文件统计
   * 对应后端接口: GET /api/v1/files/project/{projectId}/stats
   */
  getProjectFileStats: (projectId: string) => {
    return http.get<ApiResponse<FileStats>>(`/files/project/${projectId}/stats`)
  }
}