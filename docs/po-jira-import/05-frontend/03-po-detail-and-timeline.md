# PO detail with timeline and contextual actions

## What

- **PO detail page** — `GET /api/pos/{id}`: shows all fields (title, description, amount, currency, category, status, creator, timestamps).
- **Status timeline** — renders `history[]` chronologically (action, actor, from→to, comment, time), so the rejection reason and every prior attempt are visible.
- **Context-aware actions**, shown only when legal for the current user:
  - Reviewer owning the current state → **Approve** and **Reject** (reject opens a required-comment dialog).
  - Creator when status is `NEEDS_REWORK` → **Edit** (→ form) and **Resubmit**.
- Actions call the verb endpoints (POM-03-02), then invalidate the relevant queries so the detail + dashboard refresh.

## Notes

- The client decides which buttons to show from `currentUser.role` + `po.status` + creator, but the backend remains the authority (it will `403`/`409` an illegal attempt, surfaced as a toast).
- Reject button must block submit until a non-empty comment is entered (mirrors backend rule).
- Depends on **POM-05-01** and **POM-03-02**.

## Acceptance

- A finance user viewing a `PENDING_FINANCE_APPROVAL` PO sees Approve/Reject; a creator viewing their `NEEDS_REWORK` PO sees Edit/Resubmit.
- Approving/rejecting updates the status and appends a timeline entry without a full reload.
- The timeline shows the reject comment.
