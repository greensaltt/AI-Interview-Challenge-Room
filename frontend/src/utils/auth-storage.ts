import type { StoredAuthSession } from '../types/auth';

const AUTH_SESSION_STORAGE_KEY = 'ai-interview-battle-room.auth-session';

const canUseStorage = () => typeof window !== 'undefined' && Boolean(window.localStorage);

export const authStorage = {
  read(): StoredAuthSession | null {
    if (!canUseStorage()) {
      return null;
    }

    const rawValue = window.localStorage.getItem(AUTH_SESSION_STORAGE_KEY);
    if (!rawValue) {
      return null;
    }

    try {
      const parsed = JSON.parse(rawValue) as StoredAuthSession;
      if (!parsed?.accessToken || !parsed?.user) {
        return null;
      }
      return parsed;
    } catch {
      return null;
    }
  },
  write(session: StoredAuthSession) {
    if (!canUseStorage()) {
      return;
    }

    window.localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session));
  },
  clear() {
    if (!canUseStorage()) {
      return;
    }

    window.localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
  }
};

