<template>
  <main class="auth-shell">
    <section class="auth-panel">
      <div class="auth-intro">
        <p class="section-kicker">欢迎回来</p>
        <h1 class="page-title">登录 AI 面试闯关作战室</h1>
        <p class="page-copy">
          支持使用用户名或邮箱登录。登录成功后会进入受保护页面，刷新后也会自动恢复登录态。
        </p>

        <div class="tips-card">
          <strong>默认管理员</strong>
          <span>账号：admin</span>
          <span>邮箱：123@qq.com</span>
          <span>密码：123456</span>
        </div>
      </div>

      <form class="auth-card" @submit.prevent="handleSubmit">
        <label class="field-group">
          <span>用户名或邮箱</span>
          <input v-model.trim="form.account" class="text-input" type="text" autocomplete="username" />
        </label>

        <label class="field-group">
          <span>密码</span>
          <input
            v-model="form.password"
            class="text-input"
            type="password"
            autocomplete="current-password"
          />
        </label>

        <p v-if="registerSuccessMessage" class="status-banner status-banner-success">
          {{ registerSuccessMessage }}
        </p>
        <p v-if="errorMessage" class="status-banner status-banner-error">{{ errorMessage }}</p>

        <button class="primary-button" type="submit" :disabled="submitting">
          {{ submitting ? '登录中...' : '登录并进入系统' }}
        </button>

        <div class="inline-actions">
          <RouterLink to="/">返回首页</RouterLink>
          <RouterLink to="/register">还没有账号？去注册</RouterLink>
        </div>
      </form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { extractErrorMessage } from '../../api/http';
import { authStore } from '../../stores/auth';

const route = useRoute();
const router = useRouter();

const form = reactive({
  account: typeof route.query.account === 'string' ? route.query.account : '',
  password: ''
});

const submitting = ref(false);
const errorMessage = ref('');

const registerSuccessMessage =
  route.query.registered === '1' ? '注册成功，现在可以直接登录了。' : '';

const handleSubmit = async () => {
  submitting.value = true;
  errorMessage.value = '';

  try {
    await authStore.login({
      account: form.account,
      password: form.password
    });

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null;
    await router.push(authStore.resolveLandingRoute(redirect));
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, '登录失败，请检查账号或密码。');
  } finally {
    submitting.value = false;
  }
};
</script>
