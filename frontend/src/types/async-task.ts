export type AsyncTaskStatus = 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED';

export interface AsyncTaskStatusResponse {
  taskId: string;
  status: AsyncTaskStatus;
  progress: number;
  message: string;
  pollAfterMillis: number;
  errorCode: string | null;
  result: unknown;
}
