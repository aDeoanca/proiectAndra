# Auth & roles

Minimal, demo-grade authentication: a login that picks a seeded user, an HTTP-only session cookie, current-user resolution on every request, and enforcement hooks for role + self-approval rules. No JWT, no passwords — see ADR-0003.

Reference: `../design.md` §11.

## Why this is a separate story

Auth cuts across the API (Story 3 needs "who is the caller") and the frontend (Story 5 needs login + switch-user). Isolating the session mechanism and current-user plumbing keeps that cross-cutting concern in one place and unblocks both.

## Acceptance

- `POST /api/auth/login` with a seeded `userId` establishes a session cookie; `GET /api/auth/me` returns the user; `POST /api/auth/logout` clears it.
- Protected `/api/pos/**` endpoints return `401` without a valid session.
- The current user is resolvable in controllers/services (the actor passed to the workflow service).
- Any authenticated user can create a PO; reviewing is gated by role (enforced in the workflow service, supplied here).
- Depends on Story **POM-01** (seeded users) and feeds Story **POM-03** (API) and **POM-05** (frontend).
