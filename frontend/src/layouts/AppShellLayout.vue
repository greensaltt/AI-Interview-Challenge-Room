<template>
  <div class="app-shell">
    <aside class="app-sidebar">
      <RouterLink class="brand-mark" to="/">
        <span class="brand-symbol">AI</span>
        <span class="brand-text">
          <strong>AI 面试闯关作战室</strong>
          <span>Offer Dungeon</span>
        </span>
      </RouterLink>

      <nav class="sidebar-nav">
        <p class="sidebar-section-title">Workspace</p>
        <RouterLink class="sidebar-link" to="/dashboard">
          <span>工作台</span>
          <small>Ready</small>
        </RouterLink>
        <span class="sidebar-link sidebar-link-muted">
          <span>简历管理</span>
          <small>Step 10</small>
        </span>
        <span class="sidebar-link sidebar-link-muted">
          <span>岗位目标</span>
          <small>Step 13</small>
        </span>
        <span class="sidebar-link sidebar-link-muted">
          <span>学习计划</span>
          <small>Step 22</small>
        </span>
        <span class="sidebar-link sidebar-link-muted">
          <span>每日任务</span>
          <small>Step 24</small>
        </span>
        <span class="sidebar-link sidebar-link-muted">
          <span>模拟面试</span>
          <small>Step 27</small>
        </span>
        <span class="sidebar-link sidebar-link-muted">
          <span>复盘报告</span>
          <small>Step 32</small>
        </span>

        <p v-if="authStore.canAccessRoles(['ROLE_ADMIN'])" class="sidebar-section-title">
          Admin
        </p>
        <RouterLink
          v-if="authStore.canAccessRoles(['ROLE_ADMIN'])"
          class="sidebar-link"
          to="/admin"
        >
          <span>后台管理</span>
          <small>ROLE_ADMIN</small>
        </RouterLink>
      </nav>
    </aside>

    <div class="app-main">
      <header class="app-header">
        <div>
          <p class="section-kicker">Step 08</p>
          <h1 class="section-title">{{ pageTitle }}</h1>
        </div>

        <div class="header-actions">
          <div class="user-badge">
            <span>{{ authStore.displayName.value }}</span>
            <small>{{ authStore.state.user?.roleCodes.join(' / ') }}</small>
          </div>
          <button class="ghost-button" type="button" @click="handleLogout">退出登录</button>
        </div>
      </header>

      <main class="app-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { authStore } from '../stores/auth';

const route = useRoute();
const router = useRouter();

const pageTitle = computed(() => route.meta.title ?? '用户工作区');

const handleLogout = async () => {
  await authStore.logout();
  await router.push('/login');
};
</script>
