#!/usr/bin/env python3
"""
JIRA Description Sync — Markdown → JIRA issue descriptions
===========================================================

Reads the local jira_mapping.json produced by import_to_jira.py, walks the
same directory structure, and updates every matched JIRA issue's summary and
description from the corresponding markdown file on disk.

Useful after editing the local breakdown files to push the changes to JIRA
without re-importing everything.

FLAGS
-----
  --dry-run      Preview which issues would be updated without making any API
                 calls. Prints each issue key and its new summary.
  --epic         Also update the Epic description from the EPIC_BODY_FILE.
                 Without this flag the Epic is skipped.

USAGE
-----
  python3 sync_descriptions.py --dry-run    # preview
  python3 sync_descriptions.py              # update all stories + subtasks
  python3 sync_descriptions.py --epic       # also update the epic
"""

import json
import re
import sys
import time
from pathlib import Path

try:
    import requests
    from requests.auth import HTTPBasicAuth
except ImportError:
    sys.exit("Error: 'requests' is not installed. Run: pip install requests")

# Re-use all configuration and helpers from the import script.
# Module-level flag parsing in import_to_jira.py is harmless when imported.
from import_to_jira import (
    JIRA_DIR,
    MAPPING_FILE,
    JIRA_URL,
    JIRA_EMAIL,
    JIRA_API_TOKEN,
    JIRA_PROJECT_KEY,
    EPIC_ISSUE_TYPE,
    EPIC_BODY_FILE,
    STORY_DESCRIPTION_FILE,
    PLACEHOLDER_PREFIX,
    API_DELAY,
    REQUEST_TIMEOUT,
    DEFAULT_LABELS,
    markdown_to_adf,
    _extract_summary,
    parse_story_dir,
    parse_task_file,
    collect_items,
    green, yellow, red, bold, dim,
)

DRY_RUN      = "--dry-run" in sys.argv
UPDATE_EPIC  = "--epic"    in sys.argv

# Matches any JIRA-style issue key prefix at the start of a heading:
#   SA-01: …, SA-01-03: …, ND-2007: …, SEARCH-42: …
_ISSUE_KEY_RE = re.compile(r"^(#{1,6}\s+)[A-Z]+-\d+(?:-\d+)?:\s*", re.MULTILINE)


def _clean_body(md: str) -> str:
    """Strip issue key prefixes from all headings in a markdown body."""
    return _ISSUE_KEY_RE.sub(r"\1", md)


def _clean_summary(summary: str) -> str:
    """Strip a leading issue key prefix from a summary string."""
    return re.sub(r"^[A-Z]+-\d+(?:-\d+)?:\s*", "", summary)


# ── JIRA REST helpers ──────────────────────────────────────────────────────────

def _auth():
    return HTTPBasicAuth(JIRA_EMAIL, JIRA_API_TOKEN)

def _headers():
    return {"Accept": "application/json", "Content-Type": "application/json"}

def _api_url(path: str) -> str:
    return f"{JIRA_URL.rstrip('/')}/rest/api/3/{path.lstrip('/')}"


def jira_put(issue_key: str, summary: str, description_md: str):
    """
    Update an existing JIRA issue's summary and description.
    Returns True on success, False on failure.
    """
    payload = {
        "fields": {
            "summary":     summary,
            "description": markdown_to_adf(description_md),
        }
    }
    resp = requests.put(
        _api_url(f"issue/{issue_key}"),
        auth=_auth(),
        headers=_headers(),
        data=json.dumps(payload),
        timeout=REQUEST_TIMEOUT,
    )
    if not resp.ok:
        print(red(f"  PUT /issue/{issue_key} → HTTP {resp.status_code}"))
        print(red(f"  {resp.text[:400]}"))
        return False
    return True


# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    if DRY_RUN:
        print(bold(yellow("DRY-RUN MODE — no API calls will be made.\n")))

    # Load mapping
    if not MAPPING_FILE.exists():
        sys.exit(red(f"Mapping file not found: {MAPPING_FILE}\nRun import_to_jira.py first."))

    mapping: dict[str, str] = json.loads(MAPPING_FILE.read_text(encoding="utf-8"))
    print(f"Loaded {len(mapping)} mappings from {MAPPING_FILE.name}\n")

    updated = skipped = failed = 0

    # ── Epic ──────────────────────────────────────────────────────────────────
    if UPDATE_EPIC:
        epic_key  = mapping.get("EPIC")
        epic_path = JIRA_DIR / EPIC_BODY_FILE
        if epic_key and epic_path.exists():
            body    = _clean_body(epic_path.read_text(encoding="utf-8"))
            summary = _clean_summary(_extract_summary(body) or "Search Application")
            if DRY_RUN:
                print(yellow(f"  [dry-run] Would update Epic {epic_key}: {summary}"))
                updated += 1
            else:
                ok = jira_put(epic_key, summary, body)
                if ok:
                    print(green(f"  {epic_key}  {summary}"))
                    updated += 1
                    time.sleep(API_DELAY)
                else:
                    failed += 1
        else:
            print(yellow("  Epic skipped — key or body file missing"))

    # ── Stories + Subtasks ────────────────────────────────────────────────────
    stories = collect_items()
    print(bold(f"Syncing {len(stories)} stories…\n"))

    for story in stories:
        jira_key = mapping.get(story["sa_id"])
        if not jira_key:
            print(yellow(f"  {story['sa_id']} — not in mapping, skipping"))
            skipped += 1
            continue

        summary = _clean_summary(story["summary"])
        body    = _clean_body(story["body"])
        if DRY_RUN:
            print(yellow(f"  [dry-run] Would update {jira_key} ({story['sa_id']}): {summary}"))
            updated += 1
        else:
            ok = jira_put(jira_key, summary, body)
            if ok:
                print(green(f"  {story['sa_id']} → {jira_key}  {summary}"))
                updated += 1
                time.sleep(API_DELAY)
            else:
                failed += 1

        for task in story["tasks"]:
            task_key = mapping.get(task["sa_id"])
            if not task_key:
                print(yellow(f"    {task['sa_id']} — not in mapping, skipping"))
                skipped += 1
                continue

            task_summary = _clean_summary(task["summary"])
            task_body    = _clean_body(task["body"])
            if DRY_RUN:
                print(yellow(f"    [dry-run] Would update {task_key} ({task['sa_id']}): {task_summary}"))
                updated += 1
            else:
                ok = jira_put(task_key, task_summary, task_body)
                if ok:
                    print(f"    {task['sa_id']} → {task_key}  {task_summary}")
                    updated += 1
                    time.sleep(API_DELAY)
                else:
                    failed += 1

    print(bold(f"\n══ Sync complete ══"))
    print(f"  Updated: {updated}  Skipped: {skipped}  Failed: {failed}")


if __name__ == "__main__":
    main()
