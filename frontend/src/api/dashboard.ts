// Dashboard统计数据类型
export interface DashboardStats {
  projectCount: number
  collaborationCount: number
  fileCount: number
}

// 项目信息（用于 Dashboard 最近项目列表）
export interface ProjectInfo {
  id: string
  name: string
  description?: string
  language: string
  updatedAt: string
}

// Dashboard完整数据
export interface DashboardData {
  stats: DashboardStats
  recentProjects: ProjectInfo[]
}
