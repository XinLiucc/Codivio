<template>
  <div class="ide-layout">
    <!-- 顶部栏 -->
    <div class="ide-topbar">
      <div class="topbar-left">
        <el-button @click="goBack" type="text" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="project-name">{{ projectInfo?.name || '加载中...' }}</span>
        <span class="topbar-divider">/</span>
        <span class="topbar-file">{{ activeFile?.name || '未打开文件' }}</span>
      </div>
      <div class="topbar-right">
        <el-button size="small" @click="showNewFileDialog = true">
          <el-icon><DocumentAdd /></el-icon>
          新建文件
        </el-button>
        <el-button size="small" @click="showNewDirDialog = true">
          <el-icon><FolderAdd /></el-icon>
          新建目录
        </el-button>
        <el-button size="small" type="primary" :loading="saving" :disabled="!activeFile" @click="handleSaveFile">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="ide-body">
      <!-- 左侧文件树 -->
      <div class="ide-sidebar" :style="{ width: sidebarWidth + 'px' }">
        <div class="sidebar-header">
          <span class="sidebar-title">文件</span>
          <el-button type="text" size="small" @click="loadFileTree">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>

        <div v-loading="treeLoading" class="sidebar-tree">
          <div v-if="fileTree.length === 0 && !treeLoading" class="tree-empty">
            <el-text type="info" size="small">暂无文件</el-text>
          </div>
          <FileTreeItem
            v-for="node in fileTree"
            :key="node.id"
            :node="node"
            :active-id="activeFile?.id ?? null"
            @open="handleOpenFile"
            @new-file="handleNewFileInDir"
            @new-dir="handleNewDirInDir"
            @rename="handleRenameNode"
            @delete="handleDeleteNode"
          />
        </div>

        <!-- 拖拽调整宽度 -->
        <div class="sidebar-resizer" @mousedown="startResize" />
      </div>

      <!-- 右侧编辑器区域 -->
      <div class="ide-editor">
        <div v-if="!activeFile" class="editor-placeholder">
          <el-icon class="placeholder-icon"><Document /></el-icon>
          <p>从左侧选择文件打开</p>
        </div>

        <div v-else class="editor-wrapper">
          <div class="editor-tabs">
            <div class="editor-tab active">
              <el-icon><Document /></el-icon>
              <span>{{ activeFile.name }}</span>
              <el-icon class="tab-close" @click="closeFile"><Close /></el-icon>
            </div>
          </div>
          <div class="editor-content" v-loading="editorLoading">
            <CodeEditor
              v-if="!editorLoading"
              v-model="editorContent"
              :filename="activeFile.name"
              :height="'100%'"
              :show-toolbar="false"
              @save="handleSaveFile"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 新建文件对话框 -->
    <el-dialog v-model="showNewFileDialog" title="新建文件" width="400px" @closed="resetNewForm">
      <el-form :model="newForm" :rules="newFileRules" ref="newFormRef" label-width="80px">
        <el-form-item label="文件名" prop="name">
          <el-input v-model="newForm.name" placeholder="例如: main.js" autofocus />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="newForm.parentPath" placeholder="例如: /src（留空表示根目录）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewFileDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateFile">创建</el-button>
      </template>
    </el-dialog>

    <!-- 新建目录对话框 -->
    <el-dialog v-model="showNewDirDialog" title="新建目录" width="400px" @closed="resetNewForm">
      <el-form :model="newForm" :rules="newDirRules" ref="newDirFormRef" label-width="80px">
        <el-form-item label="目录名" prop="name">
          <el-input v-model="newForm.name" placeholder="例如: components" autofocus />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="newForm.parentPath" placeholder="例如: /src（留空表示根目录）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewDirDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateDir">创建</el-button>
      </template>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog v-model="showRenameDialog" title="重命名" width="400px">
      <el-input v-model="renameValue" placeholder="新名称" autofocus @keyup.enter="confirmRename" />
      <template #footer>
        <el-button @click="showRenameDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRename">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft, DocumentAdd, FolderAdd, Check, Refresh, Document, Close
} from '@element-plus/icons-vue'
import CodeEditor from '@/components/CodeEditor.vue'
import FileTreeItem from '@/components/FileTreeItem.vue'
import { projectAPI, type ProjectInfo } from '@/api/project'
import { fileTreeAPI, type FileTreeNode } from '@/api/fileTree'
import { fileAPI } from '@/api/file'

const router = useRouter()
const route = useRoute()
const projectId = route.params.projectId as string

// 项目信息
const projectInfo = ref<ProjectInfo | null>(null)

// 文件树
const fileTree = ref<FileTreeNode[]>([])
const treeLoading = ref(false)

// 编辑器
const activeFile = ref<FileTreeNode | null>(null)
const editorContent = ref('')
const editorLoading = ref(false)
const saving = ref(false)

// 侧边栏宽度
const sidebarWidth = ref(240)

// 对话框状态
const showNewFileDialog = ref(false)
const showNewDirDialog = ref(false)
const showRenameDialog = ref(false)
const creating = ref(false)

// 新建表单
const newForm = reactive({ name: '', parentPath: '' })
const newFormRef = ref<FormInstance>()
const newDirFormRef = ref<FormInstance>()

// 重命名
const renameValue = ref('')
const renameTarget = ref<FileTreeNode | null>(null)

const newFileRules: FormRules = {
  name: [{ required: true, message: '请输入文件名', trigger: 'blur' }]
}
const newDirRules: FormRules = {
  name: [{ required: true, message: '请输入目录名', trigger: 'blur' }]
}

// 加载项目信息
const loadProjectInfo = async () => {
  try {
    const res = await projectAPI.getProjectById(projectId)
    projectInfo.value = res.data.data
  } catch {
    ElMessage.error('加载项目信息失败')
  }
}

// 加载文件树
const loadFileTree = async () => {
  treeLoading.value = true
  try {
    const res = await fileTreeAPI.getFileTree(projectId)
    fileTree.value = res.data.data?.tree ?? []
  } catch {
    ElMessage.error('加载文件树失败')
  } finally {
    treeLoading.value = false
  }
}

// 打开文件
const handleOpenFile = async (node: FileTreeNode) => {
  if (node.type === 'directory') return
  if (activeFile.value?.id === node.id) return

  activeFile.value = node
  editorLoading.value = true
  editorContent.value = ''

  try {
    if (node.fileId) {
      const res = await fileAPI.downloadFile(node.fileId)
      editorContent.value = res.data || ''
    }
  } catch {
    ElMessage.error('加载文件内容失败')
  } finally {
    editorLoading.value = false
  }
}

// 关闭文件
const closeFile = () => {
  activeFile.value = null
  editorContent.value = ''
}

// 保存文件
const handleSaveFile = async () => {
  if (!activeFile.value?.fileId) return
  saving.value = true
  try {
    await fileAPI.updateFileContent(activeFile.value.fileId, editorContent.value)
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 新建文件（从目录上下文菜单触发）
const handleNewFileInDir = (node: FileTreeNode) => {
  newForm.parentPath = node.path
  showNewFileDialog.value = true
}

// 新建目录（从目录上下文菜单触发）
const handleNewDirInDir = (node: FileTreeNode) => {
  newForm.parentPath = node.path
  showNewDirDialog.value = true
}

// 创建文件
const handleCreateFile = async () => {
  if (!newFormRef.value) return
  try { await newFormRef.value.validate() } catch { return }
  creating.value = true
  try {
    await fileTreeAPI.createNode(projectId, {
      name: newForm.name,
      parentPath: newForm.parentPath,
      type: 'FILE'
    })
    ElMessage.success('文件创建成功')
    showNewFileDialog.value = false
    loadFileTree()
  } catch {
    ElMessage.error('文件创建失败')
  } finally {
    creating.value = false
  }
}

// 创建目录
const handleCreateDir = async () => {
  if (!newDirFormRef.value) return
  try { await newDirFormRef.value.validate() } catch { return }
  creating.value = true
  try {
    await fileTreeAPI.createNode(projectId, {
      name: newForm.name,
      parentPath: newForm.parentPath,
      type: 'DIRECTORY'
    })
    ElMessage.success('目录创建成功')
    showNewDirDialog.value = false
    loadFileTree()
  } catch {
    ElMessage.error('目录创建失败')
  } finally {
    creating.value = false
  }
}

// 重命名
const handleRenameNode = (node: FileTreeNode) => {
  renameTarget.value = node
  renameValue.value = node.name
  showRenameDialog.value = true
}

const confirmRename = async () => {
  if (!renameTarget.value || !renameValue.value.trim()) return
  try {
    await fileTreeAPI.renameNode(projectId, renameTarget.value.path, renameValue.value.trim())
    ElMessage.success('重命名成功')
    showRenameDialog.value = false
    if (activeFile.value?.id === renameTarget.value.id) {
      activeFile.value = { ...activeFile.value, name: renameValue.value.trim() }
    }
    loadFileTree()
  } catch {
    ElMessage.error('重命名失败')
  }
}

// 删除节点
const handleDeleteNode = async (node: FileTreeNode) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${node.name}" 吗？`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await fileTreeAPI.deleteNode(projectId, node.path)
    ElMessage.success('删除成功')
    if (activeFile.value?.id === node.id) closeFile()
    loadFileTree()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const resetNewForm = () => {
  newForm.name = ''
  newForm.parentPath = ''
}

const goBack = () => router.push('/projects')

// 侧边栏拖拽调整宽度
const startResize = (e: MouseEvent) => {
  const startX = e.clientX
  const startWidth = sidebarWidth.value
  const onMouseMove = (e: MouseEvent) => {
    const delta = e.clientX - startX
    sidebarWidth.value = Math.max(160, Math.min(480, startWidth + delta))
  }
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

onMounted(() => {
  loadProjectInfo()
  loadFileTree()
})
</script>

<style scoped>
.ide-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #1e1e1e;
  color: #d4d4d4;
  overflow: hidden;
}

/* 顶部栏 */
.ide-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 40px;
  padding: 0 12px;
  background: #323233;
  border-bottom: 1px solid #454545;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-btn {
  color: #cccccc !important;
  padding: 4px !important;
}

.project-name {
  font-size: 13px;
  font-weight: 600;
  color: #cccccc;
}

.topbar-divider {
  color: #555;
  font-size: 13px;
}

.topbar-file {
  font-size: 13px;
  color: #9d9d9d;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 主体 */
.ide-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 侧边栏 */
.ide-sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  background: #252526;
  border-right: 1px solid #454545;
  flex-shrink: 0;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #bbbbbb;
  border-bottom: 1px solid #454545;
  flex-shrink: 0;
}

.sidebar-tree {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.tree-empty {
  padding: 16px;
  text-align: center;
}

.sidebar-resizer {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  cursor: col-resize;
  z-index: 10;
}

.sidebar-resizer:hover {
  background: #007acc;
}

/* 编辑器区域 */
.ide-editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #1e1e1e;
}

.editor-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #555;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.editor-placeholder p {
  font-size: 14px;
  margin: 0;
}

/* 标签页 */
.editor-tabs {
  display: flex;
  background: #2d2d2d;
  border-bottom: 1px solid #454545;
  flex-shrink: 0;
}

.editor-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  font-size: 13px;
  color: #9d9d9d;
  border-right: 1px solid #454545;
  background: #1e1e1e;
  border-top: 1px solid #007acc;
  color: #cccccc;
}

.tab-close {
  font-size: 12px;
  cursor: pointer;
  opacity: 0.6;
  margin-left: 4px;
}

.tab-close:hover {
  opacity: 1;
}

.editor-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.editor-content :deep(.code-editor-container) {
  flex: 1;
  height: 100%;
  border: none;
  border-radius: 0;
}
</style>
