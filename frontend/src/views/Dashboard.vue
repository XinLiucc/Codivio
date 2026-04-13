<template>
  <div class="dashboard">
    <!-- 顶部欢迎区域 -->
    <div class="dashboard-header">
      <div class="welcome-section">
        <h1>
          <el-icon><Sunny /></el-icon>
          {{ getGreeting() }}
        </h1>
        <p class="welcome-text">欢迎回到 Codivio 代码协作平台</p>
      </div>
      
    </div>

    <!-- 统计数据卡片 -->
    <el-row :gutter="24" class="stats-row">
      <el-col :span="8">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-icon project">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-number">{{ projectCount }}</div>
              <div class="stats-label">我的项目</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-icon collaboration">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-number">{{ collaborationCount }}</div>
              <div class="stats-label">协作项目</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-icon file">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-number">{{ fileCount }}</div>
              <div class="stats-label">文件总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作和项目列表 -->
    <el-row :gutter="24" class="content-row">
      <!-- 项目列表 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近项目</span>
              <el-button type="text" @click="viewAllProjects">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div v-if="loading" v-loading="loading" class="loading-area"></div>
          
          <div v-else-if="projects.length === 0" class="empty-state">
            <el-empty description="还没有项目">
              <el-button type="primary" @click="createProject">
                创建第一个项目
              </el-button>
            </el-empty>
          </div>
          
          <div v-else class="project-grid">
            <div
              v-for="project in projects"
              :key="project.id"
              class="project-card"
              @click="openProject(project.id)"
            >
              <div class="project-header">
                <div class="project-name">{{ project.name }}</div>
                <div class="project-language">
                  <el-tag :type="getLanguageType(project.language)" size="small">
                    {{ project.language }}
                  </el-tag>
                </div>
              </div>
              
              <div class="project-description">
                {{ project.description || '暂无描述' }}
              </div>
              
              <div class="project-footer">
                <div class="project-time">
                  {{ formatTime(project.updatedAt) }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Sunny, Folder, UserFilled, Document, Plus, ArrowRight
} from '@element-plus/icons-vue'
import { type DashboardData } from '@/api/dashboard'
import { projectAPI } from '@/api/project'

const router = useRouter()
// 加载状态
const loading = ref(false)

// Dashboard数据
const dashboardData = ref<DashboardData | null>(null)

// 统计数据（通过计算属性从dashboardData获取）
const projectCount = computed(() => dashboardData.value?.stats.projectCount || 0)
const collaborationCount = computed(() => dashboardData.value?.stats.collaborationCount || 0)
const fileCount = computed(() => dashboardData.value?.stats.fileCount || 0)

// 项目列表（通过计算属性从dashboardData获取）
const projects = computed(() => dashboardData.value?.recentProjects || [])

// 获取问候语
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
}

// 获取语言标签类型
const getLanguageType = (language: string) => {
  const types: Record<string, string> = {
    'Vue': 'success',
    'React': 'primary',
    'Angular': 'danger',
    'Java': 'warning',
    'Python': 'success',
    'JavaScript': 'warning',
    'TypeScript': 'primary'
  }
  return types[language] || 'info'
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const now = new Date()
  const time = new Date(timeStr)
  const diff = now.getTime() - time.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return time.toLocaleDateString('zh-CN')
}

// 快捷操作方法
const createProject = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入项目名称', '创建项目', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPattern: /^.{1,50}$/,
      inputErrorMessage: '项目名称长度为1-50个字符'
    })
    
    if (value) {
      const projectData = {
        name: value,
        description: '',
        language: 'JavaScript',
        status: 1
      }
      
      await projectAPI.createProject(projectData)
      ElMessage.success('项目创建成功')
      
      // 重新加载数据
      loadDashboardData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '创建项目失败')
    }
  }
}

const openProject = (projectId: string) => {
  router.push(`/projects/${projectId}/files`)
}

const viewAllProjects = () => {
  router.push('/projects')
}

// 退出登录
// 加载数据
const loadDashboardData = async () => {
  loading.value = true

  try {
    const [projectsResponse, statsResponse] = await Promise.all([
      projectAPI.getProjects(),
      projectAPI.getDashboardStats()
    ])
    const userProjects = projectsResponse.data.data
    const stats = statsResponse.data.data

    dashboardData.value = {
      stats: {
        projectCount: stats.projectCount,
        collaborationCount: stats.collaborationCount,
        fileCount: stats.fileCount
      },
      recentProjects: userProjects.slice(0, 4).map(project => ({
        id: project.id,
        name: project.name,
        description: project.description,
        language: project.language,
        updatedAt: project.updatedAt
      }))
    }

  } catch (error: any) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 48px);
}

/* 头部区域 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.welcome-section h1 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.welcome-text {
  margin: 0;
  color: #606266;
}

.user-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stats-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stats-card:hover {
  transform: translateY(-2px);
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stats-icon.project { background: #409eff; }
.stats-icon.collaboration { background: #67c23a; }
.stats-icon.file { background: #e6a23c; }

.stats-content {
  flex: 1;
}

.stats-number {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 内容区域 */
.content-row {
  align-items: stretch;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-button {
  justify-content: flex-start;
  height: 40px;
}


/* 项目网格 */
.loading-area {
  height: 200px;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.project-card {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
}

.project-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.project-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
}

.project-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-members {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-group .el-avatar {
  border: 2px solid white;
  margin-right: -8px;
}

.avatar-group .el-avatar:last-of-type {
  margin-right: 0;
}

.more-members {
  font-size: 12px;
  color: #909399;
}

.project-time {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}
</style>
