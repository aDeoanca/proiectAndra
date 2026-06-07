# Foundations & domain model

Land the scaffolding and type-system pieces every other story depends on: the monorepo layout, a Dockerised Postgres 16, the three JPA entities (`User`, `PurchaseOrder`, `PoHistory`) with their enums, the Flyway baseline migration, and seeded demo users.

Identity uses the **single-document model** — one `purchase_orders` row per PO with a single amount + category (no line items), one append-only `po_history` row per workflow action. Reference: `../design.md` §4–§5 and ADR-0005.

## Why this is a separate story

These are pure additions with no upstream dependencies. Once the schema, entities, and seed data are in, the workflow engine (Story 2), API (Story 3), and auth (Story 4) can all be picked up in parallel without merge collisions.

## Acceptance

- `docker-compose up -d` starts Postgres 16 on `localhost:5432`; the backend connects on startup.
- Flyway applies the baseline migration cleanly on a fresh database; the schema matches the ER model in `../design.md` §5.
- The three entities map correctly (a JPA bootstrap/`@DataJpaTest` smoke test persists and reads back one row of each).
- `Status`, `Category`, and `Role` enums exist with exactly the values in the design (`Status`: 5 values, no `DRAFT`/`REJECTED`).
- Money columns are `DECIMAL` with scale 2 — never floating point.
- Seed migration inserts one demo user per role (`alice`/CREATOR, `bob`/MANAGER, `carol`/IT_REP, `dave`/FINANCE).
