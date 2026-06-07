# Queue and filter listing

## What

- `GET /api/pos` with query filters powering the dashboard:
  - `?queue=me` — POs awaiting the current user's role's action (e.g. a manager sees `PENDING_MANAGER_APPROVAL`, excluding ones they created).
  - `?creator=me` — POs created by the current user (their "My POs" list).
  - `?status=` — filter by an explicit status.
- Maps the caller's `Role` to the `PENDING_*` status its queue owns.
- Returns a lightweight list DTO (id, title, amount, category, status, creator, updatedAt) — not the full history.
- Sensible default ordering (e.g. `updatedAt` desc) and a reasonable result cap.

## Notes

- `queue=me` must exclude POs the caller created (mirrors the no-self-approval rule) so reviewers don't see their own items in their action queue.
- Implement with Spring Data derived queries or a small `Specification`; no need for full pagination at this scope (note the cap if one is applied).
- Depends on **POM-04-02** (current-user) and **POM-01-02** (entities/repos).

## Acceptance

- A manager's `?queue=me` returns only `PENDING_MANAGER_APPROVAL` POs they did not create.
- `?creator=me` returns exactly the caller's POs across all statuses.
- `?status=NEEDS_REWORK` returns only reworkable POs.
