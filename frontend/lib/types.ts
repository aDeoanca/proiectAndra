export type Role = 'CREATOR' | 'MANAGER' | 'IT_REP' | 'FINANCE';

export type Status =
  | 'PENDING_MANAGER_APPROVAL'
  | 'PENDING_IT_VALIDATION'
  | 'PENDING_FINANCE_APPROVAL'
  | 'NEEDS_REWORK'
  | 'INVOICED';

export type Category = 'SERVICES' | 'OFFICE_SUPPLIES' | 'IT_EQUIPMENT';

export type HistoryAction = 'SUBMIT' | 'APPROVE' | 'REJECT' | 'RESUBMIT';

export interface User {
  id: number;
  name: string;
  role: Role;
}

export interface PoSummary {
  id: number;
  title: string;
  amount: number;
  category: Category;
  status: Status;
  creatorId: number;
  creatorName: string;
  updatedAt: string;
}

export interface PoHistory {
  id: number;
  action: HistoryAction;
  fromStatus: Status | null;
  toStatus: Status;
  comment: string | null;
  actorId: number;
  actorName: string;
  createdAt: string;
}

export interface PoDetail extends PoSummary {
  description: string | null;
  currency: string;
  createdAt: string;
  history: PoHistory[];
}

export interface CreatePoRequest {
  title: string;
  description?: string;
  amount: number;
  category: Category;
}

export interface UpdatePoRequest {
  title?: string;
  description?: string;
  amount?: number;
  category?: Category;
}
