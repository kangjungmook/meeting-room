import { createApp } from 'vue'
import AppRoot from './AppRoot.vue'
import './style.css'
import router from './router'

createApp(AppRoot).use(router).mount('#app')