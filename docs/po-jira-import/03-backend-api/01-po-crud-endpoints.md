# PO CRUD endpoints, DTOs, and validation

## What

- `POST /api/pos` — create = submit. Request DTO `{ title, description?, amount, category }`; routes via the workflow service and returns the created PO with its computed status.
- `GET /api/pos/{id}` — detail DTO including the full `history[]` (from POM-10).
- `PATCH /api/pos/{id}` — edit `{ title?, description?, amount?, category? }`; **allowed only when status is `NEEDS_REWORK` and caller is the creator** (enforced via the workflow guard).
- Response/request DTOs (not entities) with MapStruct or hand mappers; never leak JPA entities over the wire.
- Jakarta Bean Validation on request DTOs: `title` not blank, `amount` `@Positive` with scale 2, `category` a valid enum, `description` optional.

## Notes

- Create does not accept a `status` — the server computes it. Clients can never set state directly.
- Edit re-validates the same rules as create.
- Depends on **POM-9** (workflow service) and **POM-18** (current-user resolution).

## Acceptance

- Creating a sub-$100 non-IT PO returns it already in `PENDING_FINANCE_APPROVAL`.
- `PATCH` on a PO that is not `NEEDS_REWORK` → `409`; by a non-creator → `403`.
- `amount = 0` or blank `title` → `400` with field-level details.
- Detail response includes the chronological history array.
