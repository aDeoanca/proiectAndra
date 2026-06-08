'use client';

import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import type { User } from '@/lib/types';

interface AuthContextValue {
  currentUser: User | null;
  loading: boolean;
  login: (userId: number) => Promise<void>;
  logout: () => Promise<void>;
  switchUser: (userId: number) => Promise<void>;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

const PUBLIC_PATHS = ['/login'];

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const pathname = usePathname();

  const refresh = useCallback(async () => {
    try {
      const user = await api.auth.me();
      setCurrentUser(user);
    } catch {
      setCurrentUser(null);
    }
  }, []);

  useEffect(() => {
    api.auth
      .me()
      .then(setCurrentUser)
      .catch(() => setCurrentUser(null))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!loading && !currentUser && !PUBLIC_PATHS.includes(pathname)) {
      router.replace('/login');
    }
  }, [loading, currentUser, pathname, router]);

  const login = async (userId: number) => {
    const user = await api.auth.login(userId);
    setCurrentUser(user);
    router.replace('/dashboard');
  };

  const logout = async () => {
    await api.auth.logout();
    setCurrentUser(null);
    router.replace('/login');
  };

  const switchUser = async (userId: number) => {
    const user = await api.auth.login(userId);
    setCurrentUser(user);
    router.replace('/dashboard');
  };

  return (
    <AuthContext.Provider value={{ currentUser, loading, login, logout, switchUser, refresh }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
