# Pure routing function

## What

- A stateless `WorkflowRouter` (no Spring dependencies) exposing `nextStateAfter(PurchaseOrder po, Stage completedStage) -> Status`.
- Define the ordered stage list: `MANAGER` (applies when `amount >= 100`) → `IT` (applies when `category == IT_EQUIPMENT`) → `FINANCE` (always) → done (`INVOICED`).
- `nextStateAfter` walks stages *after* `completedStage` in order and returns the first applicable stage's pending state, or `INVOICED` if none remain.
- Helper `entryState(po)` = `nextStateAfter(po, completedStage = NONE)` — the state a PO enters on submit/resubmit.
- A helper mapping a `PENDING_*` status back to the `Stage` that owns it (used by `approve`).

## Notes

- Strict boundary: `amount < 100` bypasses Manager; exactly `100.00` requires it. Use `BigDecimal.compareTo`, not `equals`.
- Pure and side-effect free — this is the unit-test crown jewel (full matrix tested in Story 6).
- Depends on **POM-01-02** (enums).

## Acceptance

- Entry-state truth table holds: `<100`+non-IT → `PENDING_FINANCE_APPROVAL`; `<100`+IT → `PENDING_IT_VALIDATION`; `>=100`+non-IT → `PENDING_MANAGER_APPROVAL`; `>=100`+IT → `PENDING_MANAGER_APPROVAL`.
- Boundary at exactly `100.00` routes to `PENDING_MANAGER_APPROVAL`.
- `approve` progression: Manager → (IT if IT-Equipment else Finance); IT → Finance; Finance → `INVOICED`.
