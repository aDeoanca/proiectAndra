# Append-only history recording

## What

- A `PoHistoryRecorder` (or method on the workflow service) that writes one `po_history` row on every successful workflow action.
- Each row captures: `po`, `actor`, `action` (`SUBMIT`/`APPROVE`/`REJECT`/`RESUBMIT`), `fromStatus`, `toStatus`, `comment` (null except on reject), `createdAt`.
- History is written in the **same transaction** as the status change, so an action and its audit row commit or roll back together.
- A read method `historyFor(poId)` returning rows ordered by `createdAt` ascending (powers the UI timeline in Story 5).

## Notes

- Append-only: no update or delete paths on `po_history`.
- `fromStatus` is null for the initial `SUBMIT`.
- Depends on **POM-9** (workflow service calls the recorder).

## Acceptance

- A submit → approve → reject → resubmit sequence produces 4 history rows with correct `from`/`to`/`action`/`actor`.
- The reject row carries the comment; approve/submit rows carry none.
- Rolling back a failed action leaves no orphan history row.
- `historyFor` returns rows in chronological order.
