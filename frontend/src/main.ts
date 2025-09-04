import { createApp } from 'vue'
import App from './App.vue'
import pinia from './stores'
import router from "@/router";

const app = createApp(App)

// 使用 Pinia
app.use(pinia)
app.use(router)

app.mount('#app')
