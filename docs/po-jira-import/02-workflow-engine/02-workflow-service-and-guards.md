# PurchaseOrderWorkflowService and guards

## What

- `PurchaseOrderWorkflowService` with four methods, each taking the acting `User`:
  - `submit(po, actor)` — set entry state from the router; actor is the creator.
  - `approve(po, actor)` — validate the actor's role owns `po.status`, advance via the router.
  - `reject(po, actor, comment)` — validate role + non-empty comment, set `NEEDS_REWORK`.
  - `resubmit(po, actor)` — only from `NEEDS_REWORK`, only by the creator, re-enter routing from the start.
- Guards:
  - **Role match** — `actor.role` must own the current `PENDING_*` state (Manager→manager state, etc.).
  - **No self-approval** — a reviewer cannot act on a PO whose `creator == actor`.
  - **Legal transition** — actions on a terminal/invalid state throw a typed `IllegalTransitionException` (→ HTTP 409 in Story 3).
  - **Reject comment** — required, non-blank.
- Throw typed exceptions (`ForbiddenActionException`, `IllegalTransitionException`, `ValidationException`) — no HTTP coupling here; the API layer maps them.

## Notes

- Service mutates the `PurchaseOrder` and delegates history writes to the recorder (POM-02-03).
- The frontend never computes transitions — this service is the single source of truth.
- Depends on **POM-02-01** (router).

## Acceptance

- Wrong-role actor on a pending PO → `ForbiddenActionException`.
- Creator approving/rejecting their own PO → `ForbiddenActionException`.
- `reject` with blank comment → `ValidationException`.
- `approve` on an `INVOICED` PO → `IllegalTransitionException`.
- `resubmit` from any non-`NEEDS_REWORK` state → `IllegalTransitionException`.
