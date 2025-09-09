<template>
  <div class="members-container">
    <div class="members-header">
      <div class="header-nav">
        <el-button @click="goBack" type="primary" plain>
          <el-icon><ArrowLeft /></el-icon>
          返回项目列表
        </el-button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="$router.push('/projects')" class="breadcrumb-link">项目管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ projectInfo?.name }}</el-breadcrumb-item>
          <el-breadcrumb-item>成员管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-content">
        <h2>项目成员管理</h2>
        <p class="members-subtitle">管理项目 "{{ projectInfo?.name }}" 的成员和权限</p>
      </div>
      
      <div class="header-actions">
        <el-button type="primary" @click="showAddMemberDialog = true" v-if="canManageMembers">
          <el-icon><Plus /></el-icon>
          添加成员
        </el-button>
      </div>
    </div>

    <!-- 成员列表 -->
    <el-card>
      <div v-loading="loading" class="members-content">
        <div v-if="members.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无项目成员">
            <el-button type="primary" @click="showAddMemberDialog = true" v-if="canManageMembers">
              添加第一个成员
            </el-button>
          </el-empty>
        </div>

        <div v-else class="members-table">
          <el-table :data="members" stripe>
            <el-table-column prop="userId" label="用户ID" width="100" />
            
            <el-table-column prop="role" label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="getRoleTagType(row.role)" size="small">
                  {{ getRoleLabel(row.role) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="joinedAt" label="加入时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.joinedAt) }}
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="200" v-if="canManageMembers">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleEditMember(row)"
                  v-if="row.role !== 'OWNER'"
                >
                  编辑角色
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleRemoveMember(row)"
                  v-if="row.role !== 'OWNER'"
                >
                  移除
                </el-button>
                <span v-if="row.role === 'OWNER'" class="owner-label">项目所有者</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="showAddMemberDialog" title="添加项目成员" width="500px">
      <el-form
        ref="addMemberFormRef"
        :model="addMemberForm"
        :rules="addMemberRules"
        label-width="80px"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="addMemberForm.userId" placeholder="请输入用户ID" type="number" />
          <div class="form-tip">暂时需要输入用户ID，后续会改为用户名搜索</div>
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="addMemberForm.role" placeholder="请选择角色">
            <el-option value="EDITOR" label="编辑者 - 可以编辑项目内容" />
            <el-option value="VIEWER" label="查看者 - 只能查看项目内容" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddMemberDialog = false">取消</el-button>
        <el-button type="primary" :loading="addingMember" @click="handleAddMember">
          添加成员
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑成员角色对话框 -->
    <el-dialog v-model="showEditMemberDialog" title="编辑成员角色" width="400px">
      <el-form
        ref="editMemberFormRef"
        :model="editMemberForm"
        :rules="editMemberRules"
        label-width="80px"
      >
        <el-form-item label="用户ID">
          <el-input :value="editMemberForm.userId" disabled />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="editMemberForm.role" placeholder="请选择角色">
            <el-option value="EDITOR" label="编辑者 - 可以编辑项目内容" />
            <el-option value="VIEWER" label="查看者 - 只能查看项目内容" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditMemberDialog = false">取消</el-button>
        <el-button type="primary" :loading="updatingMember" @click="handleUpdateMember">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft, Plus
} from '@element-plus/icons-vue'
import { projectAPI, type ProjectInfo, type ProjectMember } from '@/api/project'

const router = useRouter()
const route = useRoute()

// 项目ID (从路由参数获取)
const projectId = computed(() => parseInt(route.params.projectId as string))

// 加载状态
const loading = ref(false)
const addingMember = ref(false)
const updatingMember = ref(false)

// 项目信息和成员列表
const projectInfo = ref<ProjectInfo | null>(null)
const members = ref<ProjectMember[]>([])

// 对话框状态
const showAddMemberDialog = ref(false)
const showEditMemberDialog = ref(false)

// 表单引用
const addMemberFormRef = ref<FormInstance>()
const editMemberFormRef = ref<FormInstance>()

// 添加成员表单
const addMemberForm = reactive({
  userId: '',
  role: 'VIEWER'
})

// 编辑成员表单
const editMemberForm = reactive({
  userId: 0,
  role: 'VIEWER'
})

// 当前编辑的成员
const currentEditMember = ref<ProjectMember | null>(null)

// 表单验证规则
const addMemberRules: FormRules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const editMemberRules: FormRules = {
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 权限检查 - 简化版，实际应该检查当前用户在项目中的角色
const canManageMembers = computed(() => {
  // TODO: 实现真正的权限检查
  return true
})

// 获取角色标签类型
const getRoleTagType = (role: string) => {
  const typeMap: Record<string, string> = {
    'OWNER': 'danger',
    'EDITOR': 'primary', 
    'VIEWER': 'info'
  }
  return typeMap[role] || 'info'
}

// 获取角色标签文本
const getRoleLabel = (role: string) => {
  const labelMap: Record<string, string> = {
    'OWNER': '项目所有者',
    'EDITOR': '编辑者',
    'VIEWER': '查看者'
  }
  return labelMap[role] || role
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载项目信息
const loadProjectInfo = async () => {
  try {
    const response = await projectAPI.getProjectById(projectId.value)
    projectInfo.value = response.data.data
  } catch (error: any) {
    ElMessage.error('加载项目信息失败')
    console.error('Load project info error:', error)
  }
}

// 加载成员列表
const loadMembers = async () => {
  loading.value = true
  
  try {
    const response = await projectAPI.getProjectMembers(projectId.value)
    members.value = response.data.data
    
  } catch (error: any) {
    ElMessage.error('加载成员列表失败')
    console.error('Load members error:', error)
    
    // 如果API失败，使用模拟数据
    members.value = [
      {
        id: 1,
        userId: projectInfo.value?.ownerId || 1,
        username: 'admin',
        email: 'admin@example.com',
        role: 'OWNER',
        joinedAt: projectInfo.value?.createdAt || new Date().toISOString()
      }
    ]
  } finally {
    loading.value = false
  }
}

// 添加成员
const handleAddMember = async () => {
  if (!addMemberFormRef.value) return
  
  try {
    await addMemberFormRef.value.validate()
  } catch (error) {
    return
  }
  
  addingMember.value = true
  
  try {
    await projectAPI.addProjectMember(projectId.value, {
      userId: parseInt(addMemberForm.userId),
      role: addMemberForm.role
    })
    
    ElMessage.success('成员添加成功')
    showAddMemberDialog.value = false
    
    // 重置表单
    Object.assign(addMemberForm, {
      userId: '',
      role: 'VIEWER'
    })
    
    // 重新加载成员列表
    loadMembers()
    
  } catch (error: any) {
    ElMessage.error('添加成员失败')
    console.error('Add member error:', error)
  } finally {
    addingMember.value = false
  }
}

// 编辑成员
const handleEditMember = (member: ProjectMember) => {
  currentEditMember.value = member
  Object.assign(editMemberForm, {
    userId: member.userId,
    role: member.role
  })
  showEditMemberDialog.value = true
}

// 更新成员角色
const handleUpdateMember = async () => {
  if (!editMemberFormRef.value || !currentEditMember.value) return
  
  try {
    await editMemberFormRef.value.validate()
  } catch (error) {
    return
  }
  
  updatingMember.value = true
  
  try {
    await projectAPI.updateMemberRole(
      projectId.value,
      currentEditMember.value.userId,
      editMemberForm.role
    )
    
    ElMessage.success('角色更新成功')
    showEditMemberDialog.value = false
    
    // 重新加载成员列表
    loadMembers()
    
  } catch (error: any) {
    ElMessage.error('更新角色失败')
    console.error('Update member role error:', error)
  } finally {
    updatingMember.value = false
  }
}

// 移除成员
const handleRemoveMember = async (member: ProjectMember) => {
  try {
    await ElMessageBox.confirm(
      `确定要从项目中移除用户 ${member.userId} 吗？`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await projectAPI.removeMember(projectId.value, member.userId)
    
    ElMessage.success('成员移除成功')
    
    // 重新加载成员列表
    loadMembers()
    
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('移除成员失败')
      console.error('Remove member error:', error)
    }
  }
}

// 返回上一页
const goBack = () => {
  router.push('/projects')
}

// 组件挂载时加载数据
onMounted(() => {
  loadProjectInfo()
  loadMembers()
})
</script>

<style scoped>
.members-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.members-header {
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

.header-content {
  margin-bottom: 16px;
}

.header-content h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.members-subtitle {
  color: #606266;
  margin: 0;
}

.header-actions {
  display: flex;
  justify-content: flex-end;
}

.members-content {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.members-table {
  margin-top: 16px;
}

.owner-label {
  color: #909399;
  font-size: 12px;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>