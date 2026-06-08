# Backend test suite (Testcontainers)

## What

- **Routing unit tests** (pure, no Spring) — a parameterised matrix over `{amount <100, =100, >100} × {IT_EQUIPMENT, non-IT}` asserting the entry state, plus the per-stage `approve` progression.
- **Workflow service unit tests** — wrong-role rejection, no-self-approval, reject-requires-comment, illegal transitions (approve INVOICED, resubmit non-rework), and that history rows are written.
- **Integration tests** (`@SpringBootTest`) on **Testcontainers Postgres** — happy path for each routing variant, and the full **reject → rework → edit → resubmit** loop, asserting status + history at each step. Flyway runs against the container.
- A reusable Testcontainers base class / `@ServiceConnection` Postgres setup.

## Notes

- Testcontainers needs a Docker daemon in dev/CI (ADR-0004).
- Use `BigDecimal` literals in the boundary tests to lock the strict `< 100` rule.
- Depends on Stories **POM-7** and **POM-11**.

## Acceptance

- The routing matrix test enumerates all combinations and passes, including `100.00 → PENDING_MANAGER_APPROVAL`.
- The rework-loop integration test passes end-to-end on Testcontainers Postgres.
- Guard violations assert the correct typed exceptions / HTTP statuses.
- `./mvnw test` is green from a clean checkout (with Docker available).
