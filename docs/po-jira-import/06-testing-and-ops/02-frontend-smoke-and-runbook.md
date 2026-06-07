# Frontend smoke test and dev runbook

## What

- **Frontend smoke test** (Vitest + React Testing Library) — at minimum, the create form's Zod validation: blank title and `amount <= 0` are blocked; a valid payload passes. Optionally one render test of the dashboard's two sections with mocked queries.
- **Dev runbook** — confirm/extend the root `README.md`: prerequisites (JDK 21, Maven, Node 20, Docker), and the canonical up/run/test sequence (`docker-compose up -d` → backend → frontend), plus the seeded demo-user table and a "walk a PO through the workflow" demo script.
- A short troubleshooting note (CORS/credentials, Testcontainers needs Docker, Flyway baseline on fresh DB).

## Notes

- Frontend testing is deliberately light per ADR-0004 — don't build out heavy component/E2E coverage.
- The runbook should let a fresh clone reach a working app by copy-paste.
- Depends on **POM-05** (frontend) and **POM-01-01** (compose/runbook surface).

## Acceptance

- The frontend smoke test passes under `npm test`.
- Following the runbook from a clean clone brings up Postgres + backend + frontend and lets you log in and create a PO.
- The demo script successfully drives one PO through reject → rework → resubmit → approve → invoiced.
