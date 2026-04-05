<template>
  <div class="files-container">
    <div class="files-header">
      <div class="header-nav">
        <el-button @click="goBack" type="primary" plain>
          <el-icon><ArrowLeft /></el-icon>
          返回项目列表
        </el-button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="$router.push('/projects')" class="breadcrumb-link">项目管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ projectInfo?.name }}</el-breadcrumb-item>
          <el-breadcrumb-item>文件管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-content">
        <h2>项目文件管理</h2>
        <p class="files-subtitle">管理项目 "{{ projectInfo?.name }}" 的文件</p>
      </div>
      
      <div class="header-actions">
        <el-button type="success" @click="showTextUploadDialog = true">
          <el-icon><DocumentAdd /></el-icon>
          新建文本文件
        </el-button>
        <el-button type="primary" @click="showFileUploadDialog = true">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
      </div>
    </div>

    <!-- 文件统计 -->
    <el-row :gutter="24" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon files"><FolderOpened /></el-icon>
            <div class="stat-content">
              <div class="stat-number">{{ fileStats?.fileCount || 0 }}</div>
              <div class="stat-label">文件总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon size"><DataLine /></el-icon>
            <div class="stat-content">
              <div class="stat-number">{{ fileStats?.totalSizeFormatted || '0 B' }}</div>
              <div class="stat-label">总大小</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon updated"><Clock /></el-icon>
            <div class="stat-content">
              <div class="stat-number">{{ lastUpdateTime }}</div>
              <div class="stat-label">最后更新</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文件列表 -->
    <el-card>
      <div v-loading="loading" class="files-content">
        <div v-if="files.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无项目文件">
            <el-button type="primary" @click="showFileUploadDialog = true">
              上传第一个文件
            </el-button>
          </el-empty>
        </div>

        <div v-else class="files-table">
          <el-table :data="files" stripe>
            <el-table-column label="文件名" min-width="200">
              <template #default="{ row }">
                <div class="file-info">
                  <el-icon class="file-icon" :style="{ color: getFileIconColor(row.fileExtension) }">
                    <component :is="getFileIcon(row.fileExtension)" />
                  </el-icon>
                  <span class="file-name">{{ row.originalName }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="fileTypeDescription" label="类型" width="120" />
            
            <el-table-column prop="fileSizeFormatted" label="大小" width="100" />
            
            <el-table-column prop="updatedAt" label="修改时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.updatedAt) }}
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="handlePreviewFile(row)"
                  >
                    预览
                  </el-button>
                  <el-button 
                    type="success" 
                    size="small" 
                    @click="handleEditFile(row)"
                    v-if="isEditableFile(row.fileExtension)"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    type="info" 
                    size="small" 
                    @click="handleDownloadFile(row)"
                  >
                    下载
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="handleDeleteFile(row)"
                  >
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 文件上传对话框 -->
    <el-dialog v-model="showFileUploadDialog" title="上传文件" width="500px">
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        :auto-upload="false"
        :on-change="handleFileSelect"
        :file-list="uploadFileList"
        :limit="1"
        :on-exceed="handleExceed"
        drag
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持各种文件格式，单个文件大小不超过 10MB
          </div>
        </template>
      </el-upload>

      <el-form
        v-if="selectedFile"
        :model="uploadForm"
        :rules="uploadRules"
        ref="uploadFormRef"
        label-width="80px"
        style="margin-top: 20px;"
      >
        <el-form-item label="文件路径" prop="filePath">
          <el-input v-model="uploadForm.filePath" placeholder="例如: src/main.js">
            <template #prepend>/</template>
          </el-input>
          <div class="form-tip">指定文件在项目中的路径，不包含文件名</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showFileUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUploadFile" :disabled="!selectedFile">
          上传文件
        </el-button>
      </template>
    </el-dialog>

    <!-- 文本文件创建对话框 -->
    <el-dialog v-model="showTextUploadDialog" title="创建文本文件" width="90%" top="3vh">
      <el-form
        :model="textForm"
        :rules="textRules"
        ref="textFormRef"
        label-width="80px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="文件名" prop="fileName">
              <el-input v-model="textForm.fileName" placeholder="例如: main.js">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文件路径" prop="filePath">
              <el-input v-model="textForm.filePath" placeholder="例如: src/">
                <template #prepend>/</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="文件内容" prop="content">
          <CodeEditor
            v-model="textForm.content"
            :filename="textForm.fileName"
            :height="'60vh'"
            :show-toolbar="true"
            placeholder="请输入文件内容..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showTextUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="creatingText" @click="handleCreateTextFile">
          创建文件
        </el-button>
      </template>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog v-model="showPreviewDialog" :title="previewFile?.originalName" width="80%" top="5vh">
      <div v-loading="loadingPreview" class="preview-content">
        <div v-if="previewFile && isTextFile(previewFile.fileExtension)" class="text-preview">
          <pre>{{ previewContent }}</pre>
        </div>
        <div v-else class="binary-preview">
          <el-result
            icon="warning"
            title="无法预览此文件"
            sub-title="该文件类型不支持在线预览，请下载后查看"
          >
            <template #extra>
              <el-button type="primary" @click="handleDownloadFile(previewFile)">下载文件</el-button>
            </template>
          </el-result>
        </div>
      </div>
    </el-dialog>

    <!-- 文件编辑对话框 -->
    <el-dialog v-model="showEditDialog" :title="`编辑: ${editFile?.originalName}`" width="95%" top="2vh">
      <div v-loading="loadingEdit" class="edit-content">
        <CodeEditor
          v-if="!loadingEdit && isEditableFile(editFile?.fileExtension)"
          v-model="editContent"
          :filename="editFile?.originalName"
          :height="'70vh'"
          @save="handleSaveFile"
          :show-toolbar="true"
        />
        <el-input
          v-else-if="!loadingEdit"
          v-model="editContent"
          type="textarea"
          :rows="25"
          placeholder="文件内容..."
        />
      </div>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveFile">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadInstance, type UploadRawFile, type UploadFiles, genFileId } from 'element-plus'
import {
  ArrowLeft, Upload, DocumentAdd, FolderOpened, DataLine, Clock,
  Document, VideoPlay, Picture, Folder, Files
} from '@element-plus/icons-vue'
import CodeEditor from '@/components/CodeEditor.vue'
import { projectAPI, type ProjectInfo } from '@/api/project'
import { fileAPI, type FileInfo, type FileStats } from '@/api/file'

const router = useRouter()
const route = useRoute()

// 项目ID (从路由参数获取)
const projectId = computed(() => route.params.projectId as string)

// 加载状态
const loading = ref(false)
const uploading = ref(false)
const creatingText = ref(false)
const loadingPreview = ref(false)
const loadingEdit = ref(false)
const saving = ref(false)

// 项目信息、文件列表和统计
const projectInfo = ref<ProjectInfo | null>(null)
const files = ref<FileInfo[]>([])
const fileStats = ref<FileStats | null>(null)

// 对话框状态
const showFileUploadDialog = ref(false)
const showTextUploadDialog = ref(false)
const showPreviewDialog = ref(false)
const showEditDialog = ref(false)

// 表单引用
const uploadFormRef = ref<FormInstance>()
const textFormRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()

// 上传相关
const selectedFile = ref<File | null>(null)
const uploadFileList = ref<UploadFiles>([])
const uploadForm = reactive({
  filePath: ''
})

// 文本文件创建表单
const textForm = reactive({
  fileName: '',
  filePath: '',
  content: ''
})

// 文件预览和编辑
const previewFile = ref<FileInfo | null>(null)
const previewContent = ref('')
const editFile = ref<FileInfo | null>(null)
const editContent = ref('')

// 表单验证规则
const uploadRules: FormRules = {
  filePath: [
    { required: true, message: '请输入文件路径', trigger: 'blur' }
  ]
}

const textRules: FormRules = {
  fileName: [
    { required: true, message: '请输入文件名', trigger: 'blur' }
  ],
  filePath: [
    { required: true, message: '请输入文件路径', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文件内容', trigger: 'blur' }
  ]
}

// 计算最后更新时间
const lastUpdateTime = computed(() => {
  if (!files.value.length) return '暂无'
  const latestFile = files.value.reduce((latest, current) => 
    new Date(current.updatedAt) > new Date(latest.updatedAt) ? current : latest
  )
  return formatDateTime(latestFile.updatedAt)
})

// 获取文件图标
const getFileIcon = (extension: string) => {
  const iconMap: Record<string, any> = {
    'js': Document,
    'ts': Document,
    'vue': Document,
    'html': Document,
    'css': Document,
    'json': Document,
    'txt': Document,
    'md': Document,
    'c': Document,
    'cpp': Document,
    'h': Document,
    'py': Document,
    'java': Document,
    'jpg': Picture,
    'jpeg': Picture,
    'png': Picture,
    'gif': Picture,
    'mp4': VideoPlay,
    'avi': VideoPlay,
    'folder': Folder
  }
  // 移除扩展名前面的点号
  const cleanExtension = extension?.toLowerCase().replace(/^\./, '') || ''
  return iconMap[cleanExtension] || Files
}

// 获取文件图标颜色
const getFileIconColor = (extension: string) => {
  const colorMap: Record<string, string> = {
    'js': '#f7df1e',
    'ts': '#3178c6',
    'vue': '#4fc08d',
    'html': '#e34c26',
    'css': '#1572b6',
    'json': '#000000',
    'txt': '#666666',
    'md': '#083fa1',
    'c': '#A8B9CC',
    'cpp': '#00599C',
    'h': '#A8B9CC',
    'py': '#3776AB',
    'java': '#ED8B00',
    'jpg': '#ff6b6b',
    'jpeg': '#ff6b6b',
    'png': '#4ecdc4',
    'gif': '#45b7d1',
    'mp4': '#ff6b6b',
    'avi': '#ff6b6b'
  }
  // 移除扩展名前面的点号
  const cleanExtension = extension?.toLowerCase().replace(/^\./, '') || ''
  return colorMap[cleanExtension] || '#666666'
}

// 判断是否为可编辑文件
const isEditableFile = (extension: string) => {
  const editableTypes = ['js', 'ts', 'vue', 'html', 'css', 'json', 'txt', 'md', 'xml', 'yml', 'yaml', 'py', 'java', 'cpp', 'c', 'h', 'scss', 'less', 'sql']
  // 移除扩展名前面的点号
  const cleanExtension = extension?.toLowerCase().replace(/^\./, '') || ''
  return editableTypes.includes(cleanExtension)
}

// 判断是否为文本文件
const isTextFile = (extension: string) => {
  const textTypes = ['js', 'ts', 'vue', 'html', 'css', 'json', 'txt', 'md', 'xml', 'yml', 'yaml', 'py', 'java', 'cpp', 'c', 'h']
  // 移除扩展名前面的点号
  const cleanExtension = extension?.toLowerCase().replace(/^\./, '') || ''
  return textTypes.includes(cleanExtension)
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

// 加载文件列表
const loadFiles = async () => {
  loading.value = true
  
  try {
    const response = await fileAPI.getFilesByProject(projectId.value)
    files.value = response.data.data
    
  } catch (error: any) {
    ElMessage.error('加载文件列表失败')
    console.error('Load files error:', error)
    files.value = []
  } finally {
    loading.value = false
  }
}

// 加载文件统计
const loadFileStats = async () => {
  try {
    const response = await fileAPI.getProjectFileStats(projectId.value)
    fileStats.value = response.data.data
  } catch (error: any) {
    console.error('Load file stats error:', error)
    fileStats.value = {
      projectId: projectId.value,
      fileCount: files.value.length,
      totalSize: 0,
      totalSizeFormatted: '0 B'
    }
  }
}

// 文件选择处理
const handleFileSelect = (uploadFile: any, uploadFiles: UploadFiles) => {
  selectedFile.value = uploadFile.raw
  uploadFileList.value = uploadFiles
}

// 文件超出限制处理
const handleExceed = (files: File[]) => {
  uploadRef.value!.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  uploadRef.value!.handleStart(file)
  selectedFile.value = file
}

// 上传文件
const handleUploadFile = async () => {
  if (!uploadFormRef.value || !selectedFile.value) return
  
  try {
    await uploadFormRef.value.validate()
  } catch (error) {
    return
  }
  
  uploading.value = true
  
  try {
    await fileAPI.uploadMultipartFile(
      selectedFile.value,
      projectId.value,
      uploadForm.filePath
    )
    
    ElMessage.success('文件上传成功')
    showFileUploadDialog.value = false
    
    // 重置表单
    Object.assign(uploadForm, {
      filePath: ''
    })
    selectedFile.value = null
    uploadRef.value?.clearFiles()
    
    // 重新加载数据
    loadFiles()
    loadFileStats()
    
  } catch (error: any) {
    ElMessage.error('文件上传失败')
    console.error('Upload file error:', error)
  } finally {
    uploading.value = false
  }
}

// 创建文本文件
const handleCreateTextFile = async () => {
  if (!textFormRef.value) return
  
  try {
    await textFormRef.value.validate()
  } catch (error) {
    return
  }
  
  creatingText.value = true
  
  try {
    const fullPath = `${textForm.filePath}/${textForm.fileName}`.replace(/\/+/g, '/')
    
    await fileAPI.uploadFile({
      projectId: projectId.value,
      filePath: fullPath,
      content: textForm.content,
      mimeType: 'text/plain'
    })
    
    ElMessage.success('文件创建成功')
    showTextUploadDialog.value = false
    
    // 重置表单
    Object.assign(textForm, {
      fileName: '',
      filePath: '',
      content: ''
    })
    
    // 重新加载数据
    loadFiles()
    loadFileStats()
    
  } catch (error: any) {
    ElMessage.error('文件创建失败')
    console.error('Create text file error:', error)
  } finally {
    creatingText.value = false
  }
}

// 预览文件
const handlePreviewFile = async (file: FileInfo) => {
  previewFile.value = file
  showPreviewDialog.value = true
  
  if (isTextFile(file.fileExtension)) {
    loadingPreview.value = true
    try {
      const response = await fileAPI.downloadFile(file.id)
      previewContent.value = response.data
    } catch (error: any) {
      ElMessage.error('加载文件内容失败')
      console.error('Preview file error:', error)
    } finally {
      loadingPreview.value = false
    }
  }
}

// 编辑文件
const handleEditFile = async (file: FileInfo) => {
  editFile.value = file
  showEditDialog.value = true
  
  loadingEdit.value = true
  try {
    const response = await fileAPI.downloadFile(file.id)
    editContent.value = response.data
  } catch (error: any) {
    ElMessage.error('加载文件内容失败')
    console.error('Edit file error:', error)
  } finally {
    loadingEdit.value = false
  }
}

// 保存文件
const handleSaveFile = async () => {
  if (!editFile.value) return
  
  saving.value = true
  
  try {
    await fileAPI.updateFileContent(editFile.value.id, editContent.value)
    
    ElMessage.success('文件保存成功')
    showEditDialog.value = false
    
    // 重新加载文件列表
    loadFiles()
    
  } catch (error: any) {
    ElMessage.error('文件保存失败')
    console.error('Save file error:', error)
  } finally {
    saving.value = false
  }
}

// 下载文件
const handleDownloadFile = async (file: FileInfo) => {
  try {
    const response = await fileAPI.downloadFile(file.id)
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: file.mimeType || 'text/plain' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.originalName
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('文件下载成功')
    
  } catch (error: any) {
    ElMessage.error('文件下载失败')
    console.error('Download file error:', error)
  }
}

// 删除文件
const handleDeleteFile = async (file: FileInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.originalName}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await fileAPI.deleteFile(file.id)
    
    ElMessage.success('文件删除成功')
    
    // 重新加载数据
    loadFiles()
    loadFileStats()
    
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('文件删除失败')
      console.error('Delete file error:', error)
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
  loadFiles()
  loadFileStats()
})
</script>

<style scoped>
.files-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.files-header {
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

.files-subtitle {
  color: #606266;
  margin: 0;
}

.header-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 32px;
  padding: 16px;
  border-radius: 8px;
}

.stat-icon.files { color: #409eff; background: rgba(64, 158, 255, 0.1); }
.stat-icon.size { color: #67c23a; background: rgba(103, 194, 58, 0.1); }
.stat-icon.updated { color: #e6a23c; background: rgba(230, 162, 60, 0.1); }

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  color: #606266;
  font-size: 14px;
  margin-top: 4px;
}

.files-content {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.files-table {
  margin-top: 16px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  font-size: 16px;
}

.file-name {
  font-weight: 500;
}

.upload-demo {
  margin-bottom: 16px;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.preview-content {
  min-height: 300px;
}

.text-preview {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  max-height: 500px;
  overflow: auto;
}

.text-preview pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.binary-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.edit-content {
  min-height: 300px;
}

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-start;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
  min-width: 52px;
  font-size: 12px;
}

.action-buttons .el-button + .el-button {
  margin-left: 0;
}
</style>