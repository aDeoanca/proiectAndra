# Importing these tickets into Jira — setup guide

A plain-English walkthrough to get this folder imported into Jira. ~10 minutes.

## What you need

1. **Python 3.10+** installed — check with `python --version`.
2. **The `requests` library** — install with `pip install requests`.
3. **A Jira Cloud account** with permission to create issues in your target project.
4. **A Jira API token** (see below — this is the one secret you need to provide).
5. **The two scripts** (`import_to_jira.py`, `sync_descriptions.py`) copied into this folder.

## Getting your Jira API token

A token is like a password the script uses to log into Jira on your behalf.

1. Go to **https://id.atlassian.com/manage-profile/security/api-tokens**
2. Click **Create API token**.
3. Give it a label (e.g. "po-import") and click **Create**.
4. **Copy the token now** — Atlassian only shows it once.

You also need:
- **Your Jira email** — the address you log into Jira with.
- **Your Jira site URL** — e.g. `https://yourcompany.atlassian.net`.
- **Your project key** — the short prefix on your tickets, e.g. `PO`, `PROJ`, `ABC` (shown as `ABC-123` on existing issues, or in Project settings → Details).

> **Security note:** the token grants access to your Jira. Don't commit it to git or paste it in chat/screenshots. Paste it directly into the script's config on your own machine, and revoke it from the same page above once the import is done if you like.

## Tell the script your details

Open `import_to_jira.py` and edit the **CONFIGURATION** block near the top:

```python
JIRA_URL           = "https://yourcompany.atlassian.net"   # your site
JIRA_EMAIL         = "you@yourcompany.com"                 # your login email
JIRA_API_TOKEN     = "<paste the token you just created>"
JIRA_PROJECT_KEY   = "PO"                                  # your project key

EPIC_SUMMARY       = "Purchase Order Management System"
PLACEHOLDER_PREFIX = "POM"
DEFAULT_LABELS     = ["po-management", "backend", "frontend"]
```

One thing that sometimes trips people up: the **subtask issue type name**. Most Jira projects call it `Sub-task`, some call it `Subtask`. If subtask creation fails, open `import_to_jira.py` and switch `TASK_ISSUE_TYPE` between those two values.

## Run it

From inside this folder:

```powershell
pip install requests

# 1. Preview — makes NO changes, just prints what would be created
python import_to_jira.py --dry-run

# 2. For real — creates the Epic, 6 Stories, and 18 Subtasks
python import_to_jira.py --accept-all
```

Always do the `--dry-run` first. It should show **1 Epic + 6 Stories + 18 Subtasks**. If that looks right, run the real command.

When it finishes you'll have the full ticket tree in Jira, and the script writes a `jira_mapping.json` file recording which Jira ticket each item became.

## If you change the text later

Edit any of the `.md` files, then push the updates to the existing tickets (this does **not** create duplicates):

```powershell
python sync_descriptions.py --dry-run    # preview
python sync_descriptions.py              # push the changes
```

## If something goes wrong

- **"Authentication failed"** — wrong email or token. Regenerate the token and re-paste it.
- **"Project not found"** — `JIRA_PROJECT_KEY` is wrong; check Project settings → Details.
- **Subtasks fail but the Epic/Stories worked** — flip `TASK_ISSUE_TYPE` between `Sub-task` and `Subtask`.
- **"mapping file already exists"** — you already ran a real import. Use `python import_to_jira.py --update-only` to just refresh the markdown, or delete `jira_mapping.json` to start over (this risks duplicate tickets).

---

**Full details:** `HOW_TO_IMPORT.md` in this folder. **What's being imported:** `Breakdown.md`.
