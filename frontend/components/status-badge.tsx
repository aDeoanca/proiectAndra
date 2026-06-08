import type { Status } from '@/lib/types';

const CONFIG: Record<Status, { label: string; cls: string }> = {
  PENDING_MANAGER_APPROVAL: { label: 'Pending Manager', cls: 'bg-yellow-100 text-yellow-800' },
  PENDING_IT_VALIDATION:    { label: 'Pending IT',      cls: 'bg-blue-100 text-blue-800' },
  PENDING_FINANCE_APPROVAL: { label: 'Pending Finance', cls: 'bg-purple-100 text-purple-800' },
  NEEDS_REWORK:             { label: 'Needs Rework',    cls: 'bg-red-100 text-red-800' },
  INVOICED:                 { label: 'Invoiced',        cls: 'bg-green-100 text-green-800' },
};

export function StatusBadge({ status }: { status: Status }) {
  const { label, cls } = CONFIG[status];
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${cls}`}>
      {label}
    </span>
  );
}
