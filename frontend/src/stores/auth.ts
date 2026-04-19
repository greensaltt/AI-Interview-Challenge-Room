import { computed, reactive, readonly } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { authApi } from '../api/auth';
import type { CurrentUser, LoginRequest, RegisterRequest } from '../types/auth';
import { authStorage } from '../utils/auth-storage';

type AuthState = {
  initialized: boolean;
  initializing: boolean;
  accessToken: string | null;
  user: CurrentUser | null;
};

const state = reactive<AuthState>({
  initialized: false,
  initializing: false,
  accessToken: null,
  user: null
});

let initializePromise: Promise<void> | null = null;

const persistSession = () => {
  if (!state.accessToken || !state.user) {
    authStorage.clear();
    return;
  }

  authStorage.write({
    accessToken: state.accessToken,
    user: state.user
  });
};

const applySession = (accessToken: string, user: CurrentUser) => {
  state.accessToken = accessToken;
  state.user = user;
  persistSession();
};

const clearSession = () => {
  state.accessToken = null;
  state.user = null;
  authStorage.clear();
};

const normalizeRedirect = (redirectPath: string | null | undefined): RouteLocationRaw => {
  if (redirectPath && redirectPath.startsWith('/')) {
    return redirectPath;
  }

  return getDefaultLandingRoute();
};

export const hasRole = (roleCode: string) => state.user?.roleCodes.includes(roleCode) ?? false;

export const hasAnyRole = (roleCodes: string[]) => roleCodes.some((roleCode) => hasRole(roleCode));

export const getDefaultLandingRoute = (): RouteLocationRaw =>
  hasRole('ROLE_ADMIN') ? '/admin' : '/dashboard';

export const authStore = {
  state: readonly(state),
  isAuthenticated: computed(() => Boolean(state.accessToken && state.user)),
  displayName: computed(() => state.user?.nickname || state.user?.username || '未登录用户'),
  async initialize() {
    if (state.initialized) {
      return;
    }

    if (initializePromise) {
      return initializePromise;
    }

    initializePromise = (async () => {
      state.initializing = true;

      const storedSession = authStorage.read();
      if (!storedSession) {
        state.initialized = true;
        state.initializing = false;
        initializePromise = null;
        return;
      }

      state.accessToken = storedSession.accessToken;
      state.user = storedSession.user;

      try {
        const currentUser = await authApi.getCurrentUser(storedSession.accessToken);
        applySession(storedSession.accessToken, currentUser);
      } catch {
        clearSession();
      } finally {
        state.initialized = true;
        state.initializing = false;
        initializePromise = null;
      }
    })();

    return initializePromise;
  },
  async login(payload: LoginRequest) {
    const result = await authApi.login(payload);
    applySession(result.accessToken, result.user);
    return result;
  },
  async register(payload: RegisterRequest) {
    return authApi.register(payload);
  },
  async logout() {
    const accessToken = state.accessToken;
    clearSession();

    if (!accessToken) {
      return;
    }

    try {
      await authApi.logout(accessToken);
    } catch {
      // The API is stateless. Local cleanup is enough for this step.
    }
  },
  ensureInitialized() {
    return this.initialize();
  },
  canAccessRoles(roleCodes?: string[]) {
    if (!roleCodes || roleCodes.length === 0) {
      return true;
    }
    return hasAnyRole(roleCodes);
  },
  resolveLandingRoute(redirectPath?: string | null) {
    return normalizeRedirect(redirectPath);
  }
};

