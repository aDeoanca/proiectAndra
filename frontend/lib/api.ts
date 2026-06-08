import type { User, PoDetail, PoSummary, CreatePoRequest, UpdatePoRequest } from './types';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  constructor(
    public readonly code: string,
    message: string,
    public readonly details?: Record<string, string>,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  });
  if (res.status === 204) return undefined as T;
  const body = await res.json().catch(() => ({}));
  if (!res.ok) {
    const err = (body as { error?: { code?: string; message?: string; details?: Record<string, string> } })?.error ?? {};
    throw new ApiError(err.code ?? 'UNKNOWN', err.message ?? 'Request failed', err.details);
  }
  return body as T;
}

export const api = {
  auth: {
    me: () => request<User>('/api/auth/me'),
    login: (userId: number) =>
      request<User>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ userId }),
      }),
    logout: () => request<void>('/api/auth/logout', { method: 'POST' }),
  },

  users: {
    list: () => request<User[]>('/api/users'),
  },

  pos: {
    list: (params?: { queue?: string; creator?: string; status?: string }) => {
      const qs = new URLSearchParams(
        Object.fromEntries(
          Object.entries(params ?? {}).filter(([, v]) => v != null) as [string, string][],
        ),
      ).toString();
      return request<PoSummary[]>(`/api/pos${qs ? `?${qs}` : ''}`);
    },
    get: (id: number) => request<PoDetail>(`/api/pos/${id}`),
    create: (data: CreatePoRequest) =>
      request<PoDetail>('/api/pos', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: number, data: UpdatePoRequest) =>
      request<PoDetail>(`/api/pos/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
    approve: (id: number) =>
      request<PoDetail>(`/api/pos/${id}/approve`, { method: 'POST' }),
    reject: (id: number, comment: string) =>
      request<PoDetail>(`/api/pos/${id}/reject`, {
        method: 'POST',
        body: JSON.stringify({ comment }),
      }),
    resubmit: (id: number) =>
      request<PoDetail>(`/api/pos/${id}/resubmit`, { method: 'POST' }),
  },
};
