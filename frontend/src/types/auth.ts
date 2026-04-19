export interface CurrentUser {
  id: number;
  username: string;
  email: string;
  nickname: string;
  userStatus: string;
  roleCodes: string[];
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  nickname?: string;
}

export interface LoginRequest {
  account: string;
  password: string;
}

export interface AuthTokenResponse {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  user: CurrentUser;
}

export interface LogoutResponse {
  message: string;
}

export interface AccessScopeResponse {
  scope: string;
  userId: number;
  username: string;
  nickname: string;
  roleCodes: string[];
}

export interface StoredAuthSession {
  accessToken: string;
  user: CurrentUser;
}

