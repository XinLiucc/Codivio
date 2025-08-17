<template>
  <div id="app">
    <h1>{{ title }}</h1>
    <p>✅ Vue 3 框架运行正常</p>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      title: 'Codivio 前端框架'
    }
  }
}
</script>

<style>
#app {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  text-align: center;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

h1 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

p {
  color: #27ae60;
  font-size: 1.2rem;
}
</style>