'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createPoSchema, type CreatePoFormValues } from '@/lib/schemas';
import { ApiError } from '@/lib/api';

const CATEGORY_OPTIONS = [
  { value: 'SERVICES', label: 'Services' },
  { value: 'OFFICE_SUPPLIES', label: 'Office Supplies' },
  { value: 'IT_EQUIPMENT', label: 'IT Equipment' },
] as const;

interface PoFormProps {
  defaultValues?: Partial<CreatePoFormValues>;
  onSubmit: (data: CreatePoFormValues) => Promise<void>;
  submitLabel: string;
}

const inputCls =
  'mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500';

export function PoForm({ defaultValues, onSubmit, submitLabel }: PoFormProps) {
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<CreatePoFormValues>({
    resolver: zodResolver(createPoSchema),
    defaultValues: {
      title: '',
      description: '',
      ...defaultValues,
    },
  });

  async function onValid(data: CreatePoFormValues) {
    try {
      await onSubmit(data);
    } catch (e) {
      if (e instanceof ApiError && e.details) {
        for (const [field, message] of Object.entries(e.details)) {
          setError(field as keyof CreatePoFormValues, { message });
        }
      } else {
        setError('root', {
          message: e instanceof ApiError ? e.message : 'Submit failed. Please try again.',
        });
      }
    }
  }

  return (
    <form onSubmit={handleSubmit(onValid)} noValidate className="space-y-5">
      {/* Title */}
      <div>
        <label htmlFor="title" className="block text-sm font-medium text-gray-700">
          Title
        </label>
        <input
          id="title"
          type="text"
          placeholder="Brief description of the purchase"
          {...register('title')}
          className={inputCls}
        />
        {errors.title && (
          <p className="mt-1 text-xs text-red-600">{errors.title.message}</p>
        )}
      </div>

      {/* Description */}
      <div>
        <label htmlFor="description" className="block text-sm font-medium text-gray-700">
          Description{' '}
          <span className="font-normal text-gray-400">(optional)</span>
        </label>
        <textarea
          id="description"
          rows={3}
          placeholder="Additional details…"
          {...register('description')}
          className={`${inputCls} resize-none`}
        />
        {errors.description && (
          <p className="mt-1 text-xs text-red-600">{errors.description.message}</p>
        )}
      </div>

      {/* Amount */}
      <div>
        <label htmlFor="amount" className="block text-sm font-medium text-gray-700">
          Amount
        </label>
        <input
          id="amount"
          type="number"
          step="0.01"
          min="0.01"
          placeholder="0.00"
          {...register('amount', { valueAsNumber: true })}
          className={inputCls}
        />
        {errors.amount && (
          <p className="mt-1 text-xs text-red-600">{errors.amount.message}</p>
        )}
      </div>

      {/* Category */}
      <div>
        <label htmlFor="category" className="block text-sm font-medium text-gray-700">
          Category
        </label>
        <select
          id="category"
          {...register('category')}
          className={inputCls}
          defaultValue={defaultValues?.category ?? ''}
        >
          <option value="" disabled>
            Select a category
          </option>
          {CATEGORY_OPTIONS.map(({ value, label }) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
        {errors.category && (
          <p className="mt-1 text-xs text-red-600">{errors.category.message}</p>
        )}
      </div>

      {/* Root / server error */}
      {errors.root && (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {errors.root.message}
        </p>
      )}

      <button
        type="submit"
        disabled={isSubmitting}
        className="w-full rounded-md bg-blue-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-blue-700 disabled:opacity-50"
      >
        {isSubmitting ? 'Saving…' : submitLabel}
      </button>
    </form>
  );
}
