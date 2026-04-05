<template>
  <div class="tree-node">
    <!-- 节点行 -->
    <div
      class="tree-row"
      :class="{ active: node.type === 'FILE' && node.id === activeId, directory: node.type === 'DIRECTORY' }"
      :style="{ paddingLeft: depth * 16 + 8 + 'px' }"
      @click="handleClick"
      @contextmenu.prevent="showMenu"
    >
      <!-- 展开箭头 -->
      <el-icon class="arrow" :class="{ expanded: isOpen, invisible: node.type === 'FILE' }">
        <ArrowRight />
      </el-icon>

      <!-- 文件/目录图标 -->
      <el-icon class="node-icon">
        <component :is="nodeIcon" />
      </el-icon>

      <span class="node-name">{{ node.name }}</span>
    </div>

    <!-- 子节点（目录展开时显示） -->
    <div v-if="node.type === 'DIRECTORY' && isOpen">
      <div v-if="node.children && node.children.length === 0" class="empty-dir" :style="{ paddingLeft: (depth + 1) * 16 + 8 + 'px' }">
        <el-text size="small" type="info">空目录</el-text>
      </div>
      <FileTreeItem
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :active-id="activeId"
        :depth="depth + 1"
        @open="$emit('open', $event)"
        @new-file="$emit('new-file', $event)"
        @new-dir="$emit('new-dir', $event)"
        @rename="$emit('rename', $event)"
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
        <template v-if="node.type === 'DIRECTORY'">
          <div class="menu-item" @click="onNewFile">
            <el-icon><DocumentAdd /></el-icon> 新建文件
          </div>
          <div class="menu-item" @click="onNewDir">
            <el-icon><FolderAdd /></el-icon> 新建目录
          </div>
          <div class="menu-divider" />
        </template>
        <div class="menu-item" @click="onRename">
          <el-icon><Edit /></el-icon> 重命名
        </div>
        <div class="menu-item danger" @click="onDelete">
          <el-icon><Delete /></el-icon> 删除
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowRight, Document, Folder, FolderOpened, DocumentAdd, FolderAdd, Edit, Delete } from '@element-plus/icons-vue'
import type { FileTreeNode } from '@/api/fileTree'

const props = defineProps<{
  node: FileTreeNode
  activeId: number | null
  depth?: number
}>()

const emit = defineEmits<{
  open: [node: FileTreeNode]
  'new-file': [node: FileTreeNode]
  'new-dir': [node: FileTreeNode]
  rename: [node: FileTreeNode]
  delete: [node: FileTreeNode]
}>()

const depth = computed(() => props.depth ?? 0)
const isOpen = ref(false)

const nodeIcon = computed(() => {
  if (props.node.type === 'DIRECTORY') return isOpen.value ? FolderOpened : Folder
  return Document
})

const handleClick = () => {
  if (props.node.type === 'DIRECTORY') {
    isOpen.value = !isOpen.value
  } else {
    emit('open', props.node)
  }
}

// 右键菜单
const menuVisible = ref(false)
const menuX = ref(0)
const menuY = ref(0)

const showMenu = (e: MouseEvent) => {
  menuX.value = e.clientX
  menuY.value = e.clientY
  menuVisible.value = true
}

const closeMenu = () => { menuVisible.value = false }

const onNewFile = () => { closeMenu(); emit('new-file', props.node) }
const onNewDir = () => { closeMenu(); emit('new-dir', props.node) }
const onRename = () => { closeMenu(); emit('rename', props.node) }
const onDelete = () => { closeMenu(); emit('delete', props.node) }

onMounted(() => document.addEventListener('click', closeMenu))
onUnmounted(() => document.removeEventListener('click', closeMenu))
</script>

<style scoped>
.tree-row {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 24px;
  cursor: pointer;
  font-size: 13px;
  color: #cccccc;
  user-select: none;
  border-radius: 2px;
}

.tree-row:hover {
  background: #2a2d2e;
}

.tree-row.active {
  background: #094771;
  color: #ffffff;
}

.arrow {
  font-size: 10px;
  color: #888;
  transition: transform 0.15s;
  flex-shrink: 0;
  width: 12px;
}

.arrow.expanded {
  transform: rotate(90deg);
}

.arrow.invisible {
  visibility: hidden;
}

.node-icon {
  font-size: 14px;
  flex-shrink: 0;
  color: #c5c5c5;
}

.directory .node-icon {
  color: #dcb862;
}

.node-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  min-width: 140px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  font-size: 13px;
  color: #cccccc;
  cursor: pointer;
}

.menu-item:hover {
  background: #094771;
}

.menu-item.danger {
  color: #f48771;
}

.menu-item.danger:hover {
  background: #5a1d1d;
}

.menu-divider {
  height: 1px;
  background: #454545;
  margin: 4px 0;
}
</style>
