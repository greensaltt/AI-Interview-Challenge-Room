export interface ApiErrorDetail {
  field: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  requestId: string;
  timestamp: string;
  data: T;
  errors?: ApiErrorDetail[];
}

