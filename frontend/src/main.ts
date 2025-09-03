import { createApp } from 'vue'
import App from './App.vue'
import pinia from './stores'

const app = createApp(App)

// 使用 Pinia
app.use(pinia)

app.mount('#app')
