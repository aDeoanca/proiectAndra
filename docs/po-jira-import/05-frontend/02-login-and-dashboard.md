# Login, switch-user, and role-aware dashboard

## What

- **Login page** — fetches `GET /api/users` and renders a seeded-user picker; selecting one calls `POST /api/auth/login` and lands on the dashboard. A persistent "switch user" affordance (header menu) re-logs as another seeded user for easy demoing.
- **Dashboard** — two sections:
  - *Awaiting my action* — `GET /api/pos?queue=me`, shown only when the role has a queue (manager/IT/finance). Each row links to detail.
  - *My purchase orders* — `GET /api/pos?creator=me`, with status badges; highlights `NEEDS_REWORK` items needing edits.
- Status rendered as coloured **badges**; a "New PO" button routes to the create form.

## Notes

- The two sections are driven entirely by the backend filters (POM-14) — no client-side state derivation.
- A pure CREATOR sees only "My purchase orders"; reviewers see both.
- Depends on **POM-20** (client/auth) and **POM-14** (queue listing).

## Acceptance

- Logging in as `bob` (MANAGER) shows a populated "Awaiting my action" queue; logging in as `alice` (CREATOR) shows only her POs.
- Switch-user swaps the session and refreshes both lists.
- Status badges reflect the current status for each PO.
