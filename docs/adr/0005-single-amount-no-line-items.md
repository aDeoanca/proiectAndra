# ADR-0005: Single amount + category, no line items

**Status:** Accepted — 2026-06-07

## Context

Real procurement POs have line items (multiple products, each qty/price, summing to a
total). The spec models a PO with a single "requested amount" and a single "category", and
the routing rules key off those single values.

## Decision

Model the PO with one `amount` and one `category` (plus `currency`, `USD` only). No line
items.

## Consequences

- **+** Matches the spec exactly; routing reads two scalar fields.
- **+** Smallest correct data model.
- **−** Adding line items later means deriving `amount`/`category` from the lines (or
  picking a dominant category). Noted as future work in `docs/design.md` §15.
