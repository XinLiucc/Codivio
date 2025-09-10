import http, { ApiResponse } from '@/utils/http'

// Dashboard统计数据类型
export interface DashboardStats {
  projectCount: number      // 我的项目数
  collaborationCount: number // 协作项目数
  fileCount: number         // 文件总数
  todayActivity: number     // 今日活动数
}

// 项目成员信息
export interface ProjectMember {
  id: number
  name: string
  avatar?: string
}

// 项目信息
export interface ProjectInfo {
  id: number
  name: string
  description?: string
  language: string
  members: ProjectMember[]
  memberCount: number
  updatedAt: string
}

// 用户活动记录
export interface UserActivity {
  id: number
  icon: string        // 图标名称
  text: string       // 活动描述
  time: string       // 相对时间
  timestamp: string  // 具体时间戳
}

// Dashboard完整数据
export interface DashboardData {
  stats: DashboardStats
  recentProjects: ProjectInfo[]
  recentActivities: UserActivity[]
}

/**
 * Dashboard相关API服务
 * 注意：目前后端尚未实现Dashboard相关接口，前端使用模拟数据
 */
export const dashboardAPI = {
  // 暂时没有真实的Dashboard API接口，所有功能使用模拟数据
}

// 生成模拟数据的工具函数（开发阶段使用）
export const mockDashboardData = (): DashboardData => {
  return {
    stats: {
      projectCount: 3,
      collaborationCount: 5,
      fileCount: 24,
      todayActivity: 8
    },
    recentProjects: [
      {
        id: 1,
        name: 'Vue 3 项目模板',
        description: '基于Vue 3 + TypeScript + Vite的现代化前端项目模板',
        language: 'Vue',
        members: [
          { id: 1, name: 'Alice', avatar: '' },
          { id: 2, name: 'Bob', avatar: '' }
        ],
        memberCount: 2,
        updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString() // 2小时前
      },
      {
        id: 2,
        name: 'API 服务后端',
        description: 'Spring Boot微服务架构的后端API项目',
        language: 'Java',
        members: [
          { id: 1, name: 'Charlie', avatar: '' }
        ],
        memberCount: 1,
        updatedAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString() // 1天前
      },
      {
        id: 3,
        name: 'React Native App',
        description: '跨平台移动应用开发项目',
        language: 'React',
        members: [
          { id: 1, name: 'David', avatar: '' },
          { id: 2, name: 'Emma', avatar: '' },
          { id: 3, name: 'Frank', avatar: '' }
        ],
        memberCount: 3,
        updatedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString() // 3天前
      }
    ],
    recentActivities: [
      {
        id: 1,
        icon: 'Document',
        text: '更新了项目文档 README.md',
        time: '2小时前',
        timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
      },
      {
        id: 2,
        icon: 'Plus',
        text: '创建了新项目 "移动端应用"',
        time: '昨天',
        timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString()
      },
      {
        id: 3,
        icon: 'UserFilled',
        text: '邀请了新成员加入项目',
        time: '3天前',
        timestamp: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString()
      },
      {
        id: 4,
        icon: 'Edit',
        text: '修改了文件 main.ts',
        time: '5天前',
        timestamp: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString()
      }
    ]
  }
}