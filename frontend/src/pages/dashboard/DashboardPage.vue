<template>
  <section class="content-stack">
    <article class="surface-card hero-card">
      <p class="section-kicker">Protected Workspace</p>
      <h2 class="page-title">今天先确认身份，下一步再开始训练。</h2>
      <p class="page-copy">
        这个工作台由前端路由守卫保护，并会调用后端普通用户接口校验 Token。
        当前阶段保持功能轻量，为后续简历、岗位、学习计划和模拟面试模块提供承载空间。
      </p>
    </article>

    <div class="metric-grid">
      <article class="metric-card">
        <strong>08</strong>
        <span>当前实施步骤</span>
      </article>
      <article class="metric-card">
        <strong>JWT</strong>
        <span>登录态保护方式</span>
      </article>
      <article class="metric-card">
        <strong>2</strong>
        <span>已接入角色入口</span>
      </article>
    </div>

    <article class="surface-card">
      <div class="card-heading">
        <h3>当前会话信息</h3>
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
          <dt>用户名</dt>
          <dd>{{ scope.username }}</dd>
        </div>
        <div>
          <dt>昵称</dt>
          <dd>{{ scope.nickname }}</dd>
        </div>
        <div>
          <dt>角色</dt>
          <dd>{{ scope.roleCodes.join(', ') }}</dd>
        </div>
      </dl>
    </article>

    <article class="surface-card">
      <div class="card-heading">
        <h3>后续业务模块</h3>
      </div>
      <div class="quick-grid">
        <div class="quick-card">
          <strong>简历与岗位</strong>
          <p>上传简历、维护目标岗位，并为 JD 解析与匹配分析准备数据。</p>
        </div>
        <div class="quick-card">
          <strong>计划与任务</strong>
          <p>把能力差距拆成阶段计划和每日任务，形成可追踪的训练节奏。</p>
        </div>
        <div class="quick-card">
          <strong>模拟面试</strong>
          <p>围绕岗位、简历和题库进行多轮文本面试，并保留问答记录。</p>
        </div>
        <div class="quick-card">
          <strong>复盘报告</strong>
          <p>生成结构化评分、薄弱点和改进建议，支撑下一轮训练。</p>
        </div>
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
    errorMessage.value = '当前未检测到登录态，请重新登录。';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    scope.value = await authApi.getUserAccessScope(authStore.state.accessToken);
  } catch (error) {
    scope.value = null;
    errorMessage.value = extractErrorMessage(error, '普通用户访问范围校验失败。');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  void loadScope();
});
</script>
