# Workflow action endpoints

## What

- `POST /api/pos/{id}/approve` — reviewer advances the PO; no body (optional `{ comment }`).
- `POST /api/pos/{id}/reject` — body `{ comment }` (required); sets `NEEDS_REWORK`.
- `POST /api/pos/{id}/resubmit` — creator re-enters routing after editing.
- Each endpoint resolves the current user (Story 4), loads the PO, and delegates to the matching `PurchaseOrderWorkflowService` method. The typed workflow exceptions map to HTTP status via the global handler (POM-03-04).
- Responses return the updated PO DTO (new status + appended history).

## Notes

- These are the only ways status changes — there is no `PATCH status`. See ADR-0002.
- Authorisation is the workflow service's job (role match, no self-approval); the controller just passes the actor.
- Depends on **POM-02-02** (service) and **POM-03-04** (exception → HTTP mapping).

## Acceptance

- Manager approving a `PENDING_MANAGER_APPROVAL` PO advances it to the next applicable state.
- Reject with no comment → `400`; reject by the wrong role → `403`.
- Approve on an already-`INVOICED` PO → `409`.
- Resubmit by the creator from `NEEDS_REWORK` re-routes from the start (re-evaluating bypasses against edited values).
