const parseNumber = (value: string | undefined, fallback: number) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
};

export const runtimeConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  taskStatusBasePath: import.meta.env.VITE_TASK_STATUS_BASE_PATH ?? '/api/tasks',
  taskStatusPollIntervalMs: parseNumber(import.meta.env.VITE_TASK_STATUS_POLL_INTERVAL_MS, 3000),
};
