# Testing & ops

The safety net and the on-ramp: an exhaustive backend test suite (the routing matrix and the full rework loop, on Testcontainers Postgres), a light frontend smoke test, and a dev runbook so anyone can stand the system up. Backend-heavy, frontend-light — see ADR-0004.

Reference: `../design.md` §13–§14.

## Why this is a separate story

Testing depth and the runbook are best landed once the surfaces they cover are stable, and they shouldn't block feature stories. Concentrating them here keeps the test strategy coherent (one Testcontainers setup, one runbook) rather than scattered.

## Acceptance

- Routing function is tested across every bypass combination plus the `100.00` boundary.
- Workflow guards are covered: wrong role, self-approval, reject-without-comment, illegal transitions, resubmit re-routing.
- An integration test drives the full reject → rework → resubmit loop end-to-end against Testcontainers Postgres (exercising Flyway).
- The frontend has at least one smoke test on the create form's Zod validation.
- A runbook documents install prerequisites and the up/run/test commands; a fresh clone can be brought up by following it.
- Depends on all prior stories (it verifies them).
