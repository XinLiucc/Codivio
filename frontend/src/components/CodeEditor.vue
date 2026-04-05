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
import { ElMessage } from 'element-plus'
import { DocumentAdd, Tools, FullScreen } from '@element-plus/icons-vue'

// Props
interface Props {
  modelValue: string
  language?: string
  theme?: string
  height?: string
  readonly?: boolean
  showToolbar?: boolean
  filename?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  language: 'javascript',
  theme: 'vs-dark',
  height: '400px',
  readonly: false,
  showToolbar: true,
  filename: ''
})

// Emits
interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'save', value: string): void
  (e: 'change', value: string): void
}

const emit = defineEmits<Emits>()

// 编辑器相关状态
const editorContainer = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

// 工具栏状态
const saving = ref(false)
const isFullscreen = ref(false)
const currentLanguage = ref(props.language)
const currentTheme = ref(props.theme)

// 支持的编程语言
const supportedLanguages = [
  { label: 'JavaScript', value: 'javascript' },
  { label: 'TypeScript', value: 'typescript' },
  { label: 'Vue', value: 'html' }, // Vue 使用 HTML 语法高亮
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

// 根据文件扩展名推断语言
const getLanguageFromFilename = (filename: string): string => {
  if (!filename) return props.language
  
  const ext = filename.split('.').pop()?.toLowerCase()
  const languageMap: Record<string, string> = {
    'js': 'javascript',
    'ts': 'typescript',
    'vue': 'html',
    'html': 'html',
    'css': 'css',
    'scss': 'scss',
    'less': 'less',
    'json': 'json',
    'py': 'python',
    'java': 'java',
    'cpp': 'cpp',
    'c': 'c',
    'h': 'cpp',
    'md': 'markdown',
    'sql': 'sql',
    'xml': 'xml',
    'yml': 'yaml',
    'yaml': 'yaml',
    'txt': 'plaintext'
  }
  
  return languageMap[ext || ''] || props.language
}

// 初始化编辑器
const initializeEditor = () => {
  if (!editorContainer.value) return

  // 根据文件名推断语言
  const inferredLanguage = getLanguageFromFilename(props.filename)
  currentLanguage.value = inferredLanguage

  editor = monaco.editor.create(editorContainer.value, {
    value: props.modelValue,
    language: inferredLanguage,
    theme: currentTheme.value,
    fontSize: 14,
    lineNumbers: 'on',
    roundedSelection: false,
    scrollBeyondLastLine: false,
    readOnly: props.readonly,
    automaticLayout: true,
    minimap: { enabled: true },
    scrollbar: {
      vertical: 'visible',
      horizontal: 'visible'
    },
    suggestOnTriggerCharacters: true,
    quickSuggestions: true,
    wordWrap: 'on',
    folding: true,
    foldingHighlight: true,
    showFoldingControls: 'always'
  })

  // 监听内容变化
  editor.onDidChangeModelContent(() => {
    const value = editor?.getValue() || ''
    emit('update:modelValue', value)
    emit('change', value)
  })

  // 添加保存快捷键
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
    handleSave()
  })

  // 添加格式化快捷键
  editor.addCommand(monaco.KeyMod.Shift | monaco.KeyMod.Alt | monaco.KeyCode.KeyF, () => {
    handleFormat()
  })
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
  } catch (error) {
    ElMessage.warning('格式化失败，可能不支持当前语言')
  }
}

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  nextTick(() => {
    editor?.layout()
  })
}

// 设置语言
const setLanguage = (language: string) => {
  if (!editor) return
  monaco.editor.setModelLanguage(editor.getModel()!, language)
}

// 设置主题
const setTheme = (theme: string) => {
  monaco.editor.setTheme(theme)
}

// 监听属性变化
watch(() => props.modelValue, (newValue) => {
  if (editor && editor.getValue() !== newValue) {
    editor.setValue(newValue)
  }
})

watch(() => props.height, () => {
  nextTick(() => {
    editor?.layout()
  })
})

// 生命周期
onMounted(() => {
  nextTick(() => {
    initializeEditor()
  })
})

onUnmounted(() => {
  editor?.dispose()
})

// 暴露编辑器实例给父组件
defineExpose({
  getEditor: () => editor,
  getValue: () => editor?.getValue() || '',
  setValue: (value: string) => editor?.setValue(value),
  focus: () => editor?.focus(),
  layout: () => editor?.layout()
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
  gap: 8px;
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

/* 深色主题下的工具栏样式 */
.code-editor-container:has(.monaco-editor-wrapper[data-theme="vs-dark"]) .editor-toolbar {
  background: #2d2d30;
  border-bottom-color: #3e3e42;
  color: #cccccc;
}
</style>