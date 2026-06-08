'use client';

import Link from 'next/link';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { queryKeys } from '@/lib/query-keys';
import { useAuth } from '@/providers/auth-provider';
import { StatusBadge } from '@/components/status-badge';
import { SwitchUserMenu } from '@/components/switch-user-menu';
import type { PoSummary } from '@/lib/types';

const CATEGORY_LABELS: Record<string, string> = {
  SERVICES: 'Services',
  OFFICE_SUPPLIES: 'Office Supplies',
  IT_EQUIPMENT: 'IT Equipment',
};

function formatAmount(amount: number) {
  return amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

function PoTable({ rows, showCreator }: { rows: PoSummary[]; showCreator?: boolean }) {
  if (rows.length === 0) {
    return <p className="py-4 text-sm text-gray-400">No purchase orders found.</p>;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200">
      <table className="min-w-full divide-y divide-gray-200 text-sm">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left font-medium text-gray-500">Title</th>
            {showCreator && (
              <th className="px-4 py-3 text-left font-medium text-gray-500">Creator</th>
            )}
            <th className="px-4 py-3 text-left font-medium text-gray-500">Amount</th>
            <th className="px-4 py-3 text-left font-medium text-gray-500">Category</th>
            <th className="px-4 py-3 text-left font-medium text-gray-500">Status</th>
            <th className="px-4 py-3 text-left font-medium text-gray-500">Updated</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 bg-white">
          {rows.map((po) => (
            <tr
              key={po.id}
              className={po.status === 'NEEDS_REWORK' ? 'bg-red-50' : 'hover:bg-gray-50'}
            >
              <td className="px-4 py-3">
                <Link href={`/pos/${po.id}`} className="font-medium text-blue-600 hover:underline">
                  {po.title}
                </Link>
              </td>
              {showCreator && (
                <td className="px-4 py-3 text-gray-600">{po.creatorName}</td>
              )}
              <td className="px-4 py-3 text-gray-600">{formatAmount(po.amount)}</td>
              <td className="px-4 py-3 text-gray-600">
                {CATEGORY_LABELS[po.category] ?? po.category}
              </td>
              <td className="px-4 py-3">
                <StatusBadge status={po.status} />
              </td>
              <td className="px-4 py-3 text-gray-500">{formatDate(po.updatedAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default function DashboardPage() {
  const { currentUser, loading } = useAuth();

  const isReviewer = currentUser?.role !== 'CREATOR';

  const { data: queue, isLoading: queueLoading } = useQuery({
    queryKey: queryKeys.pos.list({ queue: 'me' }),
    queryFn: () => api.pos.list({ queue: 'me' }),
    enabled: !!currentUser && isReviewer,
  });

  const { data: myPos, isLoading: myPosLoading } = useQuery({
    queryKey: queryKeys.pos.list({ creator: 'me' }),
    queryFn: () => api.pos.list({ creator: 'me' }),
    enabled: !!currentUser,
  });

  if (loading || !currentUser) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white px-6 py-3">
        <div className="mx-auto flex max-w-5xl items-center justify-between">
          <h1 className="text-lg font-bold text-gray-900">PO Management</h1>
          <div className="flex items-center gap-3">
            <Link
              href="/pos/new"
              className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white transition-colors hover:bg-blue-700"
            >
              New PO
            </Link>
            <SwitchUserMenu />
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl space-y-8 px-6 py-8">
        {isReviewer && (
          <section>
            <h2 className="mb-4 text-base font-semibold text-gray-900">Awaiting my action</h2>
            {queueLoading ? (
              <p className="text-sm text-gray-400">Loading…</p>
            ) : (
              <PoTable rows={queue ?? []} showCreator />
            )}
          </section>
        )}

        <section>
          <h2 className="mb-4 text-base font-semibold text-gray-900">My purchase orders</h2>
          {myPosLoading ? (
            <p className="text-sm text-gray-400">Loading…</p>
          ) : (
            <PoTable rows={myPos ?? []} />
          )}
        </section>
      </main>
    </div>
  );
}
