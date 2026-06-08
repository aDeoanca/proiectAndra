# Frontend

The Next.js client: a thin, role-aware UI over the backend API. Four page-types — login/switch-user, a role-aware dashboard, PO detail with a status timeline, and a shared create/edit form. Functional styling (badges + timeline), not gold-plated. The UI renders only the actions the API reports as legal; it never computes transitions.

Reference: `../design.md` §12.

## Why this is a separate story

It's the full client tier and depends on a stable API (Story 3) and auth (Story 4). Splitting it from the backend lets the UI be built and reviewed against the frozen contract, and keeps frontend tooling (Next.js, TanStack Query, shadcn) in one story.

## Acceptance

- A user can log in by picking a seeded user and switch users to drive the whole workflow.
- The dashboard shows "Awaiting my action" (role queue) and "My purchase orders" (created by me).
- PO detail shows all fields, a chronological timeline from `po_history`, and context-aware action buttons.
- A creator can create a PO and, when it is `NEEDS_REWORK`, edit and resubmit it via the shared form.
- Server state goes through TanStack Query; forms validate with Zod before submitting.
- Depends on Story **POM-11** (API) and **POM-16** (auth).
