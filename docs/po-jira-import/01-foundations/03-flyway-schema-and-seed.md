# Flyway baseline migration and seed data

## What

- `V1__baseline.sql` — `users`, `purchase_orders`, `po_history` tables matching the entities, with FKs (`purchase_orders.creator_id → users.id`, `po_history.po_id → purchase_orders.id`, `po_history.actor_id → users.id`), NOT NULL / CHECK constraints (`amount > 0`), and indexes on `purchase_orders.status` and `po_history.po_id`.
- `V2__seed_demo_users.sql` — insert the four demo users (one per role).
- Optional `V3__seed_example_pos.sql` — a few example POs spanning each routing path (sub-$100 non-IT, sub-$100 IT, ≥$100 non-IT, ≥$100 IT) for a populated demo.

## Notes

- `amount` column `NUMERIC(12,2)` with `CHECK (amount > 0)`.
- `currency` defaults to `'USD'`.
- Keep enums as `VARCHAR` columns (matching `@Enumerated(STRING)`), not Postgres enum types, to avoid migration friction.
- Depends on **POM-01-02** (entities define the shape the migration must match).

## Acceptance

- Flyway migrates a fresh DB with no errors; `ddl-auto: validate` passes against the result.
- The four demo users exist after startup.
- The `amount > 0` CHECK constraint rejects a zero/negative insert.
