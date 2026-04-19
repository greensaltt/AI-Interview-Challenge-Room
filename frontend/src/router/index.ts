import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../pages/HomePage.vue';
import LoginPage from '../pages/auth/LoginPage.vue';
import RegisterPage from '../pages/auth/RegisterPage.vue';
import DashboardPage from '../pages/dashboard/DashboardPage.vue';
import AdminConsolePage from '../pages/admin/AdminConsolePage.vue';
import AppShellLayout from '../layouts/AppShellLayout.vue';
import { authStore } from '../stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
      meta: {
        title: '首页'
      }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: {
        guestOnly: true,
        title: '登录'
      }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterPage,
      meta: {
        guestOnly: true,
        title: '注册'
      }
    },
    {
      path: '/dashboard',
      component: AppShellLayout,
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardPage,
          meta: {
            requiresAuth: true,
            title: '用户工作台'
          }
        }
      ]
    },
    {
      path: '/admin',
      component: AppShellLayout,
      meta: {
        requiresAuth: true,
        roles: ['ROLE_ADMIN']
      },
      children: [
        {
          path: '',
          name: 'admin-console',
          component: AdminConsolePage,
          meta: {
            requiresAuth: true,
            roles: ['ROLE_ADMIN'],
            title: '后台管理'
          }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
});

router.beforeEach(async (to) => {
  await authStore.ensureInitialized();

  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth);
  const guestOnly = to.matched.some((record) => record.meta.guestOnly);
  const requiredRoles = to.matched.flatMap((record) => record.meta.roles ?? []);
  const isAuthenticated = authStore.isAuthenticated.value;

  if (guestOnly && isAuthenticated) {
    return authStore.resolveLandingRoute();
  }

  if (requiresAuth && !isAuthenticated) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    };
  }

  if (requiredRoles.length > 0 && !authStore.canAccessRoles(requiredRoles)) {
    return '/dashboard';
  }

  return true;
});

export default router;
