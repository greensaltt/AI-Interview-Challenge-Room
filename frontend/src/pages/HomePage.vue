<template>
  <main class="marketing-shell">
    <section class="hero-panel">
      <div class="hero-copy">
        <p class="section-kicker">Step 08 / Frontend Auth</p>
        <h1 class="hero-title">把后端认证链路接进前端，形成最小可用闭环。</h1>
        <p class="page-copy">
          当前已经打通注册、登录、登录态持久化、刷新恢复、路由守卫和管理员角色页面跳转，
          用来承接后续简历、岗位、学习计划与模拟面试页面。
        </p>

        <div class="link-row">
          <RouterLink v-if="!authStore.isAuthenticated.value" class="primary-button" to="/login">
            去登录
          </RouterLink>
          <RouterLink
            v-if="!authStore.isAuthenticated.value"
            class="secondary-button"
            to="/register"
          >
            去注册
          </RouterLink>
          <RouterLink v-if="authStore.isAuthenticated.value" class="primary-button" to="/dashboard">
            进入工作台
          </RouterLink>
          <RouterLink
            v-if="authStore.canAccessRoles(['ROLE_ADMIN'])"
            class="secondary-button"
            to="/admin"
          >
            进入后台
          </RouterLink>
        </div>
      </div>

      <article class="hero-status">
        <p class="status-label">当前登录态</p>
        <h2>{{ authStore.isAuthenticated.value ? '已恢复本地会话' : '未登录' }}</h2>
        <p class="page-copy">
          {{
            authStore.isAuthenticated.value
              ? `当前用户：${authStore.displayName.value}`
              : '你可以先注册普通用户，也可以直接用默认管理员账号体验后台路由。'
          }}
        </p>
        <dl class="detail-grid" v-if="authStore.state.user">
          <div>
            <dt>用户名</dt>
            <dd>{{ authStore.state.user.username }}</dd>
          </div>
          <div>
            <dt>角色</dt>
            <dd>{{ authStore.state.user.roleCodes.join(', ') }}</dd>
          </div>
        </dl>
      </article>
    </section>
  </main>
</template>

<script setup lang="ts">
import { authStore } from '../stores/auth';
</script>
