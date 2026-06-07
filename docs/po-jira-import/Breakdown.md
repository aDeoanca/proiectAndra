# Purchase Order (PO) Management System

A full-stack web application that automates company procurement: create a Purchase Order, route it through a **conditional approval workflow** (Manager → IT → Finance, with stages skipped based on amount and category), and mark it invoiced. Rejected POs loop back to their creator as *Needs Rework* and restart approval on resubmit.

**Full design:** `../design.md` (+ ADRs in `../adr/`)

## Stack

- **Backend:** Spring Boot 3.3, Java 21, Maven, Spring Data JPA, Flyway, Jakarta Validation.
- **Frontend:** Next.js 15 (App Router), TypeScript, Tailwind + shadcn/ui, TanStack Query, React Hook Form + Zod.
- **Database:** PostgreSQL 16 (Docker Compose).
- **Auth:** minimal — seeded users, cookie session, no JWT.
- **Tests:** JUnit 5 + Testcontainers (backend); light Vitest/RTL (frontend).

## Domain shape

Three entities, append-only audit trail: `users`, `purchase_orders`, `po_history`. Single amount + single category per PO (no line items). Money is `DECIMAL(scale 2)`.

Five states only — a PO is born already submitted, rejection always loops to `NEEDS_REWORK`:

`PENDING_MANAGER_APPROVAL`, `PENDING_IT_VALIDATION`, `PENDING_FINANCE_APPROVAL`, `NEEDS_REWORK`, `INVOICED`.

## Workflow shape

One pure **routing function** drives both initial submit and every resubmit (no separate restart path). Stage order Manager → IT → Finance → Invoiced; each stage applies conditionally:

- Manager — applies when `amount >= 100` (strict `< 100` bypass).
- IT — applies when `category == IT_EQUIPMENT`.
- Finance — always applies (never bypassed).

Editing the amount/category during rework re-gates the workflow. A hand-rolled `PurchaseOrderWorkflowService` enforces role guards, no-self-approval, and writes one `po_history` row per action.

## Story breakdown

1. **Foundations & domain model** — monorepo scaffold, Docker Postgres, JPA entities + enums, Flyway schema, seeded demo users.
2. **Workflow engine** — the pure routing function, the hand-rolled workflow service (submit/approve/reject/resubmit) with guards, and append-only history recording.
3. **Backend API** — PO CRUD + DTOs + validation, workflow action endpoints, queue/filter listing, consistent error handling.
4. **Auth & roles** — minimal cookie-session login over seeded users, current-user resolution, role + self-approval enforcement.
5. **Frontend** — Next.js scaffold + API client, login/switch-user + role-aware dashboard, PO detail with timeline, shared create/edit form.
6. **Testing & ops** — Testcontainers backend test suite (routing matrix + rework loop), light frontend smoke test, dev runbook.

Total: **1 Epic + 6 Stories + 18 Subtasks**.

## References

- Design doc: `../design.md`
- ADRs: `../adr/0001`–`../adr/0005`
- Original requirements: `../req.txt`
