import { runtimeConfig } from '../config/runtime';
import type { ApiErrorDetail, ApiResponse } from '../types/api';

export class ApiClientError extends Error {
  code: string;
  status: number;
  requestId?: string;
  details: ApiErrorDetail[];

  constructor(options: {
    message: string;
    code?: string;
    status: number;
    requestId?: string;
    details?: ApiErrorDetail[];
  }) {
    super(options.message);
    this.name = 'ApiClientError';
    this.code = options.code ?? 'REQUEST_FAILED';
    this.status = options.status;
    this.requestId = options.requestId;
    this.details = options.details ?? [];
  }
}

const resolveUrl = (path: string) => {
  if (/^https?:\/\//.test(runtimeConfig.apiBaseUrl)) {
    return new URL(path, runtimeConfig.apiBaseUrl).toString();
  }

  const normalizedBase = runtimeConfig.apiBaseUrl.endsWith('/')
    ? runtimeConfig.apiBaseUrl.slice(0, -1)
    : runtimeConfig.apiBaseUrl;

  return `${normalizedBase}${path}`;
};

const buildHeaders = (initHeaders: HeadersInit | undefined, hasJsonBody: boolean) => {
  const headers = new Headers(initHeaders);
  headers.set('Accept', 'application/json');
  if (hasJsonBody && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  return headers;
};

export const extractErrorMessage = (error: unknown, fallback = '请求失败，请稍后重试。') => {
  if (error instanceof ApiClientError) {
    return error.message;
  }
  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }
  return fallback;
};

export const requestJson = async <T>(
  path: string,
  init: RequestInit = {},
  options: { accessToken?: string } = {}
) => {
  const hasJsonBody = typeof init.body === 'string';
  const headers = buildHeaders(init.headers, hasJsonBody);

  if (options.accessToken) {
    headers.set('Authorization', `Bearer ${options.accessToken}`);
  }

  const response = await fetch(resolveUrl(path), {
    ...init,
    headers
  });

  let payload: ApiResponse<T> | null = null;

  try {
    payload = (await response.json()) as ApiResponse<T>;
  } catch {
    payload = null;
  }

  if (!response.ok || !payload?.success) {
    throw new ApiClientError({
      message: payload?.message ?? '请求失败，请检查后端服务是否已启动。',
      code: payload?.code,
      status: response.status,
      requestId: payload?.requestId,
      details: payload?.errors
    });
  }

  return payload.data;
};

