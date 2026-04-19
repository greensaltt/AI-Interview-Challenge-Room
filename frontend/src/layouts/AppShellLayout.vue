<template>
  <div class="app-shell">
    <aside class="app-sidebar">
      <RouterLink class="brand-mark" to="/">
        <span class="brand-kicker">Offer Dungeon</span>
        <strong>AI 面试闯关作战室</strong>
      </RouterLink>

      <nav class="sidebar-nav">
        <RouterLink class="sidebar-link" to="/dashboard">工作台</RouterLink>
        <span class="sidebar-link sidebar-link-muted">简历管理 · 第 10 步后接入</span>
        <span class="sidebar-link sidebar-link-muted">岗位目标 · 第 13 步后接入</span>
        <span class="sidebar-link sidebar-link-muted">学习计划 · 第 22 步后接入</span>
        <span class="sidebar-link sidebar-link-muted">每日任务 · 第 24 步后接入</span>
        <span class="sidebar-link sidebar-link-muted">模拟面试 · 第 27 步后接入</span>
        <span class="sidebar-link sidebar-link-muted">复盘报告 · 第 32 步后接入</span>
        <RouterLink
          v-if="authStore.canAccessRoles(['ROLE_ADMIN'])"
          class="sidebar-link"
          to="/admin"
        >
          后台管理
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
            <span>{{ authStore.displayName }}</span>
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

