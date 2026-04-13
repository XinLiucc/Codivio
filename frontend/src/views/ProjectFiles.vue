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
        <template v-if="activeFile && activeVersionId !== null">
          <el-tag size="small" type="warning" style="margin-left:4px;font-size:11px;">
            只读 · 历史版本 v{{ versions.find(v => v.id === activeVersionId)?.version }}
          </el-tag>
          <el-button size="small" type="primary" style="margin-left:6px;" @click="returnToLatest">
            回到最新版本
          </el-button>
        </template>
        <el-tag v-if="isViewer" size="small" type="info" style="margin-left:8px;">只读</el-tag>
        <div v-if="activeFile && !isViewer" class="collab-status">
          <span class="status-dot" :class="collabStatus"></span>
          <span class="status-text">{{ collabStatusText }}</span>
          <!-- 在线用户头像气泡 -->
          <div class="online-users" v-if="onlineUsers.length > 0">
            <el-tooltip
              v-for="u in onlineUsers"
              :key="u.name"
              :content="u.name"
              placement="bottom"
            >
              <div class="user-avatar" :style="{ background: u.color }">
                {{ u.name.charAt(0).toUpperCase() }}
              </div>
            </el-tooltip>
          </div>
        </div>
      </div>
      <div class="topbar-right">
        <template v-if="!isViewer">
          <el-button size="small" @click="startRootCreate('file')">
            <el-icon><DocumentAdd /></el-icon>
            新建文件
          </el-button>
          <el-button size="small" @click="startRootCreate('dir')">
            <el-icon><FolderAdd /></el-icon>
            新建目录
          </el-button>
          <el-button size="small" type="primary" :loading="saving"
            :disabled="!activeFile || activeVersionId !== null" @click="() => handleSaveFile()">
            <el-icon><Check /></el-icon>
            保存
          </el-button>
          <el-divider v-if="activeFile" direction="vertical" style="height:16px;margin:0 2px;" />
          <el-button v-if="activeFile" size="small" type="success" :loading="committing"
            :disabled="activeVersionId !== null" @click="showCommitDialog = true">
            <el-icon><Upload /></el-icon>
            提交版本
          </el-button>
        </template>
        <el-button v-if="activeFile" size="small" :type="showVersionPanel ? 'primary' : ''" @click="toggleVersionPanel">
          <el-icon><Timer /></el-icon>
          历史
        </el-button>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="ide-body">
      <!-- 左侧文件树 -->
      <div class="ide-sidebar" :style="{ width: sidebarWidth + 'px' }">
        <div class="sidebar-header">
          <span class="sidebar-title">文件</span>
          <div class="sidebar-header-actions">
            <el-icon v-if="!isViewer" class="header-action-icon" title="新建文件" @click="startRootCreate('file')">
              <DocumentAdd />
            </el-icon>
            <el-icon v-if="!isViewer" class="header-action-icon" title="新建文件夹" @click="startRootCreate('dir')">
              <FolderAdd />
            </el-icon>
            <el-icon class="header-action-icon" title="刷新" @click="loadFileTree">
              <Refresh />
            </el-icon>
          </div>
        </div>

        <div v-loading="treeLoading" class="sidebar-tree">
          <div v-if="fileTree.length === 0 && !treeLoading" class="tree-empty">
            <el-text type="info" size="small">暂无文件，点击上方图标创建</el-text>
          </div>
          <el-tree
            v-else
            ref="elTreeRef"
            :data="fileTree"
            :props="{ label: 'name', children: 'children', isLeaf: (d: FileTreeNode) => d.type === 'file' }"
            node-key="id"
            :current-node-key="activeFile?.id ?? undefined"
            highlight-current
            :expand-on-click-node="false"
            :indent="16"
            @node-click="handleNodeClick"
            @node-contextmenu="handleContextMenu"
          >
            <template #default="{ node, data }">
              <div class="tree-node-inner">
                <el-icon class="tree-node-icon" :class="{ 'icon-dir': data.type === 'directory' }">
                  <FolderOpened v-if="data.type === 'directory' && node.expanded" />
                  <Folder v-else-if="data.type === 'directory'" />
                  <Document v-else />
                </el-icon>
                <span class="tree-node-label">{{ data.name }}</span>
                <span v-if="!isViewer" class="tree-node-actions">
                  <el-icon v-if="data.type === 'directory'" title="新建文件" @click.stop="createInDir(data, 'file')"><DocumentAdd /></el-icon>
                  <el-icon v-if="data.type === 'directory'" title="新建文件夹" @click.stop="createInDir(data, 'dir')"><FolderAdd /></el-icon>
                  <el-icon title="重命名" @click.stop="renameNode(data)"><Edit /></el-icon>
                  <el-icon title="删除" @click.stop="deleteNode(data)"><Delete /></el-icon>
                </span>
              </div>
            </template>
          </el-tree>
        </div>

        <!-- 右键菜单 -->
        <teleport to="body">
          <div v-if="ctxVisible" class="ctx-menu" :style="{ left: ctxX + 'px', top: ctxY + 'px' }" @click.stop>
            <template v-if="ctxNode?.type === 'directory'">
              <div class="ctx-item" @click="createInDir(ctxNode!, 'file'); ctxVisible = false"><el-icon><DocumentAdd /></el-icon>新建文件</div>
              <div class="ctx-item" @click="createInDir(ctxNode!, 'dir'); ctxVisible = false"><el-icon><FolderAdd /></el-icon>新建文件夹</div>
              <div class="ctx-divider" />
            </template>
            <div class="ctx-item" @click="renameNode(ctxNode!); ctxVisible = false"><el-icon><Edit /></el-icon>重命名</div>
            <div class="ctx-item danger" @click="deleteNode(ctxNode!); ctxVisible = false"><el-icon><Delete /></el-icon>删除</div>
          </div>
        </teleport>

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
              ref="codeEditorRef"
              v-model="editorContent"
              :filename="activeFile.name"
              :height="'100%'"
              :show-toolbar="false"
              :readonly="activeVersionId !== null || isViewer"
              :project-id="activeVersionId === null && !isViewer ? projectId : ''"
              :file-id="activeVersionId === null ? (activeFile.fileId ?? '') : ''"
              @save="handleSaveFile"
              @collab-status="(s, t) => { collabStatus = s; collabStatusText = t }"
            />
          </div>
        </div>
      </div>

      <!-- 版本历史面板 -->
      <div v-if="showVersionPanel && activeFile" class="version-panel">
        <div class="version-panel-header">
          <span class="version-panel-title">{{ activeFile.name }} — 版本历史</span>
          <el-button type="text" size="small" @click="loadVersions">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
        <div class="version-panel-body" v-loading="versionsLoading">
          <div v-if="versions.length === 0 && !versionsLoading && !hasUncommittedChanges" class="version-empty">
            <el-text type="info" size="small">暂无版本记录，提交后可在此查看历史</el-text>
          </div>
          <div class="version-timeline">
            <!-- 工作区条目：有未提交改动时显示在最顶部 -->
            <div v-if="hasUncommittedChanges && activeVersionId === null" class="version-item">
              <div class="version-dot-wrap">
                <div class="version-dot working"></div>
                <div v-if="versions.length > 0" class="version-line"></div>
              </div>
              <div class="version-info">
                <div class="version-msg">未提交的改动</div>
                <div class="version-meta">
                  <el-tag size="small" type="warning" style="padding:0 4px;height:16px;line-height:16px;font-size:10px;">工作区</el-tag>
                  <span class="version-author" style="font-size:10px;color:#888;">已保存到数据库，未建立快照</span>
                </div>
                <el-button
                  v-if="!isViewer"
                  size="small"
                  type="success"
                  plain
                  style="margin-top:5px;font-size:11px;padding:2px 8px;height:auto;"
                  @click="showCommitDialog = true"
                >提交快照</el-button>
              </div>
            </div>
            <div v-for="(v, index) in versions" :key="v.id" class="version-item">
              <div class="version-dot-wrap">
                <div class="version-dot" :class="{ latest: index === 0 }"></div>
                <div v-if="index < versions.length - 1" class="version-line"></div>
              </div>
              <div class="version-info">
                <div class="version-msg">{{ v.message }}</div>
                <div class="version-meta">
                  <span class="version-num">v{{ v.version }}</span>
                  <el-tag
                    v-if="v.id === activeVersionId"
                    size="small"
                    type="success"
                    style="padding:0 4px;height:16px;line-height:16px;font-size:10px;"
                  >当前</el-tag>
                  <el-tag
                    v-else-if="activeVersionId === null && index === 0 && !hasUncommittedChanges"
                    size="small"
                    type="info"
                    style="padding:0 4px;height:16px;line-height:16px;font-size:10px;"
                  >最新</el-tag>
                  <span class="version-author">{{ v.createdByName }}</span>
                  <span class="version-time">{{ formatVersionTime(v.createdAt) }}</span>
                </div>
                <div
                  v-if="v.id !== activeVersionId && !(activeVersionId === null && index === 0 && !hasUncommittedChanges)"
                  class="version-actions"
                >
                  <el-button size="small" plain @click="handleViewVersion(v)">预览</el-button>
                  <el-button v-if="!isViewer" size="small" type="warning" plain @click="handleRestoreVersion(v)">恢复</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 提交版本对话框 -->
    <el-dialog v-model="showCommitDialog" title="提交版本" width="420px">
      <el-form label-width="80px">
        <el-form-item label="提交说明">
          <el-input
            v-model="commitMessage"
            type="textarea"
            :rows="3"
            placeholder="描述本次修改（可留空）"
            autofocus
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCommitDialog = false">取消</el-button>
        <el-button type="primary" :loading="committing" @click="handleCommitVersion">提交</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, DocumentAdd, FolderAdd, Edit, Delete, Check, Refresh,
  Document, Folder, FolderOpened, Close, Upload, Timer
} from '@element-plus/icons-vue'
import CodeEditor from '@/components/CodeEditor.vue'
import type { ElTree } from 'element-plus'
import { projectAPI, type ProjectInfo } from '@/api/project'
import { fileTreeAPI, type FileTreeNode } from '@/api/fileTree'
import { fileAPI, type FileVersionInfo } from '@/api/file'

const router = useRouter()
const route = useRoute()
const projectId = route.params.projectId as string

// 项目信息
const projectInfo = ref<ProjectInfo | null>(null)

// 文件树
const fileTree = ref<FileTreeNode[]>([])
const treeLoading = ref(false)

// 当前用户角色
const myRole = ref<string>('OWNER')
const isViewer = computed(() => myRole.value === 'VIEWER')

// 编辑器
const activeFile = ref<FileTreeNode | null>(null)
const editorContent = ref('')
const editorLoading = ref(false)
const saving = ref(false)
const codeEditorRef = ref<InstanceType<typeof CodeEditor> | null>(null)
const collabStatus = ref('connecting')
const collabStatusText = ref('连接中...')
const onlineUsers = computed(() => codeEditorRef.value?.onlineUsers ?? [])


// 侧边栏宽度
const sidebarWidth = ref(280)

// 版本历史面板
const showVersionPanel = ref(false)
const versions = ref<FileVersionInfo[]>([])
const versionsLoading = ref(false)
const hasUncommittedChanges = ref(false)
const showCommitDialog = ref(false)
const commitMessage = ref('')
const committing = ref(false)
const activeVersionId = ref<number | null>(null)  // 当前显示的是哪个版本

// el-tree ref
const elTreeRef = ref<InstanceType<typeof ElTree> | null>(null)

// 右键菜单
const ctxVisible = ref(false)
const ctxX = ref(0)
const ctxY = ref(0)
const ctxNode = ref<FileTreeNode | null>(null)

const handleContextMenu = (e: MouseEvent, data: FileTreeNode) => {
  ctxX.value = e.clientX
  ctxY.value = e.clientY
  ctxNode.value = data
  ctxVisible.value = true
}

const closeCtx = () => { ctxVisible.value = false }

// el-tree 节点点击
const handleNodeClick = (data: FileTreeNode) => {
  if (data.type === 'file') handleOpenFile(data)
}

// 顶部栏/header 触发根目录新建
const startRootCreate = (type: 'file' | 'dir') => createInDir(null, type)

// 通用新建（parentDir 为 null 时表示根目录）
const createInDir = async (parentDir: FileTreeNode | null, type: 'file' | 'dir') => {
  const label = type === 'file' ? '文件名' : '文件夹名'
  try {
    const { value: name } = await ElMessageBox.prompt(`请输入${label}`, `新建${type === 'file' ? '文件' : '文件夹'}`, {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPattern: /^[^\\/\s][^\\/]*$/,
      inputErrorMessage: '名称不能为空或含有斜杠'
    })
    if (!name) return
    await fileTreeAPI.createNode(projectId, {
      name,
      parentPath: parentDir?.path ?? '',
      type: type === 'dir' ? 'DIRECTORY' : 'FILE'
    })
    loadFileTree()
  } catch (e: any) {
    if (e === 'cancel') return
    const msg = e?.response?.data?.message || ''
    ElMessage.error(msg.includes('已存在') ? `"${e?.response?.data?.message?.split(': ')[1] ?? ''}" 已存在` : '创建失败')
  }
}

// 重命名
const renameNode = async (data: FileTreeNode) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('', '重命名', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputValue: data.name,
      inputPattern: /^[^\\/\s][^\\/]*$/,
      inputErrorMessage: '名称不能为空或含有斜杠'
    })
    if (!newName || newName === data.name) return
    await fileTreeAPI.renameNode(projectId, data.path, newName)
    if (activeFile.value?.id === data.id) {
      activeFile.value = { ...activeFile.value, name: newName }
    }
    loadFileTree()
  } catch (e: any) {
    if (e === 'cancel') return
    ElMessage.error('重命名失败')
  }
}

// 删除
const deleteNode = async (data: FileTreeNode) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${data.name}" 吗？`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await fileTreeAPI.deleteNode(projectId, data.path)
    ElMessage.success('删除成功')
    if (activeFile.value?.id === data.id) closeFile()
    loadFileTree()
  } catch (e: any) {
    if (e === 'cancel') return
    ElMessage.error('删除失败')
  }
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
  collabStatus.value = 'connecting'
  collabStatusText.value = '连接中...'
  versions.value = []
  showVersionPanel.value = false
  activeVersionId.value = null
  hasUncommittedChanges.value = false

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

// 递归在树中找节点
const findNodeById = (nodes: FileTreeNode[], id: number): FileTreeNode | null => {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children) {
      const found = findNodeById(n.children, id)
      if (found) return found
    }
  }
  return null
}

// 保存文件
// Yjs 模式下 editorContent 不实时同步，必须从 editor 实例取最新值
const handleSaveFile = async (contentOverride?: string | Event) => {
  if (contentOverride instanceof Event) contentOverride = undefined
  if (!activeFile.value) return

  // fileId 为空说明 RabbitMQ 异步初始化还没完成，自动刷新一次
  if (!activeFile.value.fileId) {
    try {
      const res = await fileTreeAPI.getFileTree(projectId)
      const freshNode = findNodeById(res.data.data?.tree ?? [], activeFile.value.id)
      if (freshNode?.fileId) {
        activeFile.value = { ...activeFile.value, fileId: freshNode.fileId }
        fileTree.value = res.data.data?.tree ?? fileTree.value
      } else {
        ElMessage.warning('文件初始化中，请稍等几秒再保存')
        return
      }
    } catch {
      ElMessage.warning('文件初始化中，请稍等几秒再保存')
      return
    }
  }
  const content = contentOverride ?? codeEditorRef.value?.getValue() ?? editorContent.value
  saving.value = true
  try {
    await fileAPI.updateFileContent(activeFile.value.fileId, content)
    hasUncommittedChanges.value = true
    ElMessage.success('保存成功')
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '未知错误'
    ElMessage.error(`保存失败：${msg}`)
  } finally {
    saving.value = false
  }
}

// 版本历史面板
const toggleVersionPanel = () => {
  showVersionPanel.value = !showVersionPanel.value
  if (showVersionPanel.value && activeFile.value) loadVersions()
}

const loadVersions = async () => {
  if (!activeFile.value?.fileId) return
  versionsLoading.value = true
  try {
    const res = await fileAPI.getVersions(activeFile.value.fileId)
    versions.value = res.data.data ?? []

    // 刷新后 hasUncommittedChanges 被重置，需要与最新版本内容比对
    if (!hasUncommittedChanges.value) {
      const currentContent = codeEditorRef.value?.getValue() ?? editorContent.value
      if (versions.value.length === 0) {
        // 从未提交过，有内容即视为未提交
        hasUncommittedChanges.value = currentContent.length > 0
      } else {
        // 与最新版本内容比对
        const latestVersion = versions.value[0]
        const vRes = await fileAPI.getVersion(activeFile.value.fileId, latestVersion.id)
        const latestContent = vRes.data.data?.content ?? ''
        hasUncommittedChanges.value = currentContent !== latestContent
      }
    }
  } catch {
    ElMessage.error('加载版本历史失败')
  } finally {
    versionsLoading.value = false
  }
}

const handleCommitVersion = async () => {
  if (!activeFile.value?.fileId) return
  // 先保存
  await handleSaveFile()
  committing.value = true
  try {
    await fileAPI.createVersion(activeFile.value.fileId, commitMessage.value.trim())
    hasUncommittedChanges.value = false
    ElMessage.success('版本提交成功')
    commitMessage.value = ''
    showCommitDialog.value = false
    if (showVersionPanel.value) loadVersions()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    committing.value = false
  }
}

// 预览历史版本（只读，不修改 DB）
const handleViewVersion = async (v: FileVersionInfo) => {
  if (!activeFile.value?.fileId) return
  try {
    const res = await fileAPI.getVersion(activeFile.value.fileId, v.id)
    const content = res.data.data?.content ?? ''
    editorContent.value = content
    activeVersionId.value = v.id
    // 触发 CodeEditor 重新挂载为只读模式（断开 Yjs）
    editorLoading.value = true
    await nextTick()
    editorLoading.value = false
    ElMessage.success(`正在预览 v${v.version}「${v.message}」（只读）`)
  } catch {
    ElMessage.error('加载版本内容失败')
  }
}

// 恢复到指定版本（写入 DB）
const handleRestoreVersion = async (v: FileVersionInfo) => {
  if (!activeFile.value?.fileId) return
  try {
    await ElMessageBox.confirm(
      `将当前文件内容恢复为 v${v.version}「${v.message}」？\n此操作会覆盖当前文件，可提交新版本作为备份。`,
      '恢复版本',
      { confirmButtonText: '确认恢复', cancelButtonText: '取消', type: 'warning' }
    )
    await fileAPI.restoreVersion(activeFile.value.fileId, v.id)
    ElMessage.success(`已恢复到 v${v.version}，可继续编辑`)
    await returnToLatest()
    if (showVersionPanel.value) loadVersions()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('恢复失败')
  }
}

// 回到最新版本（重新加载当前文件内容，恢复协作编辑）
const returnToLatest = async () => {
  if (!activeFile.value?.fileId) return
  try {
    const res = await fileAPI.downloadFile(activeFile.value.fileId)
    editorContent.value = res.data || ''
    activeVersionId.value = null
    // 触发 CodeEditor 重新挂载（切换 readonly + 重建 Yjs）
    editorLoading.value = true
    await nextTick()
    editorLoading.value = false
  } catch {
    ElMessage.error('加载最新版本失败')
  }
}

const formatVersionTime = (timeStr: string) => {
  return new Date(timeStr).toLocaleString('zh-CN', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}

// 新建文件（从目录行内输入触发）



const goBack = () => router.push('/projects')

// 侧边栏拖拽调整宽度
const startResize = (e: MouseEvent) => {
  const startX = e.clientX
  const startWidth = sidebarWidth.value
  const onMouseMove = (e: MouseEvent) => {
    const delta = e.clientX - startX
    sidebarWidth.value = Math.max(200, Math.min(560, startWidth + delta))
  }
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

onMounted(async () => {
  loadProjectInfo()
  loadFileTree()
  try {
    const res = await projectAPI.getMyRole(projectId)
    myRole.value = res.data.data.role
  } catch (e) {
    myRole.value = 'VIEWER'
  }
  document.addEventListener('click', closeCtx)
})

onUnmounted(() => {
  document.removeEventListener('click', closeCtx)
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

.collab-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #9d9d9d;
  margin-left: 8px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot.connected { background: #52c41a; }
.status-dot.connecting { background: #faad14; animation: pulse 1.2s infinite; }
.status-dot.disconnected { background: #ff4d4f; }

.online-users {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 6px;
}

.user-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  cursor: default;
  border: 1.5px solid rgba(255,255,255,0.3);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
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
  padding: 4px 8px 4px 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #bbbbbb;
  border-bottom: 1px solid #454545;
  flex-shrink: 0;
}

.sidebar-header-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.header-action-icon {
  font-size: 26px;
  color: #bbbbbb;
  padding: 4px;
  border-radius: 3px;
  cursor: pointer;
}

.header-action-icon:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
}

/* el-tree 暗色主题覆盖 */
:deep(.el-tree) {
  background: transparent;
  color: #cccccc;
  font-size: 14px;
}

:deep(.el-tree-node__content) {
  height: 34px;
  border-radius: 3px;
  padding-right: 4px;
}

:deep(.el-tree-node__content:hover) {
  background: #2a2d2e;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: #094771;
  color: #ffffff;
}

:deep(.el-tree-node__expand-icon) {
  color: #888;
  font-size: 14px;
}

:deep(.el-tree-node__expand-icon.is-leaf) {
  color: transparent;
}

/* 树节点内容布局 */
.tree-node-inner {
  display: flex;
  align-items: center;
  gap: 5px;
  width: 100%;
  min-width: 0;
  overflow: hidden;
}

.tree-node-icon {
  font-size: 22px;
  flex-shrink: 0;
  color: #c5c5c5;
}

.tree-node-icon.icon-dir {
  color: #dcb862;
}

.tree-node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

/* 悬停操作图标 */
.tree-node-actions {
  display: flex;
  align-items: center;
  gap: 1px;
  flex-shrink: 0;
  opacity: 0;
}

.tree-node-inner:hover .tree-node-actions {
  opacity: 1;
}

.tree-node-actions .el-icon {
  font-size: 22px;
  padding: 4px;
  border-radius: 3px;
  cursor: pointer;
  color: #c5c5c5;
}

.tree-node-actions .el-icon:hover {
  color: #fff;
  background: rgba(255,255,255,0.12);
}

.tree-node-actions .el-icon:last-child:hover {
  color: #f48771;
  background: rgba(244,135,113,0.15);
}

/* 右键菜单 */
.ctx-menu {
  position: fixed;
  z-index: 9999;
  background: #252526;
  border: 1px solid #454545;
  border-radius: 5px;
  padding: 4px 0;
  min-width: 155px;
  box-shadow: 0 6px 16px rgba(0,0,0,0.5);
}

.ctx-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 16px;
  font-size: 13px;
  color: #cccccc;
  cursor: pointer;
}

.ctx-item:hover { background: #094771; }
.ctx-item.danger { color: #f48771; }
.ctx-item.danger:hover { background: #5a1d1d; }

.ctx-divider {
  height: 1px;
  background: #454545;
  margin: 4px 0;
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
/* 版本历史面板 */
.version-panel {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #252526;
  border-left: 1px solid #3c3c3c;
  overflow: hidden;
}

.version-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #2d2d2d;
  border-bottom: 1px solid #3c3c3c;
  font-size: 12px;
  color: #cccccc;
  font-weight: 600;
  flex-shrink: 0;
}

.version-panel-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.version-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.version-empty {
  padding: 20px 12px;
  text-align: center;
  font-size: 12px;
  color: #666;
}

.version-timeline {
  padding: 4px 0;
}

.version-item {
  display: flex;
  gap: 8px;
  padding: 6px 12px;
}

.version-dot-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 12px;
  padding-top: 3px;
}

.version-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #555;
  border: 2px solid #666;
  flex-shrink: 0;
}

.version-dot.working {
  background: #e6a23c;
  border-color: #e6a23c;
}

.version-dot.latest {
  background: #4ec9b0;
  border-color: #4ec9b0;
}

.version-line {
  width: 2px;
  flex: 1;
  background: #3c3c3c;
  margin-top: 3px;
  min-height: 20px;
}

.version-info {
  flex: 1;
  min-width: 0;
  padding-bottom: 8px;
}

.version-msg {
  font-size: 12px;
  color: #cccccc;
  word-break: break-all;
  line-height: 1.4;
}

.version-meta {
  display: flex;
  gap: 6px;
  margin-top: 3px;
  flex-wrap: wrap;
}

.version-num {
  font-size: 11px;
  color: #4ec9b0;
  font-family: monospace;
}

.version-author {
  font-size: 11px;
  color: #888;
}

.version-time {
  font-size: 11px;
  color: #666;
}

.version-actions {
  display: flex;
  gap: 4px;
  margin-top: 5px;
}

.version-actions .el-button {
  font-size: 11px !important;
  padding: 2px 8px !important;
  height: auto !important;
}
</style>
