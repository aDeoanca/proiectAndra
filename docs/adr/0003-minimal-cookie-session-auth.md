# ADR-0003: Minimal cookie-session auth with seeded users

**Status:** Accepted — 2026-06-07

## Context

The spec needs role-based privileges (manager, IT, finance) but is not an auth exercise.
Options ranged from full JWT/OAuth/RBAC to a minimal demo-grade scheme.

## Decision

- Seeded demo users, one `role` each (`CREATOR`, `MANAGER`, `IT_REP`, `FINANCE`).
- Login = pick a seeded user → backend sets an HTTP-only **session cookie**. No JWT, no
  passwords (or a trivial shared one).
- **Anyone authenticated can create** a PO; role gates reviewing only.
- No-self-approval guard: a reviewer cannot act on a PO they created.

## Consequences

- **+** Trivial to demo the full workflow via a "switch user" affordance.
- **+** Guards stay simple (single role per user).
- **−** Not production auth. Real auth (passwords/JWT/RBAC, multi-role users) is explicit
  future work — see `docs/design.md` §15.
