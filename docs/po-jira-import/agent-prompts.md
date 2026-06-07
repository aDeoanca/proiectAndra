# Agent prompts — Purchase Order Management System

One prompt per ticket below, in recommended implementation order. Copy-paste into a fresh Claude Code session at `C:\proiectAndra`.

**Before you start:** every prompt assumes the agent reads `CLAUDE.md` first (the load-bearing invariants) and the linked spec. The agent implements the spec, builds, and runs the ticket's acceptance checks — but **stops before `git commit`** (you control commits). Branch/commit yourself when you're happy.

**Ticket keys:** these read `POM-NN-NN` until you run the import. Because this file lives inside the import folder, `import_to_jira.py` phase 4 rewrites every `POM-*` here into its real Jira key (e.g. `PO-123`) automatically — so after import the prompts reference real tickets.

**Order matters.** The stories are listed in dependency order, not folder order:
Foundations → Workflow engine → **Auth (before the API)** → Backend API → Frontend → Testing & ops. The API story consumes the current-user plumbing from Auth, so do Auth first. Story-level (`POM-0N`) prompts are worth running only *after* their subtasks land — they just verify the story's acceptance criteria.

---

## Story 1 — Foundations & domain model (POM-01)

### POM-01-01 · Monorepo scaffold and Dockerised Postgres

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-01-01
Spec: docs/po-jira-import/01-foundations/01-monorepo-and-docker-postgres.md
```

### POM-01-02 · JPA entities and enums

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-01-02
Spec: docs/po-jira-import/01-foundations/02-entities-and-enums.md
```

### POM-01-03 · Flyway baseline migration and seed data

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-01-03
Spec: docs/po-jira-import/01-foundations/03-flyway-schema-and-seed.md
```

### POM-01 · Foundations & domain model (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-01
Spec: docs/po-jira-import/01-foundations/description.md
```

---

## Story 2 — Workflow engine (POM-02)

### POM-02-01 · Pure routing function

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-02-01
Spec: docs/po-jira-import/02-workflow-engine/01-routing-function.md
```

### POM-02-02 · PurchaseOrderWorkflowService and guards

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-02-02
Spec: docs/po-jira-import/02-workflow-engine/02-workflow-service-and-guards.md
```

### POM-02-03 · Append-only history recording

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-02-03
Spec: docs/po-jira-import/02-workflow-engine/03-history-recording.md
```

### POM-02 · Workflow engine (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-02
Spec: docs/po-jira-import/02-workflow-engine/description.md
```

---

## Story 4 — Auth & roles (POM-04) — do this before the API

### POM-04-01 · Session login endpoints and user picker

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-04-01
Spec: docs/po-jira-import/04-auth/01-session-login-endpoints.md
```

### POM-04-02 · Current-user resolution and access enforcement

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-04-02
Spec: docs/po-jira-import/04-auth/02-current-user-and-enforcement.md
```

### POM-04 · Auth & roles (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-04
Spec: docs/po-jira-import/04-auth/description.md
```

---

## Story 3 — Backend API (POM-03)

### POM-03-04 · Global error handling

> Do this first within the story — the other endpoints rely on the exception → HTTP mapping.

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-03-04
Spec: docs/po-jira-import/03-backend-api/04-error-handling.md
```

### POM-03-01 · PO CRUD endpoints, DTOs, and validation

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-03-01
Spec: docs/po-jira-import/03-backend-api/01-po-crud-endpoints.md
```

### POM-03-02 · Workflow action endpoints

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-03-02
Spec: docs/po-jira-import/03-backend-api/02-workflow-action-endpoints.md
```

### POM-03-03 · Queue and filter listing

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-03-03
Spec: docs/po-jira-import/03-backend-api/03-queue-and-filter-listing.md
```

### POM-03 · Backend API (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-03
Spec: docs/po-jira-import/03-backend-api/description.md
```

---

## Story 5 — Frontend (POM-05)

### POM-05-01 · Next.js scaffold, API client, and query setup

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-05-01
Spec: docs/po-jira-import/05-frontend/01-scaffold-and-api-client.md
```

### POM-05-02 · Login, switch-user, and role-aware dashboard

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-05-02
Spec: docs/po-jira-import/05-frontend/02-login-and-dashboard.md
```

### POM-05-03 · PO detail with timeline and contextual actions

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-05-03
Spec: docs/po-jira-import/05-frontend/03-po-detail-and-timeline.md
```

### POM-05-04 · Shared create / edit form

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-05-04
Spec: docs/po-jira-import/05-frontend/04-create-edit-form.md
```

### POM-05 · Frontend (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-05
Spec: docs/po-jira-import/05-frontend/description.md
```

---

## Story 6 — Testing & ops (POM-06)

### POM-06-01 · Backend test suite (Testcontainers)

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-06-01
Spec: docs/po-jira-import/06-testing-and-ops/01-backend-test-suite.md
```

### POM-06-02 · Frontend smoke test and dev runbook

```
Implement this ticket. Read CLAUDE.md first, then the spec, follow the invariants, run the acceptance checks, and stop before committing.
Ticket: POM-06-02
Spec: docs/po-jira-import/06-testing-and-ops/02-frontend-smoke-and-runbook.md
```

### POM-06 · Testing & ops (story-level verification)

```
Verify the story's acceptance criteria are met. Read CLAUDE.md first, then the spec.
Ticket: POM-06
Spec: docs/po-jira-import/06-testing-and-ops/description.md
```
