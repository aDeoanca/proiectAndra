'use client';

import { useEffect, useRef, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
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

export function SwitchUserMenu() {
  const { currentUser, switchUser } = useAuth();
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const { data: users } = useQuery({
    queryKey: queryKeys.users,
    queryFn: api.users.list,
  });

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  async function handleSwitch(userId: number) {
    setOpen(false);
    try {
      await switchUser(userId);
      queryClient.invalidateQueries({ queryKey: queryKeys.pos.all });
    } catch {
      // auth state unchanged on failure
    }
  }

  const otherUsers = users?.filter((u) => u.id !== currentUser?.id) ?? [];

  return (
    <div ref={containerRef} className="relative">
      <button
        onClick={() => setOpen((v) => !v)}
        className="flex items-center gap-2 rounded-md border border-gray-200 bg-white px-3 py-1.5 text-sm text-gray-700 shadow-sm transition-colors hover:bg-gray-50"
      >
        <span className="font-medium">{currentUser?.name}</span>
        <span className="text-xs text-gray-400">
          {currentUser ? ROLE_LABELS[currentUser.role] : ''}
        </span>
        <svg className="h-3.5 w-3.5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      {open && otherUsers.length > 0 && (
        <div className="absolute right-0 z-20 mt-1 w-52 rounded-md border border-gray-200 bg-white shadow-lg">
          <div className="p-1">
            <p className="px-2 py-1.5 text-xs font-semibold uppercase tracking-wide text-gray-400">
              Switch user
            </p>
            {otherUsers.map((user) => (
              <button
                key={user.id}
                onClick={() => handleSwitch(user.id)}
                className="flex w-full items-center justify-between rounded px-2 py-2 text-sm text-gray-700 hover:bg-gray-50"
              >
                <span>{user.name}</span>
                <span className="text-xs text-gray-400">{ROLE_LABELS[user.role]}</span>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
