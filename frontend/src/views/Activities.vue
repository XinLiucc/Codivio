<template>
  <div class="activities-container">
    <div class="activities-header">
      <div class="header-nav">
        <el-button @click="goBack" type="primary" plain>
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="$router.push('/dashboard')" class="breadcrumb-link">仪表板</el-breadcrumb-item>
          <el-breadcrumb-item>活动历史</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-content">
        <h2>活动历史</h2>
        <p class="activities-subtitle">查看您在Codivio上的所有操作记录</p>
      </div>
    </div>

    <!-- 筛选器 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="活动类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable @change="handleFilter">
            <el-option value="" label="全部类型" />
            <el-option value="file" label="文件操作" />
            <el-option value="project" label="项目操作" />
            <el-option value="user" label="用户操作" />
            <el-option value="system" label="系统操作" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleFilter"
          />
        </el-form-item>

        <el-form-item>
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="primary" @click="handleFilter">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 活动列表 -->
    <el-card>
      <div v-loading="loading" class="activities-content">
        <div v-if="activities.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无活动记录" />
        </div>

        <div v-else class="activity-timeline">
          <div
            v-for="activity in activities"
            :key="activity.id"
            class="activity-item"
          >
            <div class="activity-time">
              <div class="time-dot" :class="getActivityTypeClass(activity.icon)"></div>
              <div class="time-text">
                <div class="activity-date">{{ formatDate(activity.timestamp) }}</div>
                <div class="activity-relative-time">{{ activity.time }}</div>
              </div>
            </div>

            <div class="activity-content">
              <div class="activity-icon" :class="getActivityTypeClass(activity.icon)">
                <el-icon>
                  <component :is="getActivityIcon(activity.icon)" />
                </el-icon>
              </div>

              <div class="activity-details">
                <div class="activity-text">{{ activity.text }}</div>
                <div class="activity-meta">
                  <el-tag :type="getActivityTagType(activity.icon)" size="small">
                    {{ getActivityTypeLabel(activity.icon) }}
                  </el-tag>
                  <span class="activity-timestamp">{{ formatFullDate(activity.timestamp) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document, Plus, UserFilled, Edit, Delete, FolderOpened,
  Setting, Lock, Upload, Download, ArrowLeft
} from '@element-plus/icons-vue'
import { type UserActivity } from '@/api/dashboard'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 活动列表
const activities = ref<UserActivity[]>([])

// 筛选表单
const filterForm = reactive({
  type: '',
  dateRange: null as [string, string] | null
})

// 活动图标映射
const activityIcons = {
  'Document': Document,
  'Plus': Plus,
  'UserFilled': UserFilled,
  'Edit': Edit,
  'Delete': Delete,
  'FolderOpened': FolderOpened,
  'Setting': Setting,
  'Lock': Lock,
  'Upload': Upload,
  'Download': Download
}

// 获取活动图标
const getActivityIcon = (iconName: string) => {
  return activityIcons[iconName as keyof typeof activityIcons] || Document
}

// 获取活动类型样式
const getActivityTypeClass = (iconName: string) => {
  const typeMap: Record<string, string> = {
    'Document': 'file-type',
    'Edit': 'file-type', 
    'Plus': 'project-type',
    'FolderOpened': 'project-type',
    'UserFilled': 'user-type',
    'Setting': 'system-type',
    'Lock': 'system-type',
    'Upload': 'file-type',
    'Download': 'file-type',
    'Delete': 'danger-type'
  }
  return typeMap[iconName] || 'default-type'
}

// 获取活动标签类型
const getActivityTagType = (iconName: string) => {
  const tagMap: Record<string, string> = {
    'Document': 'primary',
    'Edit': 'primary',
    'Plus': 'success',
    'FolderOpened': 'success',
    'UserFilled': 'info',
    'Setting': 'warning',
    'Lock': 'warning',
    'Upload': 'primary',
    'Download': 'primary',
    'Delete': 'danger'
  }
  return tagMap[iconName] || 'info'
}

// 获取活动类型标签
const getActivityTypeLabel = (iconName: string) => {
  const labelMap: Record<string, string> = {
    'Document': '文件',
    'Edit': '编辑',
    'Plus': '创建',
    'FolderOpened': '项目',
    'UserFilled': '用户',
    'Setting': '设置',
    'Lock': '安全',
    'Upload': '上传',
    'Download': '下载',
    'Delete': '删除'
  }
  return labelMap[iconName] || '其他'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

// 格式化完整日期
const formatFullDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载活动数据
const loadActivities = async () => {
  loading.value = true
  
  try {
    // 模拟异步加载
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 直接使用模拟数据（后端活动接口尚未实现）
    generateMockActivities()
    
  } catch (error: any) {
    ElMessage.error('加载数据失败')
    console.error('Activities data loading error:', error)
  } finally {
    loading.value = false
  }
}

// 生成模拟活动数据
const generateMockActivities = () => {
  const mockData: UserActivity[] = [
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
    },
    {
      id: 5,
      icon: 'Upload',
      text: '上传了头像',
      time: '1周前',
      timestamp: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 6,
      icon: 'Setting',
      text: '更新了账户设置',
      time: '1周前',
      timestamp: new Date(Date.now() - 8 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 7,
      icon: 'FolderOpened',
      text: '加入了协作项目 "后端API"',
      time: '2周前',
      timestamp: new Date(Date.now() - 14 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 8,
      icon: 'Lock',
      text: '修改了账户密码',
      time: '2周前',
      timestamp: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000).toISOString()
    }
  ]
  
  activities.value = mockData
  total.value = mockData.length
}

// 处理筛选
const handleFilter = () => {
  currentPage.value = 1
  loadActivities()
}

// 重置筛选
const resetFilter = () => {
  filterForm.type = ''
  filterForm.dateRange = null
  currentPage.value = 1
  loadActivities()
}

// 处理页面变更
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadActivities()
}

// 处理每页大小变更
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadActivities()
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 组件挂载时加载数据
onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.activities-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.activities-header {
  margin-bottom: 24px;
}

.header-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.breadcrumb-link {
  cursor: pointer;
  color: #409eff;
}

.breadcrumb-link:hover {
  text-decoration: underline;
}

.header-content h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.activities-subtitle {
  color: #606266;
  margin: 0;
}

.filter-card {
  margin-bottom: 24px;
}

.filter-form {
  margin-bottom: 0;
}

.activities-content {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.activity-timeline {
  position: relative;
}

.activity-item {
  display: flex;
  gap: 24px;
  padding-bottom: 24px;
  position: relative;
}

.activity-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 20px;
  top: 60px;
  bottom: -24px;
  width: 1px;
  background: #dcdfe6;
}

.activity-time {
  flex-shrink: 0;
  width: 120px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.time-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 16px;
  position: relative;
  z-index: 1;
}

.time-dot.file-type { background: #409eff; }
.time-dot.project-type { background: #67c23a; }
.time-dot.user-type { background: #909399; }
.time-dot.system-type { background: #e6a23c; }
.time-dot.danger-type { background: #f56c6c; }
.time-dot.default-type { background: #dcdfe6; }

.time-text {
  flex: 1;
}

.activity-date {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.activity-relative-time {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.activity-content {
  flex: 1;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.activity-icon.file-type { background: #409eff; }
.activity-icon.project-type { background: #67c23a; }
.activity-icon.user-type { background: #909399; }
.activity-icon.system-type { background: #e6a23c; }
.activity-icon.danger-type { background: #f56c6c; }
.activity-icon.default-type { background: #dcdfe6; }

.activity-details {
  flex: 1;
  padding-top: 8px;
}

.activity-text {
  color: #2c3e50;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity-timestamp {
  color: #909399;
  font-size: 12px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
</style>