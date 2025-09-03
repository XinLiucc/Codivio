import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Project {
  id: number
  name: string
  description: string
  language: string
  ownerId: number
  createdAt: string
  updatedAt: string
}

export interface ProjectMember {
  id: number
  projectId: number
  userId: number
  role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'
  joinedAt: string
  username?: string
  nickname?: string
}

export const useProjectStore = defineStore('project', () => {
  // 状态
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const members = ref<ProjectMember[]>([])
  const isLoading = ref<boolean>(false)

  // 计算属性
  const projectCount = computed(() => projects.value.length)
  const currentProjectMembers = computed(() =>
    members.value.filter(member => member.projectId === currentProject.value?.id)
  )
  const isCurrentProjectOwner = computed(() => {
    if (!currentProject.value) return false
    // 这里需要结合 auth store 来判断
    return true // 临时返回
  })

  // 操作
  const setProjects = (projectList: Project[]) => {
    projects.value = projectList
  }

  const addProject = (project: Project) => {
    projects.value.push(project)
  }

  const updateProject = (projectId: number, updates: Partial<Project>) => {
    const index = projects.value.findIndex(p => p.id === projectId)
    if (index !== -1) {
      projects.value[index] = { ...projects.value[index], ...updates }
    }
  }

  const removeProject = (projectId: number) => {
    const index = projects.value.findIndex(p => p.id === projectId)
    if (index !== -1) {
      projects.value.splice(index, 1)
    }
  }

  const setCurrentProject = (project: Project | null) => {
    currentProject.value = project
  }

  const setMembers = (memberList: ProjectMember[]) => {
    members.value = memberList
  }

  const addMember = (member: ProjectMember) => {
    members.value.push(member)
  }

  const removeMember = (memberId: number) => {
    const index = members.value.findIndex(m => m.id === memberId)
    if (index !== -1) {
      members.value.splice(index, 1)
    }
  }

  const setLoading = (loading: boolean) => {
    isLoading.value = loading
  }

  const clearStore = () => {
    projects.value = []
    currentProject.value = null
    members.value = []
  }

  return {
    // 状态
    projects,
    currentProject,
    members,
    isLoading,
    // 计算属性
    projectCount,
    currentProjectMembers,
    isCurrentProjectOwner,
    // 操作
    setProjects,
    addProject,
    updateProject,
    removeProject,
    setCurrentProject,
    setMembers,
    addMember,
    removeMember,
    setLoading,
    clearStore,
  }
})
