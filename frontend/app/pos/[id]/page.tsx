'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '@/lib/api';
import { queryKeys } from '@/lib/query-keys';
import { useAuth } from '@/providers/auth-provider';
import { StatusBadge } from '@/components/status-badge';
import { RejectDialog } from '@/components/reject-dialog';
import type { Role, Status } from '@/lib/types';

const ROLE_TO_STATUS: Partial<Record<Role, Status>> = {
  MANAGER: 'PENDING_MANAGER_APPROVAL',
  IT_REP: 'PENDING_IT_VALIDATION',
  FINANCE: 'PENDING_FINANCE_APPROVAL',
};

const ACTION_LABELS: Record<string, string> = {
  SUBMIT: 'Submitted',
  APPROVE: 'Approved',
  REJECT: 'Rejected',
  RESUBMIT: 'Resubmitted',
};

const STATUS_LABELS: Record<Status, string> = {
  PENDING_MANAGER_APPROVAL: 'Pending Manager',
  PENDING_IT_VALIDATION: 'Pending IT',
  PENDING_FINANCE_APPROVAL: 'Pending Finance',
  NEEDS_REWORK: 'Needs Rework',
  INVOICED: 'Invoiced',
};

const CATEGORY_LABELS: Record<string, string> = {
  SERVICES: 'Services',
  OFFICE_SUPPLIES: 'Office Supplies',
  IT_EQUIPMENT: 'IT Equipment',
};

function formatAmount(amount: number, currency: string) {
  return `${currency} ${amount.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

export default function PoDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { currentUser } = useAuth();
  const queryClient = useQueryClient();

  const [rejectOpen, setRejectOpen] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  const poId = Number(id);

  const { data: po, isLoading, error } = useQuery({
    queryKey: queryKeys.pos.detail(poId),
    queryFn: () => api.pos.get(poId),
    enabled: !isNaN(poId),
  });

  async function invalidateAll() {
    await queryClient.invalidateQueries({ queryKey: queryKeys.pos.all });
  }

  async function handleApprove() {
    setActionError(null);
    setActionLoading(true);
    try {
      await api.pos.approve(poId);
      await invalidateAll();
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : 'Action failed.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleReject(comment: string) {
    setActionError(null);
    setActionLoading(true);
    try {
      await api.pos.reject(poId, comment);
      setRejectOpen(false);
      await invalidateAll();
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : 'Action failed.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleResubmit() {
    setActionError(null);
    setActionLoading(true);
    try {
      await api.pos.resubmit(poId);
      await invalidateAll();
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : 'Action failed.');
    } finally {
      setActionLoading(false);
    }
  }

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <p className="text-sm text-gray-400">Loading…</p>
      </div>
    );
  }

  if (error || !po) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <p className="text-sm text-red-500">Failed to load purchase order.</p>
      </div>
    );
  }

  const isCreator = currentUser?.id === po.creatorId;
  const isReviewer = currentUser ? ROLE_TO_STATUS[currentUser.role] === po.status : false;
  const canReview = isReviewer && !isCreator;
  const canRework = isCreator && po.status === 'NEEDS_REWORK';

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="border-b border-gray-200 bg-white px-6 py-3">
        <div className="mx-auto flex max-w-4xl items-center gap-2 text-sm">
          <Link href="/dashboard" className="text-gray-500 hover:text-gray-700">
            ← Dashboard
          </Link>
          <span className="text-gray-300">/</span>
          <span className="font-medium text-gray-900 truncate max-w-xs">{po.title}</span>
        </div>
      </header>

      <main className="mx-auto max-w-4xl space-y-6 px-6 py-8">
        {/* PO fields */}
        <section className="rounded-lg border border-gray-200 bg-white p-6">
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0">
              <h1 className="text-xl font-bold text-gray-900">{po.title}</h1>
              {po.description && (
                <p className="mt-1 text-sm text-gray-600 whitespace-pre-wrap">{po.description}</p>
              )}
            </div>
            <div className="shrink-0">
              <StatusBadge status={po.status} />
            </div>
          </div>

          <dl className="mt-6 grid grid-cols-2 gap-x-8 gap-y-4 text-sm sm:grid-cols-3">
            <div>
              <dt className="font-medium text-gray-500">Amount</dt>
              <dd className="mt-0.5 text-gray-900">{formatAmount(po.amount, po.currency)}</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-500">Category</dt>
              <dd className="mt-0.5 text-gray-900">{CATEGORY_LABELS[po.category] ?? po.category}</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-500">Creator</dt>
              <dd className="mt-0.5 text-gray-900">{po.creatorName}</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-500">Created</dt>
              <dd className="mt-0.5 text-gray-900">{formatDate(po.createdAt)}</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-500">Last updated</dt>
              <dd className="mt-0.5 text-gray-900">{formatDate(po.updatedAt)}</dd>
            </div>
          </dl>
        </section>

        {/* Context-aware actions */}
        {(canReview || canRework) && (
          <section className="rounded-lg border border-gray-200 bg-white p-5">
            <h2 className="mb-4 text-sm font-semibold text-gray-700">Actions</h2>
            <div className="flex flex-wrap items-center gap-3">
              {canReview && (
                <>
                  <button
                    onClick={handleApprove}
                    disabled={actionLoading}
                    className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-green-700 disabled:opacity-50"
                  >
                    Approve
                  </button>
                  <button
                    onClick={() => { setActionError(null); setRejectOpen(true); }}
                    disabled={actionLoading}
                    className="rounded-md border border-red-200 bg-white px-4 py-2 text-sm font-medium text-red-600 transition-colors hover:bg-red-50 disabled:opacity-50"
                  >
                    Reject
                  </button>
                </>
              )}
              {canRework && (
                <>
                  <Link
                    href={`/pos/${po.id}/edit`}
                    className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50"
                  >
                    Edit
                  </Link>
                  <button
                    onClick={handleResubmit}
                    disabled={actionLoading}
                    className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-blue-700 disabled:opacity-50"
                  >
                    Resubmit
                  </button>
                </>
              )}
            </div>
            {actionError && (
              <p className="mt-3 text-sm text-red-600">{actionError}</p>
            )}
          </section>
        )}

        {/* Timeline */}
        <section className="rounded-lg border border-gray-200 bg-white p-6">
          <h2 className="mb-5 text-sm font-semibold text-gray-700">Timeline</h2>
          {po.history.length === 0 ? (
            <p className="text-sm text-gray-400">No history yet.</p>
          ) : (
            <ol className="space-y-0">
              {po.history.map((entry, i) => (
                <li key={entry.id} className="flex gap-4">
                  {/* Connector */}
                  <div className="flex flex-col items-center">
                    <div className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full bg-gray-300 ring-2 ring-white" />
                    {i < po.history.length - 1 && (
                      <div className="mt-1 w-px flex-1 bg-gray-200" />
                    )}
                  </div>

                  {/* Content */}
                  <div className="pb-6 min-w-0">
                    <p className="text-sm font-medium text-gray-900">
                      {ACTION_LABELS[entry.action] ?? entry.action}{' '}
                      <span className="font-normal text-gray-500">by {entry.actorName}</span>
                    </p>
                    <p className="mt-0.5 text-xs text-gray-500">
                      {entry.fromStatus ? STATUS_LABELS[entry.fromStatus] : '—'}
                      {' → '}
                      {STATUS_LABELS[entry.toStatus]}
                    </p>
                    {entry.comment && (
                      <p className="mt-2 rounded-md border border-gray-100 bg-gray-50 px-3 py-2 text-sm text-gray-700">
                        {entry.comment}
                      </p>
                    )}
                    <p className="mt-1 text-xs text-gray-400">{formatDate(entry.createdAt)}</p>
                  </div>
                </li>
              ))}
            </ol>
          )}
        </section>
      </main>

      {rejectOpen && (
        <RejectDialog
          onConfirm={handleReject}
          onClose={() => setRejectOpen(false)}
          loading={actionLoading}
        />
      )}
    </div>
  );
}
