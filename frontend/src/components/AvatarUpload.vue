<template>
  <div class="avatar-upload">
    <el-upload
      class="avatar-uploader"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :loading="uploading"
    >
      <div class="avatar-container">
        <el-avatar 
          :size="size" 
          :src="avatarUrl" 
          :icon="UserFilled"
          class="avatar-display"
        />
        <div class="upload-overlay">
          <el-icon><Camera /></el-icon>
          <span class="upload-text">{{ uploading ? '上传中...' : '更换头像' }}</span>
        </div>
      </div>
    </el-upload>
    
    <div class="upload-tips">
      <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
      <p>建议尺寸：200x200 像素</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, Camera } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { UploadProps } from 'element-plus'

interface Props {
  avatarUrl?: string
  size?: number
  disabled?: boolean
}

interface Emits {
  (e: 'success', url: string): void
  (e: 'error', error: Error): void
}

const props = withDefaults(defineProps<Props>(), {
  size: 80,
  disabled: false
})

const emit = defineEmits<Emits>()
const authStore = useAuthStore()

const uploading = ref(false)

const uploadUrl = computed(() => '/api/v1/users/avatar')

const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${authStore.token}`
}))

// 上传前验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
  if (!isImage) {
    ElMessage.error('仅支持 JPG、PNG、GIF 格式')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('文件大小不能超过 2MB')
    return false
  }
  uploading.value = true
  return true
}

// 上传成功
const handleSuccess: UploadProps['onSuccess'] = (response) => {
  uploading.value = false
  if (response.code === 200) {
    ElMessage.success('头像上传成功')
    emit('success', response.data.avatarUrl)
  } else {
    ElMessage.error(response.message || '上传失败')
    emit('error', new Error(response.message))
  }
}

// 上传失败
const handleError: UploadProps['onError'] = (error) => {
  uploading.value = false
  console.error('Upload error:', error)
  
  ElMessage.error('头像上传失败，请重试')
  emit('error', error)
}
</script>

<style scoped>
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-uploader :deep(.el-upload) {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-container {
  position: relative;
  display: inline-block;
}

.avatar-display {
  display: block;
  transition: all 0.3s ease;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  opacity: 0;
  transition: all 0.3s ease;
}

.avatar-container:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.upload-text {
  font-size: 12px;
  text-align: center;
}

.upload-tips {
  text-align: center;
}

.upload-tips p {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

/* 禁用状态 */
.avatar-uploader.is-disabled :deep(.el-upload) {
  cursor: not-allowed;
}

.avatar-uploader.is-disabled .avatar-container:hover .upload-overlay {
  opacity: 0;
}

/* 上传中状态 */
.avatar-container.uploading .avatar-display {
  opacity: 0.6;
}

.avatar-container.uploading .upload-overlay {
  opacity: 1;
  background: rgba(64, 158, 255, 0.8);
}
</style>