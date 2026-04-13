<template>
  <div class="tree-node">
    <!-- 节点行：重命名时换成输入框 -->
    <div
      v-if="!renaming"
      class="tree-row"
      :class="{ active: node.type === 'file' && node.id === activeId, directory: node.type === 'directory' }"
      :style="{ paddingLeft: depth * 16 + 8 + 'px' }"
      @click="handleClick"
      @contextmenu.prevent="props.readonly ? undefined : showMenu"
    >
      <el-icon class="arrow" :class="{ expanded: isOpen, invisible: node.type === 'file' }">
        <ArrowRight />
      </el-icon>
      <el-icon class="node-icon">
        <component :is="nodeIcon" />
      </el-icon>
      <span class="node-name">{{ node.name }}</span>

      <!-- 悬停操作图标 -->
      <div v-if="!props.readonly" class="row-actions" @click.stop>
        <template v-if="node.type === 'directory'">
          <el-icon class="action-icon" title="新建文件" @click="startInlineCreate('file')">
            <DocumentAdd />
          </el-icon>
          <el-icon class="action-icon" title="新建文件夹" @click="startInlineCreate('dir')">
            <FolderAdd />
          </el-icon>
        </template>
        <el-icon class="action-icon" title="重命名" @click="startRename">
          <Edit />
        </el-icon>
        <el-icon class="action-icon danger" title="删除" @click="onDelete">
          <Delete />
        </el-icon>
      </div>
    </div>

    <!-- 行内重命名输入行 -->
    <div
      v-else
      class="tree-row rename-row"
      :style="{ paddingLeft: depth * 16 + 8 + 'px' }"
    >
      <el-icon class="arrow invisible"><ArrowRight /></el-icon>
      <el-icon class="node-icon" :class="{ directory: node.type === 'directory' }">
        <component :is="nodeIcon" />
      </el-icon>
      <input
        ref="renameInputRef"
        v-model="renameName"
        class="inline-input"
        @keydown.enter.prevent="submitRename"
        @keydown.esc="cancelRename"
        @blur="cancelRename"
      />
    </div>

    <!-- 子节点 -->
    <div v-if="node.type === 'directory' && isOpen">
      <!-- 行内新建输入框 -->
      <div v-if="inlineCreating" class="tree-row inline-row" :style="{ paddingLeft: (depth + 1) * 16 + 8 + 'px' }">
        <el-icon class="arrow invisible"><ArrowRight /></el-icon>
        <el-icon class="node-icon" :class="{ directory: inlineCreating === 'dir' }">
          <Folder v-if="inlineCreating === 'dir'" />
          <Document v-else />
        </el-icon>
        <input
          ref="inlineInputRef"
          v-model="inlineName"
          class="inline-input"
          @keydown.enter.prevent="submitInlineCreate"
          @keydown.esc="cancelInlineCreate"
          @blur="cancelInlineCreate"
        />
      </div>

      <div v-if="node.children && node.children.length === 0 && !inlineCreating"
           class="empty-dir"
           :style="{ paddingLeft: (depth + 1) * 16 + 8 + 'px' }">
        <el-text size="small" type="info">空目录</el-text>
      </div>

      <FileTreeItem
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :active-id="activeId"
        :depth="depth + 1"
        :readonly="props.readonly"
        @open="$emit('open', $event)"
        @new-file="(n, name) => $emit('new-file', n, name)"
        @new-dir="(n, name) => $emit('new-dir', n, name)"
        @rename="(n, name) => $emit('rename', n, name)"
        @delete="$emit('delete', $event)"
      />
    </div>

    <!-- 右键菜单 -->
    <teleport to="body">
      <div
        v-if="menuVisible"
        class="context-menu"
        :style="{ left: menuX + 'px', top: menuY + 'px' }"
        @click.stop
      >
        <template v-if="node.type === 'directory'">
          <div class="menu-item" @click="onNewFile"><el-icon><DocumentAdd /></el-icon> 新建文件</div>
          <div class="menu-item" @click="onNewDir"><el-icon><FolderAdd /></el-icon> 新建目录</div>
          <div class="menu-divider" />
        </template>
        <div class="menu-item" @click="onRename"><el-icon><Edit /></el-icon> 重命名</div>
        <div class="menu-item danger" @click="onDelete"><el-icon><Delete /></el-icon> 删除</div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ArrowRight, Document, Folder, FolderOpened, DocumentAdd, FolderAdd, Edit, Delete } from '@element-plus/icons-vue'
import type { FileTreeNode } from '@/api/fileTree'

const props = defineProps<{
  node: FileTreeNode
  activeId: number | null
  depth?: number
  readonly?: boolean
}>()

const emit = defineEmits<{
  open: [node: FileTreeNode]
  'new-file': [node: FileTreeNode, name: string]
  'new-dir': [node: FileTreeNode, name: string]
  rename: [node: FileTreeNode, newName: string]
  delete: [node: FileTreeNode]
}>()

const depth = computed(() => props.depth ?? 0)
const isOpen = ref(false)

const nodeIcon = computed(() => {
  if (props.node.type === 'directory') return isOpen.value ? FolderOpened : Folder
  return Document
})

const handleClick = () => {
  if (props.node.type === 'directory') {
    isOpen.value = !isOpen.value
  } else {
    emit('open', props.node)
  }
}

// ── 行内新建 ──────────────────────────────────────────────
const inlineCreating = ref<'file' | 'dir' | null>(null)
const inlineName = ref('')
const inlineInputRef = ref<HTMLInputElement | null>(null)

const startInlineCreate = async (type: 'file' | 'dir') => {
  isOpen.value = true
  inlineCreating.value = type
  inlineName.value = ''
  await nextTick()
  inlineInputRef.value?.focus()
}

const submitInlineCreate = () => {
  const name = inlineName.value.trim()
  if (!name) { cancelInlineCreate(); return }
  emit(inlineCreating.value === 'file' ? 'new-file' : 'new-dir', props.node, name)
  cancelInlineCreate()
}

const cancelInlineCreate = () => {
  inlineCreating.value = null
  inlineName.value = ''
}

// ── 行内重命名 ────────────────────────────────────────────
const renaming = ref(false)
const renameName = ref('')
const renameInputRef = ref<HTMLInputElement | null>(null)

const startRename = async () => {
  closeMenu()
  renaming.value = true
  renameName.value = props.node.name
  await nextTick()
  renameInputRef.value?.focus()
  renameInputRef.value?.select()
}

const submitRename = () => {
  const name = renameName.value.trim()
  cancelRename()
  if (!name || name === props.node.name) return
  emit('rename', props.node, name)
}

const cancelRename = () => {
  renaming.value = false
  renameName.value = ''
}

// ── 右键菜单 ──────────────────────────────────────────────
const menuVisible = ref(false)
const menuX = ref(0)
const menuY = ref(0)

const showMenu = (e: MouseEvent) => {
  menuX.value = e.clientX
  menuY.value = e.clientY
  menuVisible.value = true
}

const closeMenu = () => { menuVisible.value = false }

const onNewFile = () => { closeMenu(); startInlineCreate('file') }
const onNewDir  = () => { closeMenu(); startInlineCreate('dir') }
const onRename  = () => { closeMenu(); startRename() }
const onDelete  = () => { closeMenu(); emit('delete', props.node) }

onMounted(() => document.addEventListener('click', closeMenu))
onUnmounted(() => document.removeEventListener('click', closeMenu))
</script>

<style scoped>
.tree-row {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 26px;
  cursor: pointer;
  font-size: 13px;
  color: #cccccc;
  user-select: none;
  border-radius: 2px;
  position: relative;
}

.tree-row:hover {
  background: #2a2d2e;
}

.tree-row.active {
  background: #094771;
  color: #ffffff;
}

.rename-row {
  cursor: default;
}

.arrow {
  font-size: 10px;
  color: #888;
  transition: transform 0.15s;
  flex-shrink: 0;
  width: 12px;
}

.arrow.expanded { transform: rotate(90deg); }
.arrow.invisible { visibility: hidden; }

.node-icon {
  font-size: 15px;
  flex-shrink: 0;
  color: #c5c5c5;
}

.directory .node-icon,
.node-icon.directory {
  color: #dcb862;
}

.node-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

/* 悬停操作图标 */
.row-actions {
  display: flex;
  align-items: center;
  gap: 1px;
  padding-right: 4px;
  opacity: 0;
  flex-shrink: 0;
}

.tree-row:hover .row-actions {
  opacity: 1;
}

.action-icon {
  font-size: 15px;
  color: #c5c5c5;
  padding: 3px;
  border-radius: 3px;
  cursor: pointer;
}

.action-icon:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.15);
}

.action-icon.danger:hover {
  color: #f48771;
  background: rgba(244, 135, 113, 0.15);
}

/* 行内输入（新建 & 重命名共用） */
.inline-row { cursor: default; }

.inline-input {
  flex: 1;
  min-width: 0;
  background: #1e3a5f;
  border: 1px solid #007acc;
  border-radius: 2px;
  color: #cccccc;
  font-size: 13px;
  padding: 0 4px;
  height: 20px;
  outline: none;
  font-family: inherit;
}

.empty-dir {
  height: 22px;
  display: flex;
  align-items: center;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: #252526;
  border: 1px solid #454545;
  border-radius: 4px;
  padding: 4px 0;
  z-index: 9999;
  min-width: 150px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  font-size: 13px;
  color: #cccccc;
  cursor: pointer;
}

.menu-item:hover { background: #094771; }
.menu-item.danger { color: #f48771; }
.menu-item.danger:hover { background: #5a1d1d; }

.menu-divider {
  height: 1px;
  background: #454545;
  margin: 4px 0;
}
</style>
