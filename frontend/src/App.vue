<template>
  <div id="app">
    <!--router-view是路由组件的显示区域-->
    <router-view />
  </div>
</template>

<script setup lang="ts">

</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
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