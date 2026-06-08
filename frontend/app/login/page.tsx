'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { queryKeys } from '@/lib/query-keys';
import { useAuth } from '@/providers/auth-provider';
import type { Role } from '@/lib/types';

const ROLE_LABELS: Record<Role, string> = {
  CREATOR: 'Creator',
  MANAGER: 'Manager',
  IT_REP: 'IT Rep',
  FINANCE: 'Finance',
};

export default function LoginPage() {
  const { login } = useAuth();
  const [loggingIn, setLoggingIn] = useState<number | null>(null);

  const { data: users, isLoading, error } = useQuery({
    queryKey: queryKeys.users,
    queryFn: api.users.list,
  });

  async function handleLogin(userId: number) {
    setLoggingIn(userId);
    try {
      await login(userId);
    } catch {
      setLoggingIn(null);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm space-y-6 rounded-xl border border-gray-200 bg-white p-8 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">PO Management</h1>
          <p className="mt-1 text-sm text-gray-500">Select a user to sign in</p>
        </div>

        {isLoading && <p className="text-sm text-gray-400">Loading users…</p>}
        {error && <p className="text-sm text-red-500">Failed to load users.</p>}

        {users && (
          <ul className="divide-y divide-gray-100 overflow-hidden rounded-lg border border-gray-200">
            {users.map((user) => (
              <li key={user.id}>
                <button
                  onClick={() => handleLogin(user.id)}
                  disabled={loggingIn !== null}
                  className="flex w-full items-center justify-between px-4 py-3 text-left transition-colors hover:bg-gray-50 disabled:opacity-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
                >
                  <div>
                    <p className="text-sm font-medium text-gray-900">{user.name}</p>
                    <p className="text-xs text-gray-500">{ROLE_LABELS[user.role]}</p>
                  </div>
                  {loggingIn === user.id && (
                    <span className="text-xs text-gray-400">Signing in…</span>
                  )}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </main>
  );
}
