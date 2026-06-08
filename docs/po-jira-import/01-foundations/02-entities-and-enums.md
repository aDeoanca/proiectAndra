# JPA entities and enums

## What

- `User` entity — `id`, `name`, `email`, `role` (`@Enumerated(STRING)`).
- `PurchaseOrder` entity — `id`, `title`, `description`, `amount` (`BigDecimal`), `currency`, `category`, `status`, `creator` (`@ManyToOne User`), `createdAt`, `updatedAt` (auditing timestamps).
- `PoHistory` entity — `id`, `po` (`@ManyToOne`), `actor` (`@ManyToOne User`), `action`, `fromStatus`, `toStatus`, `comment`, `createdAt`. **Append-only** — no update/delete mappings.
- Enums: `Role` (`CREATOR`, `MANAGER`, `IT_REP`, `FINANCE`), `Category` (`SERVICES`, `OFFICE_SUPPLIES`, `IT_EQUIPMENT`), `Status` (`PENDING_MANAGER_APPROVAL`, `PENDING_IT_VALIDATION`, `PENDING_FINANCE_APPROVAL`, `NEEDS_REWORK`, `INVOICED`), `HistoryAction` (`SUBMIT`, `APPROVE`, `REJECT`, `RESUBMIT`).

## Notes

- `amount` is `BigDecimal` mapped to `DECIMAL(scale 2)` — never `double`/`float`.
- Enums persisted as `STRING`, not ordinal, so reordering values can't corrupt data.
- No `DRAFT` and no terminal `REJECTED` status — see ADR-0002.
- Depends on **POM-4** (project scaffold).

## Acceptance

- `@DataJpaTest` persists and reads back one `User`, one `PurchaseOrder`, and one `PoHistory` row.
- `Status` has exactly 5 values; `Category` and `Role` match the design.
- `PurchaseOrder.amount` column is `DECIMAL(_, 2)` in the generated/validated schema.
