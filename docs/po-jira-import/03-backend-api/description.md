# Backend API

Expose the workflow over a REST API: PO CRUD with DTOs and validation, explicit workflow action endpoints, queue/filter listing for the dashboards, and a consistent error response shape. The API is the only thing the frontend talks to; it owns all validation and never lets a client construct an illegal transition.

Reference: `../design.md` §9–§10. Action verbs over generic status PATCH — see ADR-0002.

## Why this is a separate story

It composes the engine (Story 2) into HTTP endpoints and stabilises the contract the frontend (Story 5) builds against. Keeping it separate lets the API surface and DTOs be reviewed and frozen before any UI work starts.

## Acceptance

- All endpoints in `../design.md` §9 exist with the documented request/response shapes.
- Workflow transitions are exposed as explicit verbs (`/approve`, `/reject`, `/resubmit`), never a client-set status.
- Bean Validation rejects bad input (missing title, `amount <= 0`, unknown category) with `400` and field details.
- `PATCH /api/pos/{id}` succeeds only when the PO is `NEEDS_REWORK` and the caller is the creator; otherwise `409`/`403`.
- Errors use the shape `{ "error": { "code", "message", "details?" } }` with correct HTTP status (`409` illegal transition, `403` guard violation, `404` not found, `400` validation).
- Depends on Story **POM-02** (workflow engine) and **POM-04** (auth supplies the current user).
