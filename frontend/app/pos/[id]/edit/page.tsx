'use client';

import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { queryKeys } from '@/lib/query-keys';
import { useAuth } from '@/providers/auth-provider';
import { PoForm } from '@/components/po-form';
import type { CreatePoFormValues } from '@/lib/schemas';

export default function EditPoPage() {
  const { id } = useParams<{ id: string }>();
  const { currentUser } = useAuth();
  const router = useRouter();
  const queryClient = useQueryClient();

  const poId = Number(id);

  const { data: po, isLoading, error } = useQuery({
    queryKey: queryKeys.pos.detail(poId),
    queryFn: () => api.pos.get(poId),
    enabled: !isNaN(poId),
  });

  async function handleEdit(data: CreatePoFormValues) {
    await api.pos.update(poId, data);
    queryClient.invalidateQueries({ queryKey: queryKeys.pos.all });
    router.push(`/pos/${poId}`);
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

  const canEdit = currentUser?.id === po.creatorId && po.status === 'NEEDS_REWORK';

  if (!canEdit) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-3 bg-gray-50">
        <p className="text-sm text-gray-600">This purchase order cannot be edited.</p>
        <Link href={`/pos/${poId}`} className="text-sm text-blue-600 hover:underline">
          Back to PO
        </Link>
      </div>
    );
  }

  const defaultValues: Partial<CreatePoFormValues> = {
    title: po.title,
    description: po.description ?? '',
    amount: po.amount,
    category: po.category,
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white px-6 py-3">
        <div className="mx-auto flex max-w-2xl items-center gap-2 text-sm">
          <Link href={`/pos/${poId}`} className="text-gray-500 hover:text-gray-700">
            ← {po.title}
          </Link>
          <span className="text-gray-300">/</span>
          <span className="font-medium text-gray-900">Edit</span>
        </div>
      </header>

      <main className="mx-auto max-w-2xl px-6 py-8">
        <div className="rounded-lg border border-gray-200 bg-white p-6">
          <h1 className="mb-1 text-lg font-bold text-gray-900">Edit Purchase Order</h1>
          <p className="mb-6 text-sm text-gray-500">
            Save your changes, then resubmit from the detail page.
          </p>
          <PoForm
            onSubmit={handleEdit}
            submitLabel="Save changes"
            defaultValues={defaultValues}
          />
        </div>
      </main>
    </div>
  );
}
