import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { authStore } from './stores/auth';
import './styles/index.css';

await authStore.initialize();

createApp(App).use(router).mount('#app');

