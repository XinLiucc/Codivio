<template>
  <div id="app">
    <!--router-view是路由组件的显示区域-->
    <router-view />
  </div>
</template>

<script setup lang="ts">

</script>

<style>
/* 亮色变量 */
:root {
  --app-bg: #f5f7fa;
  --sidebar-bg: #ffffff;
  --sidebar-border: #e8eaed;
  --sidebar-logo-border: #f0f0f0;
  --sidebar-footer-border: #f0f0f0;
  --nav-hover-bg: #f0f6ff;
  --nav-active-bg: #ecf5ff;
  --nav-text: #606266;
  --nav-active-text: #409eff;
  --user-name-color: #303133;
  --user-role-color: #909399;
  --logo-text-color: #1a1a2e;
}

/* 深色变量 */
html.dark {
  --app-bg: #1a1a1a;
  --sidebar-bg: #242424;
  --sidebar-border: #3a3a3a;
  --sidebar-logo-border: #333333;
  --sidebar-footer-border: #333333;
  --nav-hover-bg: #2a3a4a;
  --nav-active-bg: #1a3a5a;
  --nav-text: #aaaaaa;
  --nav-active-text: #409eff;
  --user-name-color: #e0e0e0;
  --user-role-color: #777777;
  --logo-text-color: #e0e0e0;
}

html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}

#app {
  height: 100%;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* ── 深色模式全局覆盖 ── */
html.dark body {
  color: #e0e0e0;
  background: #1a1a1a;
}

/* 页面内容区背景 */
html.dark .dashboard,
html.dark .profile-container,
html.dark .settings-container,
html.dark .projects-container,
html.dark .members-container {
  background-color: #1a1a1a !important;
}

/* 卡片 */
html.dark .el-card {
  background: #242424 !important;
  border-color: #3a3a3a !important;
  color: #e0e0e0 !important;
}

html.dark .el-card__header {
  border-bottom-color: #3a3a3a !important;
  color: #e0e0e0 !important;
}

/* 项目卡片（自定义） */
html.dark .project-card {
  background: #2a2a2a !important;
  border-color: #3a3a3a !important;
  color: #e0e0e0 !important;
}

html.dark .project-card:hover {
  border-color: #409eff !important;
}

html.dark .project-name {
  color: #e0e0e0 !important;
}

html.dark .project-description,
html.dark .project-time {
  color: #888 !important;
}

/* 统计卡片数字 */
html.dark .stats-number {
  color: #e0e0e0 !important;
}

html.dark .stats-label {
  color: #888 !important;
}

/* 欢迎区域 */
html.dark .dashboard-header {
  background: #242424 !important;
  box-shadow: 0 2px 4px rgba(0,0,0,0.3) !important;
}

html.dark .welcome-section h1 {
  color: #e0e0e0 !important;
}

html.dark .welcome-text {
  color: #888 !important;
}

/* 表单文字 */
html.dark .el-form-item__label {
  color: #aaa !important;
}

html.dark .info-text {
  color: #aaa !important;
}

/* 表格 */
html.dark .el-table {
  background: #242424 !important;
  color: #e0e0e0 !important;
}

html.dark .el-table th,
html.dark .el-table tr {
  background: #242424 !important;
  color: #e0e0e0 !important;
}

html.dark .el-table td,
html.dark .el-table--border th {
  border-color: #3a3a3a !important;
}

html.dark .el-table__inner-wrapper::before {
  background: #3a3a3a !important;
}

/* 输入框 */
html.dark .el-input__wrapper {
  background: #2a2a2a !important;
  box-shadow: 0 0 0 1px #4a4a4a inset !important;
}

html.dark .el-input__inner {
  color: #e0e0e0 !important;
}

/* card-header 文字 */
html.dark .card-header span,
html.dark .card-header {
  color: #e0e0e0 !important;
}

/* 确保 Element Plus 弹窗正确定位 */
.el-message-box__wrapper {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100% !important;
  height: 100% !important;
  z-index: 2001 !important;
}

.el-message-box {
  position: absolute !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
}
</style>