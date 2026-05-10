<template>
  <main class="auth-shell">
    <header class="auth-topbar">
      <RouterLink class="mini-brand" to="/">
        <span class="brand-symbol">AI</span>
        <span class="brand-text">
          <strong>AI 面试闯关作战室</strong>
          <span>创建训练账号</span>
        </span>
      </RouterLink>
    </header>

    <section class="auth-panel">
      <div class="auth-intro">
        <p class="section-kicker">New Candidate</p>
        <h1 class="page-title">先拥有一张自己的训练档案。</h1>
        <p class="page-copy">
          注册完成后会回到登录页。当前阶段只保留最小账号链路，让后续简历、岗位和面试数据都有明确归属。
        </p>
      </div>

      <form class="auth-card" @submit.prevent="handleSubmit">
        <p class="eyebrow">Create Account</p>

        <label class="field-group">
          <span>用户名</span>
          <input
            v-model.trim="form.username"
            class="text-input"
            type="text"
            autocomplete="username"
            placeholder="用于登录的唯一账号"
          />
        </label>

        <label class="field-group">
          <span>邮箱</span>
          <input
            v-model.trim="form.email"
            class="text-input"
            type="email"
            autocomplete="email"
            placeholder="name@example.com"
          />
        </label>

        <label class="field-group">
          <span>昵称</span>
          <input
            v-model.trim="form.nickname"
            class="text-input"
            type="text"
            autocomplete="nickname"
            placeholder="可留空，默认使用用户名"
          />
        </label>

        <label class="field-group">
          <span>密码</span>
          <input
            v-model="form.password"
            class="text-input"
            type="password"
            autocomplete="new-password"
          />
        </label>

        <label class="field-group">
          <span>确认密码</span>
          <input
            v-model="form.confirmPassword"
            class="text-input"
            type="password"
            autocomplete="new-password"
          />
        </label>

        <p v-if="errorMessage" class="status-banner status-banner-error">{{ errorMessage }}</p>

        <button class="primary-button" type="submit" :disabled="submitting">
          {{ submitting ? '注册中...' : '创建账号' }}
        </button>

        <div class="inline-actions">
          <RouterLink to="/">返回首页</RouterLink>
          <RouterLink to="/login">已有账号？去登录</RouterLink>
        </div>
      </form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { extractErrorMessage } from '../../api/http';
import { authStore } from '../../stores/auth';

const router = useRouter();

const form = reactive({
  username: '',
  email: '',
  nickname: '',
  password: '',
  confirmPassword: ''
});

const submitting = ref(false);
const errorMessage = ref('');

const handleSubmit = async () => {
  if (form.password !== form.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致，请重新检查。';
    return;
  }

  submitting.value = true;
  errorMessage.value = '';

  try {
    await authStore.register({
      username: form.username,
      email: form.email,
      nickname: form.nickname,
      password: form.password
    });

    await router.push({
      path: '/login',
      query: {
        account: form.username,
        registered: '1'
      }
    });
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, '注册失败，请稍后重试。');
  } finally {
    submitting.value = false;
  }
};
</script>
