import type {
  AccessScopeResponse,
  AuthTokenResponse,
  CurrentUser,
  LoginRequest,
  LogoutResponse,
  RegisterRequest
} from '../types/auth';
import { requestJson } from './http';

export const authApi = {
  register(payload: RegisterRequest) {
    return requestJson<CurrentUser>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  login(payload: LoginRequest) {
    return requestJson<AuthTokenResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  logout(accessToken: string) {
    return requestJson<LogoutResponse>(
      '/api/auth/logout',
      {
        method: 'POST'
      },
      { accessToken }
    );
  },
  getCurrentUser(accessToken: string) {
    return requestJson<CurrentUser>('/api/auth/me', {}, { accessToken });
  },
  getUserAccessScope(accessToken: string) {
    return requestJson<AccessScopeResponse>('/api/user/access-scope', {}, { accessToken });
  },
  getAdminAccessScope(accessToken: string) {
    return requestJson<AccessScopeResponse>('/api/admin/access-scope', {}, { accessToken });
  }
};

