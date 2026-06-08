'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { queryKeys } from '@/lib/query-keys';
import { PoForm } from '@/components/po-form';
import type { CreatePoFormValues } from '@/lib/schemas';

export default function NewPoPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  async function handleCreate(data: CreatePoFormValues) {
    const po = await api.pos.create(data);
    queryClient.invalidateQueries({ queryKey: queryKeys.pos.all });
    router.push(`/pos/${po.id}`);
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white px-6 py-3">
        <div className="mx-auto flex max-w-2xl items-center gap-2 text-sm">
          <Link href="/dashboard" className="text-gray-500 hover:text-gray-700">
            ← Dashboard
          </Link>
          <span className="text-gray-300">/</span>
          <span className="font-medium text-gray-900">New Purchase Order</span>
        </div>
      </header>

      <main className="mx-auto max-w-2xl px-6 py-8">
        <div className="rounded-lg border border-gray-200 bg-white p-6">
          <h1 className="mb-6 text-lg font-bold text-gray-900">New Purchase Order</h1>
          <PoForm onSubmit={handleCreate} submitLabel="Create PO" />
        </div>
      </main>
    </div>
  );
}
