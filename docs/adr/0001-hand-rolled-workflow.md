# ADR-0001: Hand-rolled workflow service, not a state-machine library

**Status:** Accepted — 2026-06-07

## Context

The approval workflow has 5 states, mostly linear with two conditional skips, driven by a
pure routing function rather than a complex event graph. Choice: a state-machine library
(e.g. Spring State Machine) vs. a hand-rolled service.

## Decision

Hand-roll `PurchaseOrderWorkflowService`. Transitions are conditional *computations*
(skip-if-amount, skip-if-category) plus role guards — not declarative transition tables.

## Consequences

- **+** Simpler, more testable (routing is a pure function), far more AI-navigable.
- **+** No persistence-adapter/listener ceremony around ~30 lines of logic.
- **−** If the process later grows into a branching BPMN-style graph, revisit and adopt a
  library then (YAGNI until then).
