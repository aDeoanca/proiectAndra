'use client';

import { useState } from 'react';

interface RejectDialogProps {
  onConfirm: (comment: string) => void;
  onClose: () => void;
  loading: boolean;
}

export function RejectDialog({ onConfirm, onClose, loading }: RejectDialogProps) {
  const [comment, setComment] = useState('');
  const trimmed = comment.trim();

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onClick={(e) => e.target === e.currentTarget && !loading && onClose()}
    >
      <div className="w-full max-w-md rounded-xl border border-gray-200 bg-white p-6 shadow-xl">
        <h2 className="text-base font-semibold text-gray-900">Reject purchase order</h2>
        <p className="mt-1 text-sm text-gray-500">
          A non-empty comment is required to reject.
        </p>

        <textarea
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Enter rejection reason…"
          rows={4}
          autoFocus
          className="mt-4 w-full resize-none rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />

        <div className="mt-4 flex justify-end gap-3">
          <button
            onClick={onClose}
            disabled={loading}
            className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(trimmed)}
            disabled={!trimmed || loading}
            className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-700 disabled:opacity-50"
          >
            {loading ? 'Rejecting…' : 'Reject'}
          </button>
        </div>
      </div>
    </div>
  );
}
