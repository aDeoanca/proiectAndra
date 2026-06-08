# Shared create / edit form

## What

- One React Hook Form + Zod form component reused for **create** and **rework edit**.
- Fields: `title` (required), `description` (optional), `amount` (positive, 2-decimal), `category` (select from the enum).
- Create mode → `POST /api/pos`; edit mode (only reachable for a `NEEDS_REWORK` PO owned by the user) → `PATCH /api/pos/{id}`, then offer **Resubmit**.
- Zod schema mirrors the backend validation; server-side validation errors (`400` with `details`) are mapped back onto the matching form fields.
- On success, navigate to the new/updated PO's detail page.

## Notes

- The same Zod schema module from POM-20 is the single source of client validation truth.
- Edit pre-fills from the loaded PO; the form does not expose `status` (server-owned).
- Depends on **POM-20** (client/schema) and **POM-12** (create/edit endpoints).

## Acceptance

- Submitting a valid new PO creates it and lands on its detail page with the correct computed status.
- `amount = 0` or blank title is blocked client-side by Zod; a backend `400` maps onto the offending field.
- Editing a `NEEDS_REWORK` PO then resubmitting re-routes it (e.g. raising amount past $100 now requires manager approval).
