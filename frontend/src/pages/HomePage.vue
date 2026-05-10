<template>
  <main class="marketing-shell">
    <header class="top-nav">
      <RouterLink class="brand-lockup" to="/">
        <span class="brand-symbol">AI</span>
        <span class="brand-text">
          <strong>AI 面试闯关作战室</strong>
          <span>Offer Dungeon</span>
        </span>
      </RouterLink>

      <div class="nav-actions">
        <RouterLink v-if="!authStore.isAuthenticated.value" class="text-button" to="/login">
          登录
        </RouterLink>
        <RouterLink v-if="!authStore.isAuthenticated.value" class="secondary-button" to="/register">
          创建账号
        </RouterLink>
        <RouterLink v-if="authStore.isAuthenticated.value" class="secondary-button" to="/dashboard">
          工作台
        </RouterLink>
      </div>
    </header>

    <section class="hero-panel">
      <div class="hero-copy">
        <p class="section-kicker">Campus Career Training MVP</p>
        <h1 class="hero-title">把求职准备，练成一条清晰的通关路线。</h1>
        <p class="hero-lede">
          当前版本已打通注册、登录、会话恢复、路由守卫和管理员入口。后续简历、岗位、
          学习计划与模拟面试会沿着这套工作台继续生长。
        </p>

        <div class="link-row">
          <RouterLink v-if="!authStore.isAuthenticated.value" class="primary-button" to="/login">
            进入训练室
          </RouterLink>
          <RouterLink
            v-if="!authStore.isAuthenticated.value"
            class="secondary-button"
            to="/register"
          >
            注册新账号
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
        <div>
          <p class="status-label">Session Status</p>
          <h2>{{ authStore.isAuthenticated.value ? '会话已就绪' : '等待登录' }}</h2>
          <p class="page-copy">
            {{
              authStore.isAuthenticated.value
                ? `当前用户：${authStore.displayName.value}`
                : '可注册普通用户，也可使用默认管理员账号体验后台权限链路。'
            }}
          </p>
        </div>

        <div class="status-board">
          <div class="status-step">
            <span class="step-index">01</span>
            <span>
              <strong>认证闭环</strong>
              <span>注册、登录、刷新恢复已完成</span>
            </span>
          </div>
          <div class="status-step">
            <span class="step-index">02</span>
            <span>
              <strong>权限边界</strong>
              <span>用户工作台与管理员入口已分流</span>
            </span>
          </div>
          <div class="status-step">
            <span class="step-index">03</span>
            <span>
              <strong>业务预留</strong>
              <span>为简历、JD、面试和报告承载布局</span>
            </span>
          </div>
        </div>

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
