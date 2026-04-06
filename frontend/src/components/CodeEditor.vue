<template>
  <div class="code-editor-container">
    <!-- 编辑器工具栏 -->
    <div v-if="showToolbar" class="editor-toolbar">
      <div class="toolbar-left">
        <el-button size="small" type="primary" @click="handleSave" :loading="saving">
          <el-icon><DocumentAdd /></el-icon>
          保存
        </el-button>
        <el-button size="small" @click="handleFormat">
          <el-icon><Tools /></el-icon>
          格式化
        </el-button>
        <el-button size="small" @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
      </div>

      <div class="toolbar-right">
        <!-- 协作连接状态 -->
        <div v-if="projectId && fileId" class="collab-status">
          <span class="status-dot" :class="wsStatus"></span>
          <span class="status-text">{{ wsStatusText }}</span>
        </div>

        <el-select v-model="currentLanguage" size="small" style="width: 120px" @change="setLanguage">
          <el-option
            v-for="lang in supportedLanguages"
            :key="lang.value"
            :label="lang.label"
            :value="lang.value"
          />
        </el-select>

        <el-select v-model="currentTheme" size="small" style="width: 120px" @change="setTheme">
          <el-option label="深色主题" value="vs-dark" />
          <el-option label="浅色主题" value="vs" />
          <el-option label="高对比度" value="hc-black" />
        </el-select>
      </div>
    </div>

    <!-- 编辑器容器 -->
    <div
      ref="editorContainer"
      class="monaco-editor-wrapper"
      :class="{ 'fullscreen': isFullscreen }"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as monaco from 'monaco-editor'
import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { MonacoBinding } from 'y-monaco'
import { ElMessage } from 'element-plus'
import { DocumentAdd, Tools, FullScreen } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

// Props
interface Props {
  modelValue: string
  language?: string
  theme?: string
  height?: string
  readonly?: boolean
  showToolbar?: boolean
  filename?: string
  projectId?: string
  fileId?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  language: 'javascript',
  theme: 'vs-dark',
  height: '400px',
  readonly: false,
  showToolbar: true,
  filename: '',
  projectId: '',
  fileId: ''
})

// Emits
interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'save', value: string): void
  (e: 'change', value: string): void
  (e: 'collab-status', status: string, text: string): void
}

const emit = defineEmits<Emits>()

// 编辑器相关
const editorContainer = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

// Yjs 相关
let yjsDoc: Y.Doc | null = null
let yjsProvider: WebsocketProvider | null = null
let yjsBinding: MonacoBinding | null = null
const wsStatus = ref<'connecting' | 'connected' | 'disconnected'>('connecting')
const wsStatusText = ref('连接中...')

// 工具栏状态
const saving = ref(false)
const isFullscreen = ref(false)
const currentLanguage = ref(props.language)
const currentTheme = ref(props.theme)

// 支持的编程语言
const supportedLanguages = [
  { label: 'JavaScript', value: 'javascript' },
  { label: 'TypeScript', value: 'typescript' },
  { label: 'Vue', value: 'html' },
  { label: 'HTML', value: 'html' },
  { label: 'CSS', value: 'css' },
  { label: 'JSON', value: 'json' },
  { label: 'Python', value: 'python' },
  { label: 'Java', value: 'java' },
  { label: 'C++', value: 'cpp' },
  { label: 'Markdown', value: 'markdown' },
  { label: 'SQL', value: 'sql' },
  { label: 'XML', value: 'xml' },
  { label: 'YAML', value: 'yaml' },
  { label: 'Plain Text', value: 'plaintext' }
]

const getLanguageFromFilename = (filename: string): string => {
  if (!filename) return props.language
  const ext = filename.split('.').pop()?.toLowerCase()
  const languageMap: Record<string, string> = {
    'js': 'javascript', 'ts': 'typescript', 'vue': 'html',
    'html': 'html', 'css': 'css', 'scss': 'scss', 'less': 'less',
    'json': 'json', 'py': 'python', 'java': 'java', 'cpp': 'cpp',
    'c': 'c', 'h': 'cpp', 'md': 'markdown', 'sql': 'sql',
    'xml': 'xml', 'yml': 'yaml', 'yaml': 'yaml', 'txt': 'plaintext'
  }
  return languageMap[ext || ''] || props.language
}

// 初始化编辑器
const initializeEditor = () => {
  if (!editorContainer.value) return

  const inferredLanguage = getLanguageFromFilename(props.filename)
  currentLanguage.value = inferredLanguage

  editor = monaco.editor.create(editorContainer.value, {
    value: '',
    language: inferredLanguage,
    theme: currentTheme.value,
    fontSize: 14,
    lineNumbers: 'on',
    roundedSelection: false,
    scrollBeyondLastLine: false,
    readOnly: props.readonly,
    automaticLayout: true,
    minimap: { enabled: true },
    scrollbar: { vertical: 'visible', horizontal: 'visible' },
    suggestOnTriggerCharacters: true,
    quickSuggestions: true,
    wordWrap: 'on',
    folding: true,
    foldingHighlight: true,
    showFoldingControls: 'always'
  })

  if (props.projectId && props.fileId) {
    // 协作模式：接入 Yjs
    setupYjs()
  } else {
    // 普通模式：直接设置内容
    editor.setValue(props.modelValue)

    editor.onDidChangeModelContent(() => {
      const value = editor?.getValue() || ''
      emit('update:modelValue', value)
      emit('change', value)
    })
  }

  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
    handleSave()
  })

  editor.addCommand(monaco.KeyMod.Shift | monaco.KeyMod.Alt | monaco.KeyCode.KeyF, () => {
    handleFormat()
  })
}

// 为每个用户生成固定颜色（基于用户名哈希，同一用户颜色稳定）
const getUserColor = (name: string): string => {
  const colors = ['#e57373','#f06292','#ba68c8','#7986cb','#4fc3f7','#4db6ac','#81c784','#ffb74d','#a1887f','#90a4ae']
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

// 设置 Yjs 协作
const setupYjs = () => {
  if (!editor) return

  const authStore = useAuthStore()
  const token = authStore.token
  const userName = authStore.user?.nickname || authStore.user?.username || '匿名用户'

  yjsDoc = new Y.Doc()
  const yText = yjsDoc.getText('monaco')

  // y-websocket 会把 serverUrl + '/' + roomname 拼成最终 URL
  // 结果：ws://localhost:8080/collaboration/ws/{projectId}/{fileId}?token=xxx
  yjsProvider = new WebsocketProvider(
    `ws://localhost:8080/collaboration/ws/${props.projectId}`,
    props.fileId!,
    yjsDoc,
    { params: { token } }
  )

  // 设置本地用户信息，用于多光标显示
  yjsProvider.awareness.setLocalStateField('user', {
    name: userName,
    color: getUserColor(userName),
  })

  // 动态注入远程用户光标 CSS（y-monaco 只生成 class，颜色需手动注入）
  const styleEl = document.createElement('style')
  styleEl.id = 'yjs-cursor-styles'
  document.head.appendChild(styleEl)

  const updateCursorStyles = () => {
    const rules: string[] = []
    yjsProvider!.awareness.getStates().forEach((state, clientID) => {
      if (clientID === yjsDoc!.clientID) return
      const color = state.user?.color || '#999'
      const name = state.user?.name || '匿名'
      rules.push(`
        .yRemoteSelection-${clientID} { background-color: ${color}40; }
        .yRemoteSelectionHead-${clientID} {
          position: relative;
          border-left: 2px solid ${color};
        }
        .yRemoteSelectionHead-${clientID}::after {
          content: '${name.replace(/'/g, "\\'")}';
          position: absolute;
          top: -18px;
          left: -2px;
          background: ${color};
          color: #fff;
          font-size: 11px;
          padding: 1px 4px;
          border-radius: 3px;
          white-space: nowrap;
          pointer-events: none;
          z-index: 100;
        }
      `)
    })
    styleEl.textContent = rules.join('\n')
  }

  yjsProvider.awareness.on('change', ({ added }: { added: number[] }) => {
    updateCursorStyles()
    // 有新用户加入时，直接把当前光标位置写入 awareness，让对方立即看到我的光标
    if (added.length > 0 && editor && yjsDoc) {
      const yText = yjsDoc.getText('monaco')
      const sel = editor.getSelection()
      const model = editor.getModel()
      if (sel && model) {
        const anchor = Y.createRelativePositionFromTypeIndex(yText, model.getOffsetAt(sel.getStartPosition()))
        const head = Y.createRelativePositionFromTypeIndex(yText, model.getOffsetAt(sel.getEndPosition()))
        yjsProvider!.awareness.setLocalStateField('selection', { anchor, head })
      }
    }
  })

  // 页面刷新/关闭前主动清除 awareness，避免残留光标
  const handleBeforeUnload = () => yjsProvider?.awareness.setLocalState(null)
  window.addEventListener('beforeunload', handleBeforeUnload)

  const updateStatus = (status: string, text: string) => {
    wsStatus.value = status as any
    wsStatusText.value = text
    emit('collab-status', status, text)
  }

  yjsProvider.on('status', ({ status }: { status: string }) => {
    if (status === 'connected') {
      updateStatus('connected', '协作中')
    } else if (status === 'disconnected') {
      updateStatus('disconnected', '已断开')
    } else {
      updateStatus('connecting', '连接中...')
    }
  })

  // synced 事件补救（status 事件可能在监听前已触发）
  yjsProvider.on('sync', (isSynced: boolean) => {
    if (isSynced) updateStatus('connected', '协作中')
  })

  // 首次同步完成后，如果文档为空则用已加载的文件内容初始化
  yjsProvider.once('synced', () => {
    if (yText.length === 0 && props.modelValue) {
      yjsDoc!.transact(() => {
        yText.insert(0, props.modelValue)
      })
    }
  })

  // 绑定 Yjs 到 Monaco（MonacoBinding 接管内容同步，不再手动 setValue/getValue）
  yjsBinding = new MonacoBinding(
    yText,
    editor.getModel()!,
    new Set([editor]),
    yjsProvider.awareness
  )
}

// 清理 Yjs
const cleanupYjs = () => {
  yjsProvider?.awareness.setLocalState(null)
  yjsBinding?.destroy()
  yjsProvider?.destroy()
  yjsDoc?.destroy()
  yjsBinding = null
  yjsProvider = null
  yjsDoc = null
  document.getElementById('yjs-cursor-styles')?.remove()
}

// 保存文件
const handleSave = () => {
  if (!editor) return
  emit('save', editor.getValue())
}

// 格式化代码
const handleFormat = async () => {
  if (!editor) return
  try {
    const action = editor.getAction('editor.action.formatDocument')
    if (action) {
      await action.run()
      ElMessage.success('格式化完成')
    } else {
      ElMessage.warning('当前语言不支持格式化功能')
    }
  } catch {
    ElMessage.warning('格式化失败，可能不支持当前语言')
  }
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  nextTick(() => { editor?.layout() })
}

const setLanguage = (language: string) => {
  if (!editor) return
  monaco.editor.setModelLanguage(editor.getModel()!, language)
}

const setTheme = (theme: string) => {
  monaco.editor.setTheme(theme)
}

// 普通模式下同步外部 modelValue 变化；Yjs 模式下由 MonacoBinding 管理，不干预
watch(() => props.modelValue, (newValue) => {
  if (props.projectId && props.fileId) return
  if (editor && editor.getValue() !== newValue) {
    editor.setValue(newValue)
  }
})

watch(() => props.height, () => {
  nextTick(() => { editor?.layout() })
})

onMounted(() => {
  nextTick(() => { initializeEditor() })
})

onUnmounted(() => {
  cleanupYjs()
  editor?.dispose()
})

defineExpose({
  getEditor: () => editor,
  getValue: () => editor?.getValue() || '',
  setValue: (value: string) => editor?.setValue(value),
  focus: () => editor?.focus(),
  layout: () => editor?.layout(),
  wsStatus,
  wsStatusText
})
</script>

<style scoped>
.code-editor-container {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f5f5;
  border-bottom: 1px solid #d9d9d9;
}

.toolbar-left {
  display: flex;
  gap: 8px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.collab-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #666;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot.connected {
  background: #52c41a;
}

.status-dot.connecting {
  background: #faad14;
  animation: pulse 1.2s infinite;
}

.status-dot.disconnected {
  background: #ff4d4f;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.monaco-editor-wrapper {
  height: v-bind(height);
  position: relative;
}

.monaco-editor-wrapper.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 9999;
  background: white;
}
</style>
