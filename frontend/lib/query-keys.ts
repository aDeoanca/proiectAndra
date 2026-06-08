export const queryKeys = {
  currentUser: ['currentUser'] as const,
  users: ['users'] as const,
  pos: {
    all: ['pos'] as const,
    list: (params: Record<string, string | undefined>) =>
      ['pos', 'list', params] as const,
    detail: (id: number) => ['pos', id] as const,
  },
} as const;
