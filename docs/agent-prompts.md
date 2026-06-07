# Agent prompts — EntraID Backup & Restore (Epic ND-2199)

One prompt per ticket below, in recommended implementation order. Copy-paste into a fresh Claude Code session at `C:\sitaas\zeus`.

All work happens on `feature/ND-2199` (already checked out across every submodule via `switch_branch.sh feature/ND-2199`).

Story-level prompts (e.g. ND-2200) are usually only worth running *after* their subtasks are merged — they verify story-level acceptance criteria. Real implementation lives in the subtasks.

Each prompt invokes `/work-ticket`, which handles the Jira start → implement → build → commit → push → comment → finish loop against the rules in `CLAUDE.md`. Just point it at the spec.

---

## Story 1 — Foundations & domain model (ND-2200)

### ND-2201 · Add DataType.Identity and LocationType.ENTRA_ID

```
/work-ticket ND-2201
Spec: docs/designs/entraid-jira-import/01-foundations/01-data-type-and-location-type.md
```

### ND-2202 · Policy classes — backup and restore

```
/work-ticket ND-2202
Spec: docs/designs/entraid-jira-import/01-foundations/02-policy-classes.md
```

### ND-2203 · Item subclasses for directory objects

```
/work-ticket ND-2203
Spec: docs/designs/entraid-jira-import/01-foundations/03-item-subclasses.md
```

### ND-2200 · Foundations & domain model (story-level verification)

```
/work-ticket ND-2200
Spec: docs/designs/entraid-jira-import/01-foundations/description.md
```

---

## Story 2 — EntraID connector, read path (ND-2204)

### ND-2205 · Connector skeleton, Init, and ScanSources

```
/work-ticket ND-2205
Spec: docs/designs/entraid-jira-import/02-connector/01-connector-skeleton-and-scansources.md
```

### ND-2206 · Users + Groups scanners with delta support

```
/work-ticket ND-2206
Spec: docs/designs/entraid-jira-import/02-connector/02-users-and-groups-with-delta.md
```

### ND-2207 · Other object-kind scanners

```
/work-ticket ND-2207
Spec: docs/designs/entraid-jira-import/02-connector/03-other-object-kinds.md
```

### ND-2208 · Search and metadata

```
/work-ticket ND-2208
Spec: docs/designs/entraid-jira-import/02-connector/04-search-and-metadata.md
```

### ND-2204 · EntraID connector — read path (story-level verification)

```
/work-ticket ND-2204
Spec: docs/designs/entraid-jira-import/02-connector/description.md
```

---

## Story 3 — Backup pipeline (ND-2209)

### ND-2210 · Prometheus scanner workflows — Backup + Incremental

```
/work-ticket ND-2210
Spec: docs/designs/entraid-jira-import/03-backup-pipeline/01-prometheus-scanner-workflows.md
```

### ND-2211 · Hermes worker workflow — one blob per directory object

```
/work-ticket ND-2211
Spec: docs/designs/entraid-jira-import/03-backup-pipeline/02-hermes-worker-workflow.md
```

### ND-2209 · Backup pipeline (story-level verification)

```
/work-ticket ND-2209
Spec: docs/designs/entraid-jira-import/03-backup-pipeline/description.md
```

---

## Story 4 — Restore pipeline (ND-2212)

### ND-2213 · Phase resolver in Prometheus + backup→live ID mapping

```
/work-ticket ND-2213
Spec: docs/designs/entraid-jira-import/04-restore-pipeline/01-phase-resolver-and-mapping-table.md
```

### ND-2214 · Hermes restore handlers — skeletons (phase 1)

```
/work-ticket ND-2214
Spec: docs/designs/entraid-jira-import/04-restore-pipeline/02-leaf-handlers.md
```

### ND-2215 · Hermes restore handlers — references (phase 2)

```
/work-ticket ND-2215
Spec: docs/designs/entraid-jira-import/04-restore-pipeline/03-reference-handlers.md
```

### ND-2212 · Restore pipeline (story-level verification)

```
/work-ticket ND-2212
Spec: docs/designs/entraid-jira-import/04-restore-pipeline/description.md
```

---

## Story 5 — Apollo API + UI (ND-2216)

### ND-2217 · Snapshot service + IdentityController

```
/work-ticket ND-2217
Spec: docs/designs/entraid-jira-import/05-apollo-api-and-ui/01-snapshot-service-and-controller.md
```

### ND-2218 · Apollo frontend — Identity tab + restore UI

```
/work-ticket ND-2218
Spec: docs/designs/entraid-jira-import/05-apollo-api-and-ui/02-identity-ui.md
```

### ND-2216 · Apollo API + restore UI (story-level verification)

```
/work-ticket ND-2216
Spec: docs/designs/entraid-jira-import/05-apollo-api-and-ui/description.md
```

---

## Story 6 — Operations, auth & rollout (ND-2219)

### ND-2220 · Sitaas-app scope extension + re-consent + feature flag

```
/work-ticket ND-2220
Spec: docs/designs/entraid-jira-import/06-operations-rollout/01-sitaas-app-scope-extension-and-feature-flag.md
```

### ND-2221 · Reporting models + GA runbook

```
/work-ticket ND-2221
Spec: docs/designs/entraid-jira-import/06-operations-rollout/02-reporting-and-runbook.md
```

### ND-2219 · Operations, auth & rollout (story-level verification)

```
/work-ticket ND-2219
Spec: docs/designs/entraid-jira-import/06-operations-rollout/description.md
```
