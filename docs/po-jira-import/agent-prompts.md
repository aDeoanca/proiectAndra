# Agent prompts — Purchase Order Management System

One prompt per ticket below, in recommended implementation order. Copy-paste into a fresh Claude Code session at `C:\proiectAndra`.

**Before you start:** every prompt assumes the agent reads `CLAUDE.md` first (the load-bearing invariants) and the linked spec. The agent implements the spec, builds, and runs the ticket's acceptance checks — but **stops before `git commit`** (you control commits). Branch/commit yourself when you're happy.

**Ticket keys:** these read `POM-NN-NN` until you run the import. Because this file lives inside the import folder, `import_to_jira.py` phase 4 rewrites every `POM-*` here into its real Jira key (e.g. `PO-123`) automatically — so after import the prompts reference real tickets.

**Order matters.** The stories are listed in dependency order, not folder order:
Foundations → Workflow engine → **Auth (before the API)** → Backend API → Frontend → Testing & ops. The API story consumes the current-user plumbing from Auth, so do Auth first. Story-level (`POM-0N`) prompts are worth running only *after* their subtasks land — they just verify the story's acceptance criteria.

---

## Story 1 — Foundations & domain model (POM-3)

### POM-4 · Monorepo scaffold and Dockerised Postgres

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-4
Spec: docs/po-jira-import/01-foundations/01-monorepo-and-docker-postgres.md
```

### POM-5 · JPA entities and enums

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-5
Spec: docs/po-jira-import/01-foundations/02-entities-and-enums.md
```

### POM-6 · Flyway baseline migration and seed data

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-6
Spec: docs/po-jira-import/01-foundations/03-flyway-schema-and-seed.md
```

### POM-3 · Foundations & domain model (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-3
Spec: docs/po-jira-import/01-foundations/description.md
```

---

## Story 2 — Workflow engine (POM-7)

### POM-8 · Pure routing function

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-8
Spec: docs/po-jira-import/02-workflow-engine/01-routing-function.md
```

### POM-9 · PurchaseOrderWorkflowService and guards

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-9
Spec: docs/po-jira-import/02-workflow-engine/02-workflow-service-and-guards.md
```

### POM-10 · Append-only history recording

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-10
Spec: docs/po-jira-import/02-workflow-engine/03-history-recording.md
```

### POM-7 · Workflow engine (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-7
Spec: docs/po-jira-import/02-workflow-engine/description.md
```

---

## Story 4 — Auth & roles (POM-16) — do this before the API

### POM-17 · Session login endpoints and user picker

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-17
Spec: docs/po-jira-import/04-auth/01-session-login-endpoints.md
```

### POM-18 · Current-user resolution and access enforcement

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-18
Spec: docs/po-jira-import/04-auth/02-current-user-and-enforcement.md
```

### POM-16 · Auth & roles (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-16
Spec: docs/po-jira-import/04-auth/description.md
```

---

## Story 3 — Backend API (POM-11)

### POM-15 · Global error handling

> Do this first within the story — the other endpoints rely on the exception → HTTP mapping.

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-15
Spec: docs/po-jira-import/03-backend-api/04-error-handling.md
```

### POM-12 · PO CRUD endpoints, DTOs, and validation

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-12
Spec: docs/po-jira-import/03-backend-api/01-po-crud-endpoints.md
```

### POM-13 · Workflow action endpoints

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-13
Spec: docs/po-jira-import/03-backend-api/02-workflow-action-endpoints.md
```

### POM-14 · Queue and filter listing

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-14
Spec: docs/po-jira-import/03-backend-api/03-queue-and-filter-listing.md
```

### POM-11 · Backend API (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-11
Spec: docs/po-jira-import/03-backend-api/description.md
```

---

## Story 5 — Frontend (POM-19)

### POM-20 · Next.js scaffold, API client, and query setup

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-20
Spec: docs/po-jira-import/05-frontend/01-scaffold-and-api-client.md
```

### POM-21 · Login, switch-user, and role-aware dashboard

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-21
Spec: docs/po-jira-import/05-frontend/02-login-and-dashboard.md
```

### POM-22 · PO detail with timeline and contextual actions

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-22
Spec: docs/po-jira-import/05-frontend/03-po-detail-and-timeline.md
```

### POM-23 · Shared create / edit form

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-23
Spec: docs/po-jira-import/05-frontend/04-create-edit-form.md
```

### POM-19 · Frontend (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-19
Spec: docs/po-jira-import/05-frontend/description.md
```

---

## Story 6 — Testing & ops (POM-24)

### POM-25 · Backend test suite (Testcontainers)

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-25
Spec: docs/po-jira-import/06-testing-and-ops/01-backend-test-suite.md
```

### POM-26 · Frontend smoke test and dev runbook

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-26
Spec: docs/po-jira-import/06-testing-and-ops/02-frontend-smoke-and-runbook.md
```

### POM-24 · Testing & ops (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-24
Spec: docs/po-jira-import/06-testing-and-ops/description.md
```
