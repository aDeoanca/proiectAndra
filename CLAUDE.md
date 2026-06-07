# CLAUDE.md — Purchase Order (PO) Management System

Full-stack PO management app: create → conditional approval → invoice, with a
reject/rework loop. This file is the operating manual for working in this repo. The
authoritative design is [`docs/design.md`](./docs/design.md); decisions are recorded in
[`docs/adr/`](./docs/adr).

## Stack

- **Backend:** Spring Boot 3.3, Java 21, **Maven**, Spring Data JPA, Flyway, Jakarta Validation, Lombok/MapStruct.
- **Frontend:** Next.js 15 (App Router), TypeScript, Tailwind + shadcn/ui, TanStack Query, React Hook Form + Zod.
- **Database:** PostgreSQL 16 (Docker Compose).
- **Auth:** minimal — seeded users, cookie session, **no JWT**.
- **Tests:** JUnit 5 + Testcontainers (backend); light Vitest/RTL (frontend).

## Repo Layout

```
backend/    Spring Boot app (Maven)
frontend/   Next.js app
docs/       design.md, adr/, req.txt
docker-compose.yml
```

## Commands

> Code is not scaffolded yet — these are the intended commands once `backend/` and `frontend/` exist.

| Task | Command |
|------|---------|
| Start Postgres | `docker-compose up -d` |
| Run backend | `cd backend && ./mvnw spring-boot:run` |
| Test backend | `cd backend && ./mvnw test` |
| Run frontend | `cd frontend && npm run dev` |
| Test frontend | `cd frontend && npm test` |

## Load-Bearing Invariants

These encode the spec. **Do not violate them; change them only by updating `docs/design.md` and the relevant ADR first.**

1. **Five states only:** `PENDING_MANAGER_APPROVAL`, `PENDING_IT_VALIDATION`,
   `PENDING_FINANCE_APPROVAL`, `NEEDS_REWORK`, `INVOICED`. No `DRAFT`, no terminal `REJECTED`.
2. **One routing function drives all transitions.** Submit and resubmit share the same
   code path. Routing reads the PO's *current* amount/category, so edits during rework
   re-gate the workflow. Stage order: Manager → IT → Finance → Invoiced.
3. **Manager bypass is strict `amount < 100`.** Exactly `100.00` requires manager approval.
4. **IT validation only when `category == IT_EQUIPMENT`.**
5. **Finance approval is never bypassed.**
6. **No self-approval:** a reviewer cannot act on a PO they created.
7. **Role gates reviewing only; anyone can create.** Single role per user.
8. **A PO is immutable once submitted, except in `NEEDS_REWORK`,** where only the creator
   may edit it (then resubmit).
9. **Reject requires a non-empty comment.**
10. **`po_history` is append-only** — it is the audit trail and the UI timeline. Every
    workflow action writes a row.
11. **Money is `DECIMAL(scale 2)`** — never floating point.
12. **Workflow logic is hand-rolled** (`PurchaseOrderWorkflowService`), not a state-machine
    library. The frontend never computes transitions — it renders only the actions the API
    reports as legal.

## Conventions

- Backend owns all business rules; frontend is a thin client.
- Workflow actions are **explicit verb endpoints** (`/approve`, `/reject`, `/resubmit`),
  never a generic status PATCH.
- Errors return `{ "error": { "code", "message", "details?" } }` with correct HTTP status
  (e.g. `409` for illegal transitions, `403` for role/guard violations).
- Schema changes go through **Flyway migrations**, never `ddl-auto: update` in prod config.

## Working Agreements

- **Do not `git commit`** unless explicitly asked — the user controls commits. Staging and
  `.gitignore` are fine.
- When a request conflicts with an invariant above, surface the conflict and update the
  design doc/ADR before coding.
