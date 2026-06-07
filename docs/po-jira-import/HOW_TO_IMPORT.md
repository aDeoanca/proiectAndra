# How to import this folder into Jira

This directory matches the layout `import_to_jira.py` expects (the same scripts used for the EntraID import). To run the import:

## 1. Copy the scripts in here

```powershell
Copy-Item <path-to>\import_to_jira.py    C:\proiectAndra\docs\po-jira-import\
Copy-Item <path-to>\sync_descriptions.py C:\proiectAndra\docs\po-jira-import\
```

The script uses `Path(__file__).parent` as `JIRA_DIR`, so it has to live alongside the story directories.

## 2. Edit the CONFIGURATION block at the top of `import_to_jira.py`

Change these values for this project (leave `JIRA_URL`, `JIRA_EMAIL`, and the issue-type names alone unless your project differs):

```python
JIRA_PROJECT_KEY   = "<your project key>"     # was "ND" for EntraID
EPIC_SUMMARY       = "Purchase Order Management System"
EPIC_BODY_FILE     = "Breakdown.md"           # already correct
PLACEHOLDER_PREFIX = "POM"
DEFAULT_LABELS     = ["po-management", "backend", "frontend"]
```

`STORY_DESCRIPTION_FILE = "description.md"`, `EPIC_ISSUE_TYPE = "Epic"`, `STORY_ISSUE_TYPE = "Story"`, `TASK_ISSUE_TYPE = "Sub-task"` are already correct.

## 3. Dry-run first

```powershell
pip install requests
python import_to_jira.py --dry-run
```

This prints the full hierarchy without making any API calls. Confirm:

- Epic summary is "Purchase Order Management System".
- Six stories, in order: Foundations / Workflow engine / Backend API / Auth & roles / Frontend / Testing & ops.
- Subtask counts per story: 3 / 3 / 4 / 2 / 4 / 2 = 18 subtasks total.

## 4. Real run

```powershell
python import_to_jira.py --accept-all
```

(Drop `--accept-all` if you want a confirmation prompt after every issue.)

The script:

1. Creates the Epic.
2. Creates the 6 Stories under it.
3. Creates the 18 Subtasks under their respective Stories.
4. Saves `jira_mapping.json` (placeholder → real Jira key).
5. Backfills the real Jira keys into all the markdown files in this directory (the `POM-NN` / `POM-NN-NN` dependency references), so they stay in sync with the board.

## 5. Pushing later edits

After you tweak any of the markdown files locally:

```powershell
python sync_descriptions.py --dry-run    # preview
python sync_descriptions.py              # push
```

This re-uses `jira_mapping.json` and updates summaries + descriptions on the existing tickets — does not create new ones.

## Story breakdown

- **01-foundations** — monorepo scaffold, Docker Postgres, JPA entities + enums, Flyway schema + seed. *3 subtasks.*
- **02-workflow-engine** — routing function, hand-rolled workflow service, history recording. *3 subtasks.*
- **03-backend-api** — PO CRUD + DTOs, workflow action endpoints, queue/filter listing, error handling. *4 subtasks.*
- **04-auth** — cookie-session login over seeded users, current-user + role/self-approval enforcement. *2 subtasks.*
- **05-frontend** — Next.js scaffold + API client, dashboard, PO detail timeline, create/edit form. *4 subtasks.*
- **06-testing-and-ops** — Testcontainers backend suite, light frontend smoke test, runbook. *2 subtasks.*

Total: **1 Epic + 6 Stories + 18 Subtasks**.
