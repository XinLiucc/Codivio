<template>
  <div class="projects-container">
    <div class="projects-header">
      <h2>我的项目</h2>
      <p class="projects-subtitle">管理和协作您的代码项目</p>
      
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建项目
        </el-button>
      </div>
    </div>

    <!-- 项目统计 -->
    <el-row :gutter="24" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon project"><Folder /></el-icon>
            <div class="stat-content">
              <div class="stat-number">{{ projects.length }}</div>
              <div class="stat-label">总项目数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon active"><View /></el-icon>
            <div class="stat-content">
              <div class="stat-number">{{ activeProjectsCount }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon completed"><Lock /></el-icon>
            <div class="stat-content">
              <div class="stat-number">{{ completedProjectsCount }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 项目列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <div class="header-controls">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索项目..."
              :prefix-icon="Search"
              clearable
              style="width: 300px"
              @input="handleSearch"
            />
            <el-select v-model="languageFilter" placeholder="编程语言" clearable style="width: 120px; margin-left: 12px">
              <el-option value="" label="全部语言" />
              <el-option
                v-for="lang in uniqueLanguages"
                :key="lang"
                :value="lang"
                :label="lang"
              />
            </el-select>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="projects-content">
        <div v-if="filteredProjects.length === 0 && !loading" class="empty-state">
          <el-empty :description="searchKeyword ? '没有找到匹配的项目' : '还没有项目'">
            <el-button v-if="!searchKeyword" type="primary" @click="showCreateDialog = true">
              创建第一个项目
            </el-button>
          </el-empty>
        </div>

        <div v-else class="projects-grid">
          <div
            v-for="project in filteredProjects"
            :key="project.id"
            class="project-card"
            @click="openProject(project.id)"
          >
            <div class="project-header">
              <div class="project-name">{{ project.name }}</div>
              <div class="project-actions" @click.stop>
                <el-dropdown @command="handleProjectAction">
                  <el-icon class="action-icon"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{ action: 'edit', project }">编辑</el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'files', project }">文件管理</el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'members', project }">成员管理</el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'delete', project }" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="project-meta">
              <el-tag :type="getLanguageType(project.language)" size="small">
                {{ project.language }}
              </el-tag>
              <el-tag :type="getStatusTagType(project.status)" size="small">{{ getStatusLabel(project.status) }}</el-tag>
            </div>
            
            <div class="project-description">
              {{ project.description || '暂无描述' }}
            </div>
            
            <div class="project-footer">
              <div class="project-stats">
                <span class="stat-item">
                  <el-icon><Calendar /></el-icon>
                  创建于 {{ formatTime(project.createdAt) }}
                </span>
              </div>
              <div class="project-time">
                {{ formatTime(project.updatedAt) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 创建项目对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建项目" width="500px">
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="80px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述（可选）"
          />
        </el-form-item>
        
        <el-form-item label="编程语言" prop="language">
          <el-select v-model="createForm.language" placeholder="请选择编程语言">
            <el-option value="JavaScript" label="JavaScript" />
            <el-option value="TypeScript" label="TypeScript" />
            <el-option value="Vue" label="Vue" />
            <el-option value="React" label="React" />
            <el-option value="Python" label="Python" />
            <el-option value="Java" label="Java" />
            <el-option value="Go" label="Go" />
            <el-option value="Rust" label="Rust" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="项目状态">
          <el-select v-model="createForm.status" placeholder="请选择项目状态">
            <el-option :value="1" label="进行中" />
            <el-option :value="0" label="已完成" />
            <el-option :value="2" label="已暂停" />
            <el-option :value="3" label="已取消" />
          </el-select>
          <div class="form-tip">
            项目状态可以后续修改
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateProject">
          创建项目
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑项目对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑项目" width="500px">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述（可选）"
          />
        </el-form-item>
        
        <el-form-item label="编程语言" prop="language">
          <el-select v-model="editForm.language" placeholder="请选择编程语言">
            <el-option value="JavaScript" label="JavaScript" />
            <el-option value="TypeScript" label="TypeScript" />
            <el-option value="Vue" label="Vue" />
            <el-option value="React" label="React" />
            <el-option value="Python" label="Python" />
            <el-option value="Java" label="Java" />
            <el-option value="Go" label="Go" />
            <el-option value="Rust" label="Rust" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="项目状态">
          <el-select v-model="editForm.status" placeholder="请选择项目状态">
            <el-option :value="1" label="进行中" />
            <el-option :value="0" label="已完成" />
            <el-option :value="2" label="已暂停" />
            <el-option :value="3" label="已取消" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="handleUpdateProject">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus, Folder, View, Lock, Search, UserFilled, MoreFilled, Calendar
} from '@element-plus/icons-vue'
import { projectAPI, type ProjectInfo } from '@/api/project'

const router = useRouter()

// 状态管理
const loading = ref(false)
const creating = ref(false)
const updating = ref(false)
const projects = ref<ProjectInfo[]>([])

// 搜索和筛选
const searchKeyword = ref('')
const languageFilter = ref('')

// 对话框状态
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const currentEditProject = ref<ProjectInfo | null>(null)

// 表单引用
const createFormRef = ref<FormInstance>()
const editFormRef = ref<FormInstance>()

// 创建项目表单
const createForm = reactive({
  name: '',
  description: '',
  language: 'JavaScript',
  status: 1
})

// 编辑项目表单
const editForm = reactive({
  name: '',
  description: '',
  language: 'JavaScript',
  status: 1
})

// 表单验证规则
const createRules: FormRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 1, max: 50, message: '项目名称长度为1-50个字符', trigger: 'blur' }
  ],
  language: [
    { required: true, message: '请选择编程语言', trigger: 'change' }
  ]
}

const editRules: FormRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 1, max: 50, message: '项目名称长度为1-50个字符', trigger: 'blur' }
  ],
  language: [
    { required: true, message: '请选择编程语言', trigger: 'change' }
  ]
}

// 计算属性
const activeProjectsCount = computed(() => 
  projects.value.filter(p => p.status === 1).length  // 进行中的项目
)

const completedProjectsCount = computed(() => 
  projects.value.filter(p => p.status === 0).length  // 已完成的项目
)

const uniqueLanguages = computed(() => 
  [...new Set(projects.value.map(p => p.language))]
)

const filteredProjects = computed(() => {
  let result = projects.value
  
  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(p => 
      p.name.toLowerCase().includes(keyword) ||
      (p.description && p.description.toLowerCase().includes(keyword))
    )
  }
  
  // 按语言筛选
  if (languageFilter.value) {
    result = result.filter(p => p.language === languageFilter.value)
  }
  
  return result
})

// 获取语言标签类型
const getLanguageType = (language: string) => {
  const types: Record<string, string> = {
    'Vue': 'success',
    'React': 'primary',
    'Angular': 'danger',
    'Java': 'warning',
    'Python': 'success',
    'JavaScript': 'warning',
    'TypeScript': 'primary',
    'Go': 'info',
    'Rust': 'danger'
  }
  return types[language] || 'info'
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const types: Record<number, string> = {
    0: 'success',  // 已完成
    1: 'primary',  // 进行中
    2: 'warning',  // 已暂停
    3: 'info'      // 已取消
  }
  return types[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: number) => {
  const labels: Record<number, string> = {
    0: '已完成',
    1: '进行中',
    2: '已暂停',
    3: '已取消'
  }
  return labels[status] || '未知'
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

// 加载项目列表
const loadProjects = async () => {
  loading.value = true
  
  try {
    const response = await projectAPI.getProjects()
    projects.value = response.data.data
    
  } catch (error: any) {
    ElMessage.error(error.message || '加载项目列表失败')
    console.error('Load projects error:', error)
  } finally {
    loading.value = false
  }
}

// 创建项目
const handleCreateProject = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
  } catch (error) {
    return
  }
  
  creating.value = true
  
  try {
    await projectAPI.createProject(createForm)
    
    ElMessage.success('项目创建成功')
    showCreateDialog.value = false
    
    // 重置表单
    Object.assign(createForm, {
      name: '',
      description: '',
      language: 'JavaScript',
      isPublic: false
    })
    
    // 重新加载项目列表
    loadProjects()
    
  } catch (error: any) {
    ElMessage.error(error.message || '创建项目失败')
    console.error('Create project error:', error)
  } finally {
    creating.value = false
  }
}

// 编辑项目
const handleUpdateProject = async () => {
  if (!editFormRef.value || !currentEditProject.value) return
  
  try {
    await editFormRef.value.validate()
  } catch (error) {
    return
  }
  
  updating.value = true
  
  try {
    await projectAPI.updateProject(currentEditProject.value.id, editForm)
    
    ElMessage.success('项目更新成功')
    showEditDialog.value = false
    
    // 重新加载项目列表
    loadProjects()
    
  } catch (error: any) {
    ElMessage.error(error.message || '更新项目失败')
    console.error('Update project error:', error)
  } finally {
    updating.value = false
  }
}

// 删除项目
const handleDeleteProject = async (project: ProjectInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目 "${project.name}" 吗？此操作不可恢复。`,
      '删除项目',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await projectAPI.deleteProject(project.id)
    ElMessage.success('项目删除成功')
    
    // 重新加载项目列表
    loadProjects()
    
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除项目失败')
    }
  }
}

// 项目操作处理
const handleProjectAction = ({ action, project }: { action: string, project: ProjectInfo }) => {
  switch (action) {
    case 'edit':
      currentEditProject.value = project
      Object.assign(editForm, {
        name: project.name,
        description: project.description || '',
        language: project.language,
        status: project.status
      })
      showEditDialog.value = true
      break
    case 'files':
      router.push(`/projects/${project.id}/files`)
      break
    case 'members':
      router.push(`/projects/${project.id}/members`)
      break
    case 'delete':
      handleDeleteProject(project)
      break
  }
}

// 打开项目
const openProject = (projectId: string) => {
  router.push(`/projects/${projectId}/files`)
}

// 搜索处理
const handleSearch = () => {
  // 搜索是响应式的，不需要额外处理
}

// 组件挂载时加载数据
onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.projects-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--app-bg);
}

.projects-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.projects-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.projects-subtitle {
  color: #606266;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.project { background: #409eff; }
.stat-icon.public { background: #67c23a; }
.stat-icon.private { background: #909399; }

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  align-items: center;
}

/* 项目网格 */
.projects-content {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.project-card {
  padding: 20px;
  border: 1px solid var(--sidebar-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--sidebar-bg);
}

.project-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.project-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
}

.project-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.project-card:hover .project-actions {
  opacity: 1;
}

.action-icon {
  cursor: pointer;
  color: #909399;
  font-size: 18px;
}

.action-icon:hover {
  color: #409eff;
}

.project-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.project-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.project-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.project-time {
  font-size: 12px;
  color: #909399;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>