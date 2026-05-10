<template>
  <section class="content-stack">
    <article class="surface-card hero-card">
      <p class="section-kicker">ROLE_ADMIN</p>
      <h2 class="page-title">后台入口已经接入权限边界。</h2>
      <p class="page-copy">
        这个页面用于验证“前端角色判断 + 后端管理员接口权限”是否协同工作。
        后续用户管理、题库维护、面试记录和 Prompt 模板都会放在这套后台框架里。
      </p>
    </article>

    <article class="surface-card">
      <div class="card-heading">
        <h3>管理员访问范围校验</h3>
        <button class="ghost-button" type="button" @click="loadScope" :disabled="loading">
          {{ loading ? '刷新中...' : '重新校验' }}
        </button>
      </div>

      <p v-if="errorMessage" class="status-banner status-banner-error">{{ errorMessage }}</p>

      <dl v-if="scope" class="detail-grid">
        <div>
          <dt>访问范围</dt>
          <dd>{{ scope.scope }}</dd>
        </div>
        <div>
          <dt>用户 ID</dt>
          <dd>{{ scope.userId }}</dd>
        </div>
        <div>
          <dt>用户名</dt>
          <dd>{{ scope.username }}</dd>
        </div>
        <div>
          <dt>角色</dt>
          <dd>{{ scope.roleCodes.join(', ') }}</dd>
        </div>
      </dl>
    </article>

    <article class="surface-card">
      <div class="card-heading">
        <h3>后台能力预留</h3>
      </div>
      <div class="pill-row">
        <span class="pill">用户管理</span>
        <span class="pill">题库维护</span>
        <span class="pill">岗位模板</span>
        <span class="pill">面试记录</span>
        <span class="pill">复盘报告</span>
        <span class="pill">Prompt 模板</span>
        <span class="pill pill-muted">操作日志</span>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { authApi } from '../../api/auth';
import { extractErrorMessage } from '../../api/http';
import { authStore } from '../../stores/auth';
import type { AccessScopeResponse } from '../../types/auth';

const scope = ref<AccessScopeResponse | null>(null);
const errorMessage = ref('');
const loading = ref(false);

const loadScope = async () => {
  if (!authStore.state.accessToken) {
    errorMessage.value = '当前未检测到登录态，请重新登录后再试。';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    scope.value = await authApi.getAdminAccessScope(authStore.state.accessToken);
  } catch (error) {
    scope.value = null;
    errorMessage.value = extractErrorMessage(error, '管理员访问范围校验失败。');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  void loadScope();
});
</script>
