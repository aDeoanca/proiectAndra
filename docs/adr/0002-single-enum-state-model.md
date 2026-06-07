# ADR-0002: Single-enum state model with explicit verb endpoints

**Status:** Accepted — 2026-06-07

## Context

The PO lifecycle could be modeled as a single status enum, or as separate stage/decision
fields with derived status. Transitions could be exposed as a generic status PATCH or as
explicit action endpoints.

## Decision

- One `status` enum on the PO with exactly 5 values:
  `PENDING_MANAGER_APPROVAL`, `PENDING_IT_VALIDATION`, `PENDING_FINANCE_APPROVAL`,
  `NEEDS_REWORK`, `INVOICED`. No `DRAFT`, no terminal `REJECTED`.
- Transitions exposed as explicit verbs: `/approve`, `/reject`, `/resubmit`.

## Consequences

- **+** Status maps 1:1 to the spec's stages; trivial to render and reason about.
- **+** Conditional steps are transition rules, not extra states.
- **+** Verb endpoints make illegal client-constructed transitions impossible and map 1:1
  to the workflow service.
- **−** Derived/computed status views (if ever needed) must be built on top of the enum.
