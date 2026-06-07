# Workflow engine

The heart of the system: a single pure **routing function** plus a hand-rolled `PurchaseOrderWorkflowService` that enforces transitions, role guards, and no-self-approval, and writes one append-only `po_history` row per action. No state-machine library — see ADR-0001.

Reference: `../design.md` §6–§8.

## Why this is a separate story

This is the load-bearing business logic and the highest test-value code in the project. Isolating it keeps the routing rules and guards in pure, framework-light classes that are exhaustively unit-testable independent of the web/API layer (Story 3) and persistence wiring.

## Acceptance

- One routing function drives both initial submit and resubmit — no separate restart code path.
- Stage order Manager → IT → Finance → Invoiced; Manager applies only when `amount >= 100` (strict `< 100` bypass), IT only when `category == IT_EQUIPMENT`, Finance always.
- Editing amount/category before resubmit re-gates the workflow (recomputed from current values).
- Guards enforced: only the role owning the current state may act; a reviewer cannot act on a PO they created; reject requires a non-empty comment; illegal transitions are rejected.
- Every `submit`/`approve`/`reject`/`resubmit` writes exactly one `po_history` row capturing `fromStatus`, `toStatus`, `action`, `actor`, and `comment`.
- Depends on Story **POM-01** (entities, enums, persistence).
