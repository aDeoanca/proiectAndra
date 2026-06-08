#!/usr/bin/env python3
"""
JIRA Cloud Import Script — Markdown → Epic/Story/Subtask
=========================================================

DESCRIPTION
-----------
This script imports a directory of structured markdown files into JIRA Cloud
as a three-level issue hierarchy: one Epic, N Stories, and N Subtasks per
Story.  After creating the issues it saves a mapping of placeholder IDs to
real JIRA keys, then backfills every markdown file with those real keys so
the local files stay in sync with the board.

It is reusable for any project — configure the epic name, project key,
placeholder prefix, and issue type names in the CONFIGURATION section below,
point the script at a directory that follows the input format described here,
and run it.

Markdown descriptions are automatically converted to Atlassian Document Format
(ADF), which is the only rich-text format accepted by the JIRA Cloud API v3.
The conversion handles headings, pipe tables, bullet and ordered lists,
task-list checkboxes (- [ ]), fenced code blocks, inline bold, inline code,
and hyperlinks.


INPUT FORMAT
------------
Place this script in a directory alongside the markdown files you want to
import.  The directory layout must follow this structure:

  <this directory>/
  ├── import_to_jira.py              This script.
  │
  ├── <epic-body-file>.md            A single file used as the Epic's
  │                                  description body. The filename is set by
  │                                  the EPIC_BODY_FILE configuration variable.
  │
  ├── NN-<story-slug>/               One directory per Story issue.
  │   │                              NN is a two-digit number (01–99).
  │   │                              The slug after the hyphen is free-form.
  │   │                              Examples: 01-auth, 14-search, 22-logging
  │   │
  │   ├── <description-file>.md      Story description. The filename is set by
  │   │                              STORY_DESCRIPTION_FILE (default:
  │   │                              description.md). The first # or ## heading
  │   │                              becomes the Story's JIRA summary. The full
  │   │                              file content becomes the description body.
  │   │
  │   ├── NN-<task-slug>.md          One file per Subtask issue.
  │   │                              NN is a two-digit number (01–99).
  │   └── NN-<task-slug>.md          Same heading/body rules as the Story file.
  │
  ├── NN-<story-slug>/               More story directories…
  │   └── …
  │
  └── (other files)                  Anything that does not match the NN-
                                     directory pattern is ignored during
                                     parsing (mapping output, this script,
                                     README files, etc.).

Naming rules:
  - Story dirs must start with exactly two digits followed by a hyphen.
    Directories that do not match this pattern are silently skipped.
  - Task files must be .md files inside a story directory. The description
    file (configurable name) is excluded; every other .md file is treated
    as a Subtask.
  - Directories and files are processed in alphabetical order, which equals
    numeric order given the NN- prefix convention.

Placeholder IDs:
  Markdown files may contain placeholder identifiers that follow the pattern
  <PREFIX>-NN (story-level) and <PREFIX>-NN-NN (task-level), where PREFIX is
  set by the PLACEHOLDER_PREFIX configuration variable. For example, with
  PREFIX = "SA":

    SA-01      → Story placeholder   (derived from dir name 01-*)
    SA-01-03   → Subtask placeholder (derived from dir 01-*, file 03-*.md)

  The script derives these IDs automatically from the directory/file names
  during parsing. After all issues are created, Phase 4 replaces every
  occurrence of these placeholders in every .md file with the real JIRA keys.

  If STRIP_PLACEHOLDER_PREFIX = True (default), the placeholder prefix and
  its colon separator (e.g. "SA-01: ") are stripped from issue summaries
  before sending them to JIRA, so that issue titles read cleanly without
  the internal numbering.

Markdown conventions for file content:
  - The first level-1 or level-2 heading (# or ##) is extracted as the JIRA
    issue summary. Everything else becomes the description body.
  - Supported inline formatting: **bold**, `code`, [label](url)
  - Supported block elements: headings (#–######), pipe tables, bullet lists,
    ordered lists, task-list items (- [ ] / - [x]), fenced code blocks (```).


JIRA HIERARCHY CREATED
-----------------------
  Epic       ← one, using EPIC_SUMMARY as title and EPIC_BODY_FILE as body
  └── Story      ← one per NN-* directory, parented to the Epic
      └── Subtask    ← one per task .md file, parented to its Story

  Parent/child links use JIRA Cloud's "parent" field, which works for both
  company-managed and team-managed projects. No custom Epic Link field is used.


PHASES
------
  1. Parse     Walk the directory for NN-* story dirs. Read the description
               file per story and all other .md files as subtasks. Extract
               summaries from first headings.
  2. Create    POST to JIRA Cloud API: 1 Epic → N Stories → N Subtasks.
               Pauses for user confirmation after each issue unless
               --accept-all is set. Aborts (unless --force) if the mapping
               file already exists to prevent duplicate issue creation.
               On failure or Ctrl+C, saves a partial mapping so no created
               issues are lost.
  3. Save map  Write the mapping file (placeholder → real JIRA key) as JSON.
  4. Update    Replace every placeholder ID in every .md file in this
               directory with the real JIRA key from the mapping. Longest
               placeholders are replaced first to prevent partial-match
               corruption (e.g. SA-01-01 before SA-01).


AUTHENTICATION
--------------
  JIRA Cloud uses HTTP Basic Auth: your Atlassian account email + an API token.
  Generate a token at: https://id.atlassian.com/manage-profile/security/api-tokens
  Store both in the JIRA_EMAIL and JIRA_API_TOKEN variables in the configuration
  section below.


FLAGS
-----
  --dry-run           Preview all operations without making any API calls or
                      modifying any files. Prints the full hierarchy that would
                      be created and which files would be updated. Use this to
                      verify the parsed structure before committing to an import.
  --accept-all        Disable the per-issue confirmation prompt. Without this
                      flag the script pauses after every created issue and waits
                      for ENTER before proceeding. Use --accept-all for
                      unattended runs.
  --force             Proceed even if the mapping file already exists. Without
                      this flag the script aborts when the file is present to
                      prevent creating duplicate issues.
  --update-only       Skip phases 1–3 entirely. Load the existing mapping file
                      and run phase 4 (markdown file updates) only. Use this if
                      the import completed but markdown files were not fully
                      updated, or after manually editing the mapping file.
  --epic KEY          Use an existing JIRA Epic rather than creating a new one.
                      KEY must be a valid issue key in the configured project
                      (e.g. SEARCH-1). The script verifies the issue exists and
                      is of type Epic before proceeding. Stories are created as
                      children of that Epic. The "EPIC" entry in the mapping
                      file is set to this key.


USAGE
-----
  pip install requests
  python3 import_to_jira.py --dry-run                      # verify structure first
  python3 import_to_jira.py --accept-all                   # unattended full import
  python3 import_to_jira.py                                # interactive full import
  python3 import_to_jira.py --update-only                  # re-run markdown update
  python3 import_to_jira.py --force --accept-all           # force re-import
  python3 import_to_jira.py --epic SEARCH-1 --accept-all   # reuse existing epic
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
    sys.exit("Error: 'requests' is not installed.  Run: pip install requests")

# ==============================================================================
# CONFIGURATION — edit these values before running
# ==============================================================================

# Your JIRA Cloud instance URL — no trailing slash.
JIRA_URL = "https://deoancaioanaandra.atlassian.net"

# Your Atlassian account email address.
JIRA_EMAIL = "deoancaioanaandra@gmail.com"

# API token for authentication.
# Generate one at: https://id.atlassian.com/manage-profile/security/api-tokens
JIRA_API_TOKEN = "YOUR_ATLASSIAN_API_TOKEN_HERE"

# Key of the destination JIRA project (e.g. "SEARCH", "SA", "PORTAL").
# This appears as the prefix in all created issue keys: SEARCH-1, SEARCH-2, …
JIRA_PROJECT_KEY = "POM"

# Issue type names — must match exactly what your project uses.
# To list all types available in your project:
#   GET /rest/api/3/project/{projectKey}/statuses
# Common variants: "Subtask" vs "Sub-task" — check yours if subtask creation fails.
EPIC_ISSUE_TYPE  = "Epic"
STORY_ISSUE_TYPE = "Story"
TASK_ISSUE_TYPE  = "Subtask"

# Summary (title) for the Epic issue.
EPIC_SUMMARY = "Purchase Order Management System"

# File in this directory to use as the Epic's description body.
EPIC_BODY_FILE = "Breakdown.md"

# Filename within each story directory that holds the story description.
STORY_DESCRIPTION_FILE = "description.md"

# Prefix used in placeholder IDs throughout the markdown files.
# Placeholder IDs follow the pattern <PREFIX>-NN (stories) and <PREFIX>-NN-NN
# (subtasks). The script derives these from directory/file names and uses them
# as keys in the mapping file. Phase 4 replaces them with real JIRA keys.
# Examples: "SA" → SA-01, SA-01-01  |  "PLAT" → PLAT-01, PLAT-01-03
PLACEHOLDER_PREFIX = "POM"

# When True, strips the placeholder prefix and colon from issue summaries
# (e.g. "SA-01: Project Foundation" becomes "Project Foundation").
# Recommended: the placeholder IDs are internal and won't match real JIRA keys.
STRIP_PLACEHOLDER_PREFIX = True

# Output filename for the placeholder → real JIRA key mapping, saved in this
# directory. Written as JSON: {"SA-01": "SEARCH-5", "SA-01-01": "SEARCH-6", …}
MAPPING_FILENAME = "jira_mapping.json"

# Labels applied to every created issue. Set to [] to apply no labels.
DEFAULT_LABELS = ["po-management", "backend", "frontend"]

# Seconds to pause between consecutive API calls.
# JIRA Cloud rate-limits to ~10 req/s on the free/standard tier; 0.35 s is safe.
API_DELAY = 0.35

# Timeout in seconds for all HTTP requests.
REQUEST_TIMEOUT = 15

# ==============================================================================
# END CONFIGURATION
# ==============================================================================

JIRA_DIR     = Path(__file__).parent.resolve()
MAPPING_FILE = JIRA_DIR / MAPPING_FILENAME

DRY_RUN     = "--dry-run"     in sys.argv
FORCE       = "--force"       in sys.argv
UPDATE_ONLY = "--update-only" in sys.argv
ACCEPT_ALL  = "--accept-all"  in sys.argv

def _flag_value(flag: str) -> str | None:
    """Return the value following a named flag (e.g. --epic SEARCH-1), or None."""
    try:
        return sys.argv[sys.argv.index(flag) + 1]
    except (ValueError, IndexError):
        return None

EXISTING_EPIC_KEY = _flag_value("--epic")


# ── Terminal colour helpers ────────────────────────────────────────────────────

def green(s):  return f"\033[32m{s}\033[0m"
def yellow(s): return f"\033[33m{s}\033[0m"
def red(s):    return f"\033[31m{s}\033[0m"
def bold(s):   return f"\033[1m{s}\033[0m"
def dim(s):    return f"\033[2m{s}\033[0m"


def _await_confirm(created: str, mapping: dict[str, str] = None):
    """
    Pause after a successful JIRA issue creation and wait for the user to press
    ENTER before proceeding to the next API call.

    Skipped automatically when:
      - ACCEPT_ALL is True (--accept-all flag was passed), or
      - DRY_RUN is True (no real API calls are being made).

    If the user presses Ctrl+C, the script saves whatever mapping has been
    built so far (so --update-only can be used later) and then exits.

    created: short human-readable label for what was just created, shown in
             the prompt (e.g. "Epic SEARCH-1" or "Story SEARCH-2").
    mapping:  the in-progress placeholder → JIRA key mapping dict. Saved to
              disk on Ctrl+C so the user doesn't lose track of created issues.
    """
    if ACCEPT_ALL or DRY_RUN:
        return
    try:
        input(dim(f"  ↳ Created {created}. Press ENTER to continue, Ctrl+C to abort…"))
    except KeyboardInterrupt:
        print()
        if mapping:
            MAPPING_FILE.write_text(json.dumps(mapping, indent=2), encoding="utf-8")
            print(yellow(f"  Partial mapping saved → {MAPPING_FILE}"))
        sys.exit(
            yellow(
                "\nImport interrupted. Issues created so far are already saved in JIRA.\n"
                f"Run with --update-only to apply the markdown updates using the saved mapping.\n"
            )
        )


# ── Markdown → Atlassian Document Format (ADF) ────────────────────────────────
#
# JIRA Cloud API v3 requires issue descriptions as ADF JSON — it does not
# accept plain text or classic wiki markup.  The functions below implement a
# minimal but practical subset of the ADF spec covering everything present in
# the breakdown markdown files: headings, pipe tables, bullet/ordered lists,
# task-list items (- [ ]), fenced code blocks, and paragraphs with inline bold,
# inline code, and hyperlinks.
#
# ADF spec reference: https://developer.atlassian.com/cloud/jira/platform/apis/document/structure/

def _text_node(text: str, marks: list = None) -> dict:
    """ADF text leaf node, optionally with marks (bold, code, etc.)."""
    node = {"type": "text", "text": text}
    if marks:
        node["marks"] = marks
    return node


def _paragraph(*inline_nodes) -> dict:
    """ADF paragraph node wrapping a list of inline (text) nodes."""
    return {"type": "paragraph", "content": list(inline_nodes)}


def _heading(level: int, text: str) -> dict:
    """ADF heading node (levels 1–6)."""
    return {
        "type":    "heading",
        "attrs":   {"level": min(level, 6)},
        "content": [_text_node(text)],
    }


def _inline_nodes(raw: str) -> list[dict]:
    """
    Parse a raw text string into a list of ADF inline nodes.

    Handles (in priority order):
      **bold text**     → strong mark
      `inline code`     → code mark
      [label](url)      → link mark (label as text, href as the URL)
      everything else   → plain text node
    """
    nodes = []
    pattern = re.compile(
        r'\*\*(.+?)\*\*'                # group 1: bold text
        r'|`([^`]+)`'                   # group 2: inline code
        r'|\[([^\]]+)\]\(([^)]+)\)'     # group 3: link label, group 4: link URL
        r'|((?:(?!\*\*|`|\[).)+)',      # group 5: plain text (anything else)
        re.DOTALL,
    )
    for m in pattern.finditer(raw):
        if m.group(1):
            nodes.append(_text_node(m.group(1), [{"type": "strong"}]))
        elif m.group(2):
            nodes.append(_text_node(m.group(2), [{"type": "code"}]))
        elif m.group(3):
            nodes.append(_text_node(m.group(3), [{"type": "link", "attrs": {"href": m.group(4)}}]))
        elif m.group(5):
            nodes.append(_text_node(m.group(5)))
    return nodes or [_text_node(raw)]


def _clean_list_text(raw_item: str) -> str:
    """
    Strip the list marker and optional GitHub-style checkbox prefix from a
    raw list item line.

    Examples:
      "- [ ] Acceptance criterion"  →  "Acceptance criterion"
      "- [x] Done item"             →  "Done item"
      "* plain bullet"              →  "plain bullet"
      "1. Ordered item"             →  stripped by caller via regex
    """
    text = raw_item.lstrip("-•* \t").strip()
    text = re.sub(r'^\[[ xX]\]\s*', '', text)   # strip checkbox marker
    return text


def _bullet_list(raw_items: list[str]) -> dict:
    """ADF unordered list node from a list of raw markdown bullet lines."""
    return {
        "type": "bulletList",
        "content": [
            {
                "type":    "listItem",
                "content": [_paragraph(*_inline_nodes(_clean_list_text(item)))],
            }
            for item in raw_items
            if item.strip()
        ],
    }


def _ordered_list(raw_items: list[str]) -> dict:
    """ADF ordered list node from a list of raw markdown numbered lines."""
    return {
        "type": "orderedList",
        "content": [
            {
                "type":    "listItem",
                "content": [_paragraph(*_inline_nodes(re.sub(r'^\d+\.\s*', '', item).strip()))],
            }
            for item in raw_items
            if item.strip()
        ],
    }


def _code_block(code: str, lang: str = "") -> dict:
    """ADF code block node with optional language annotation."""
    node = {"type": "codeBlock", "content": [_text_node(code)]}
    if lang:
        node["attrs"] = {"language": lang}
    return node


def _is_table_separator(line: str) -> bool:
    """
    Return True if the line is a markdown table alignment/separator row.
    Matches rows like:  |---|---|  |:---|:---:|  | --- | :---: |
    """
    return bool(re.match(r'^\|[-| :]+\|$', line.strip()))


def _parse_table_cells(line: str) -> list[str]:
    """Split a markdown table row into a list of cell text strings."""
    return [cell.strip() for cell in line.strip().strip("|").split("|")]


def _adf_table(rows: list[str]) -> dict:
    """
    Convert a list of raw markdown table row strings to an ADF table node.

    The first non-separator row becomes tableHeader cells; all subsequent
    rows become tableCell cells.  The separator row (|---|---|) is skipped.
    """
    adf_rows = []
    header_done = False

    for row in rows:
        if _is_table_separator(row):
            header_done = True   # everything after the separator is body rows
            continue

        cell_type = "tableCell" if header_done else "tableHeader"
        adf_rows.append({
            "type": "tableRow",
            "content": [
                {
                    "type":    cell_type,
                    "attrs":   {},
                    "content": [_paragraph(*_inline_nodes(cell))],
                }
                for cell in _parse_table_cells(row)
            ],
        })

    return {
        "type":    "table",
        "attrs":   {"isNumberColumnEnabled": False, "layout": "default"},
        "content": adf_rows,
    }


def markdown_to_adf(md: str) -> dict:
    """
    Convert a markdown string to an Atlassian Document Format (ADF) document.

    Supported constructs (in detection order per line):
      - Fenced code blocks (``` … ```)
      - ATX headings (# through ######)
      - Pipe tables (| col | col |)
      - Unordered lists (-, *, •) including task-list items (- [ ])
      - Ordered lists (1. 2. …)
      - Horizontal rules (--- or ────) — skipped (ADF rule node not rendered well)
      - Blank lines — skipped
      - Everything else → paragraph with inline bold/code/link handling

    Returns a complete ADF document node {"type": "doc", "version": 1, …}.
    """
    lines = md.splitlines()
    content: list[dict] = []
    i = 0

    while i < len(lines):
        line = lines[i]

        # ── Fenced code block ────────────────────────────────────────────────
        fence = re.match(r"^```(\w*)", line)
        if fence:
            lang = fence.group(1)
            code_lines: list[str] = []
            i += 1
            while i < len(lines) and not lines[i].startswith("```"):
                code_lines.append(lines[i])
                i += 1
            content.append(_code_block("\n".join(code_lines), lang))
            i += 1   # skip closing ```
            continue

        # ── ATX heading ──────────────────────────────────────────────────────
        heading = re.match(r"^(#{1,6})\s+(.*)", line)
        if heading:
            level = len(heading.group(1))
            content.append(_heading(level, heading.group(2).strip()))
            i += 1
            continue

        # ── Pipe table ───────────────────────────────────────────────────────
        # Collect all consecutive table rows (including the separator row).
        if line.strip().startswith("|") and line.strip().endswith("|"):
            table_lines: list[str] = []
            while (
                i < len(lines)
                and lines[i].strip().startswith("|")
                and lines[i].strip().endswith("|")
            ):
                table_lines.append(lines[i])
                i += 1
            content.append(_adf_table(table_lines))
            continue

        # ── Unordered list block ─────────────────────────────────────────────
        # Also matches task-list items (- [ ] text).
        if re.match(r"^[-*•]\s", line):
            items: list[str] = []
            while i < len(lines) and re.match(r"^[-*•]\s", lines[i]):
                items.append(lines[i])
                i += 1
            content.append(_bullet_list(items))
            continue

        # ── Ordered list block ───────────────────────────────────────────────
        if re.match(r"^\d+\.\s", line):
            items = []
            while i < len(lines) and re.match(r"^\d+\.\s", lines[i]):
                items.append(lines[i])
                i += 1
            content.append(_ordered_list(items))
            continue

        # ── Horizontal rule — skip ───────────────────────────────────────────
        if re.match(r"^[-─═*]{3,}$", line.strip()):
            i += 1
            continue

        # ── Blank line — skip ────────────────────────────────────────────────
        if not line.strip():
            i += 1
            continue

        # ── Paragraph ────────────────────────────────────────────────────────
        # Collect consecutive non-blank, non-special lines into one paragraph.
        # Stop collecting when the next line starts a heading, code fence,
        # table row, list item, or horizontal rule.
        para_lines: list[str] = []
        while i < len(lines) and lines[i].strip() and not re.match(
            r"^(#{1,6}\s|```|[-*•]\s|\d+\.\s|[-─═*]{3,}$)",
            lines[i],
        ) and not (lines[i].strip().startswith("|") and lines[i].strip().endswith("|")):
            para_lines.append(lines[i])
            i += 1

        raw = " ".join(para_lines)
        nodes = _inline_nodes(raw)
        if nodes:
            content.append(_paragraph(*nodes))

    # ADF documents must have at least one block node.
    if not content:
        content.append(_paragraph(_text_node("")))

    return {"type": "doc", "version": 1, "content": content}


# ── Markdown file parsing ──────────────────────────────────────────────────────

def _extract_summary(text: str) -> str:
    """
    Extract the first level-1 or level-2 heading from the file as the JIRA
    issue summary.

    If STRIP_PLACEHOLDER_PREFIX is True, removes the leading placeholder ID
    and colon (e.g. "SA-01: " or "SA-01-03: ") before returning, since those
    IDs are internal and won't match real JIRA keys.

    Returns "Untitled" if no heading is found.
    """
    for line in text.splitlines():
        m = re.match(r"^#{1,2}\s+(.+)", line)
        if m:
            title = m.group(1).strip()
            if STRIP_PLACEHOLDER_PREFIX:
                prefix = re.escape(PLACEHOLDER_PREFIX)
                title = re.sub(rf"^{prefix}-\d{{2}}(?:-\d{{2}})?:\s*", "", title)
            return title
    return "Untitled"


def parse_story_dir(story_dir: Path) -> dict | None:
    """
    Parse a story directory and return a story dict, or None if the directory
    does not contain a description file.

    The story's placeholder ID is derived from the PLACEHOLDER_PREFIX and the
    two-digit numeric prefix of the directory name (e.g. with prefix "SA",
    directory "01-project-foundation" → "SA-01").

    Task files are every .md file in the directory except the description file,
    sorted alphabetically (which equals numeric order given the NN- prefix).

    Returns:
        {
            "sa_id":      "<PREFIX>-01",
            "summary":    "Project Foundation & Infrastructure",
            "body":       "<full markdown text>",
            "task_files": [Path, Path, …],
        }
    """
    desc_path = story_dir / STORY_DESCRIPTION_FILE
    if not desc_path.exists():
        return None

    body = desc_path.read_text(encoding="utf-8")

    return {
        "sa_id":      f"{PLACEHOLDER_PREFIX}-{story_dir.name.split('-')[0]}",
        "summary":    _extract_summary(body),
        "body":       body,
        "task_files": sorted(
            f for f in story_dir.iterdir()
            if f.is_file() and f.suffix == ".md" and f.name != STORY_DESCRIPTION_FILE
        ),
    }


def parse_task_file(task_file: Path, story_dir: Path) -> dict:
    """
    Parse a task file and return a task dict.

    The task's placeholder ID is derived from the PLACEHOLDER_PREFIX, the
    two-digit prefix of the story directory, and the two-digit prefix of the
    task filename (e.g. with prefix "SA", dir "01-…", file "03-*.md" → "SA-01-03").

    Returns:
        {
            "sa_id":   "<PREFIX>-01-03",
            "summary": "Add ASP.NET Core Identity packages",
            "body":    "<full markdown text>",
        }
    """
    body = task_file.read_text(encoding="utf-8")
    return {
        "sa_id":   f"{PLACEHOLDER_PREFIX}-{story_dir.name.split('-')[0]}-{task_file.stem.split('-')[0]}",
        "summary": _extract_summary(body),
        "body":    body,
    }


# ── JIRA Cloud REST API helpers ────────────────────────────────────────────────

def _auth() -> HTTPBasicAuth:
    """HTTP Basic Auth using the configured Atlassian email and API token."""
    return HTTPBasicAuth(JIRA_EMAIL, JIRA_API_TOKEN)


def _headers() -> dict:
    return {"Accept": "application/json", "Content-Type": "application/json"}


def _api_url(path: str) -> str:
    """Build a full JIRA Cloud REST API v3 URL from a relative path."""
    return f"{JIRA_URL.rstrip('/')}/rest/api/3/{path.lstrip('/')}"


def jira_get(path: str) -> dict:
    """Perform an authenticated GET request and return the parsed JSON body."""
    resp = requests.get(_api_url(path), auth=_auth(), headers=_headers(), timeout=REQUEST_TIMEOUT)
    resp.raise_for_status()
    return resp.json()


def jira_post(path: str, payload: dict) -> dict:
    """
    Perform an authenticated POST request and return the parsed JSON body.
    Prints the error body before raising if the response is not 2xx.
    """
    resp = requests.post(
        _api_url(path),
        auth=_auth(),
        headers=_headers(),
        data=json.dumps(payload),
        timeout=REQUEST_TIMEOUT,
    )
    if not resp.ok:
        print(red(f"  POST /{path} → HTTP {resp.status_code}"))
        print(red(f"  {resp.text[:600]}"))
        resp.raise_for_status()
    return resp.json()


def validate_connection():
    """
    Confirm that the configured credentials are valid and the target project
    exists before starting the import.  Exits the process on any failure.
    """
    print("Validating JIRA Cloud connection…")
    try:
        me = jira_get("myself")
        print(green(f"  Authenticated as: {me.get('displayName') or me.get('emailAddress')}"))
    except Exception as exc:
        sys.exit(red(f"  Authentication failed: {exc}"))

    try:
        proj = jira_get(f"project/{JIRA_PROJECT_KEY}")
        print(green(f"  Project found: {proj['name']} (key: {proj['key']}, id: {proj['id']})"))
    except Exception as exc:
        sys.exit(red(f"  Project '{JIRA_PROJECT_KEY}' not found: {exc}"))


def validate_existing_epic(epic_key: str) -> str:
    """
    Verify that the given issue key exists and is an Epic.
    Returns the epic key on success; exits the process on failure.
    """
    print(f"Validating existing epic {epic_key}…")
    try:
        issue = jira_get(f"issue/{epic_key}?fields=summary,issuetype")
    except Exception as exc:
        sys.exit(red(f"  Could not fetch issue {epic_key}: {exc}"))

    issue_type = issue.get("fields", {}).get("issuetype", {}).get("name", "")
    if issue_type != EPIC_ISSUE_TYPE:
        sys.exit(red(f"  {epic_key} is a '{issue_type}', not an '{EPIC_ISSUE_TYPE}'."))

    summary = issue.get("fields", {}).get("summary", "")
    print(green(f"  Epic found: {epic_key} — {summary}"))
    return epic_key


def create_issue(
    summary: str,
    description_md: str,
    issue_type: str,
    parent_key: str | None = None,
) -> str:
    """
    Create a single JIRA Cloud issue and return its key (e.g. "SEARCH-42").

    Parent/child relationships are established via the "parent" field:
      - Story  → parent_key = Epic key
      - Subtask → parent_key = Story key

    This approach is correct for JIRA Cloud (both company-managed and
    team-managed projects).  The older Epic Link custom field approach is a
    JIRA Server/Data Center convention and is not used here.

    In dry-run mode returns a placeholder key without making any API call.

    Sleeps API_DELAY seconds after each successful creation to respect
    JIRA Cloud's rate limits.
    """
    fields: dict = {
        "project":     {"key": JIRA_PROJECT_KEY},
        "summary":     summary,
        "issuetype":   {"name": issue_type},
        "description": markdown_to_adf(description_md),
    }

    if DEFAULT_LABELS:
        fields["labels"] = DEFAULT_LABELS

    if parent_key:
        fields["parent"] = {"key": parent_key}

    if DRY_RUN:
        return f"{JIRA_PROJECT_KEY}-DRY"

    result = jira_post("issue", {"fields": fields})
    time.sleep(API_DELAY)
    return result["key"]


# ── Phase 1: collect all story / task metadata ────────────────────────────────

def collect_items() -> list[dict]:
    """
    Walk the script's directory and return a list of story dicts, each with
    their tasks embedded.

    Only directories whose names start with two digits followed by a hyphen
    (e.g. "01-project-foundation") are treated as story directories.
    Everything else (the epic body file, this script, the mapping file, etc.)
    is ignored.

    Returns:
        [
            {
                "sa_id":   "<PREFIX>-01",
                "summary": "...",
                "body":    "…",
                "tasks": [
                    {"sa_id": "<PREFIX>-01-01", "summary": "...", "body": "…"},
                    …
                ],
            },
            …
        ]
    """
    stories = []
    for entry in sorted(JIRA_DIR.iterdir()):
        if not entry.is_dir() or not re.match(r"^\d{2}-", entry.name):
            continue

        story = parse_story_dir(entry)
        if story is None:
            print(yellow(f"  Skipping {entry.name} — no {STORY_DESCRIPTION_FILE} found"))
            continue

        story["tasks"] = [parse_task_file(tf, entry) for tf in story.pop("task_files")]
        stories.append(story)

    return stories


# ── Phase 2: create all issues in JIRA ────────────────────────────────────────

def create_all_issues(stories: list[dict]) -> dict[str, str]:
    """
    Create the full epic hierarchy in JIRA Cloud and return the placeholder
    → real JIRA key mapping.

    Order: Epic first, then stories in directory order, then each story's
    subtasks in file order.  This order matters because each issue needs its
    parent's key before it can be created.

    Returns:
        {
            "EPIC":           "SEARCH-1",
            "<PREFIX>-01":    "SEARCH-2",
            "<PREFIX>-01-01": "SEARCH-3",
            …
        }
    """
    mapping: dict[str, str] = {}

    # ── Epic ─────────────────────────────────────────────────────────────────
    if EXISTING_EPIC_KEY:
        print(bold(f"\n── Using existing Epic: {EXISTING_EPIC_KEY} ──"))
        epic_key = EXISTING_EPIC_KEY
    else:
        print(bold("\n── Creating Epic ──"))
        epic_body_path = JIRA_DIR / EPIC_BODY_FILE
        if not epic_body_path.exists():
            sys.exit(red(f"  Epic body file not found: {epic_body_path}"))

        epic_key = create_issue(
            summary        = EPIC_SUMMARY,
            description_md = epic_body_path.read_text(encoding="utf-8"),
            issue_type     = EPIC_ISSUE_TYPE,
        )
        print(green(f"  {epic_key}  {EPIC_SUMMARY}"))
        _await_confirm(f"Epic {epic_key} — {EPIC_SUMMARY}", mapping)

    mapping["EPIC"] = epic_key

    # ── Stories + Subtasks ───────────────────────────────────────────────────
    for story in stories:
        print(bold(f"\n── Story {story['sa_id']} ──"))
        try:
            story_key = create_issue(
                summary        = story["summary"],
                description_md = story["body"],
                issue_type     = STORY_ISSUE_TYPE,
                parent_key     = epic_key,
            )
        except Exception as exc:
            save_mapping(mapping)
            sys.exit(red(f"  Failed to create story {story['sa_id']}: {exc}"))

        mapping[story["sa_id"]] = story_key
        print(green(f"  {story['sa_id']} → {story_key}  {story['summary']}"))
        _await_confirm(f"Story {story_key} — {story['summary']}", mapping)

        for task in story["tasks"]:
            try:
                task_key = create_issue(
                    summary        = task["summary"],
                    description_md = task["body"],
                    issue_type     = TASK_ISSUE_TYPE,
                    parent_key     = story_key,
                )
            except Exception as exc:
                save_mapping(mapping)
                sys.exit(red(f"  Failed to create task {task['sa_id']}: {exc}"))

            mapping[task["sa_id"]] = task_key
            print(f"    {task['sa_id']} → {task_key}  {task['summary']}")
            _await_confirm(f"Subtask {task_key} — {task['summary']}", mapping)

    return mapping


# ── Phase 3: save mapping file ────────────────────────────────────────────────

def save_mapping(mapping: dict[str, str]):
    """
    Write the placeholder → JIRA key mapping to the mapping file.

    In dry-run mode, prints the mapping that would be written without touching
    the filesystem.
    """
    if DRY_RUN:
        print(yellow(f"\n[dry-run] Would write: {MAPPING_FILE}"))
        print(json.dumps(mapping, indent=2))
        return

    MAPPING_FILE.write_text(json.dumps(mapping, indent=2), encoding="utf-8")
    print(green(f"\nMapping saved → {MAPPING_FILE}"))


# ── Phase 4: update all markdown files ────────────────────────────────────────

def update_markdown_files(mapping: dict[str, str]):
    """
    Replace placeholder IDs with real JIRA keys in every .md file under
    this directory (recursive).

    Replacement order: longest placeholder keys first.  This ensures task
    refs (e.g. SA-01-01) are replaced before their parent story ref
    (e.g. SA-01), preventing the shorter pattern from partially matching
    inside the longer one and producing a corrupted key.

    Word-boundary anchors (\\b) prevent false matches such as SA-011 being
    treated as SA-01 with a trailing digit.

    In dry-run mode, lists which files would be changed without writing them.
    """
    replacements = sorted(
        [(sa_id, jira_key) for sa_id, jira_key in mapping.items() if sa_id != "EPIC"],
        key=lambda pair: len(pair[0]),
        reverse=True,
    )

    updated = 0
    for md_file in sorted(JIRA_DIR.rglob("*.md")):
        original = md_file.read_text(encoding="utf-8")
        text = original
        for sa_id, jira_key in replacements:
            text = re.sub(rf"\b{re.escape(sa_id)}\b", jira_key, text)
        if text != original:
            if DRY_RUN:
                print(yellow(f"  [dry-run] Would update: {md_file.relative_to(JIRA_DIR)}"))
            else:
                md_file.write_text(text, encoding="utf-8")
                print(f"  Updated: {md_file.relative_to(JIRA_DIR)}")
            updated += 1

    print(green(f"\n{updated} file(s) updated."))


# ── Dry-run preview output ─────────────────────────────────────────────────────

def print_dry_run_preview(stories: list[dict]):
    """
    Print the full hierarchy that would be created, without making any API
    calls.  Useful for confirming that the parser found everything expected
    before committing to an import.
    """
    total_tasks = sum(len(s["tasks"]) for s in stories)
    epic_label = f"(reuse {EXISTING_EPIC_KEY})" if EXISTING_EPIC_KEY else "(new)"
    new_epics = 0 if EXISTING_EPIC_KEY else 1
    print(f"\n  Would create: {new_epics} Epic {epic_label} + {len(stories)} Stories + {total_tasks} Subtasks\n")
    print(f"  [{EPIC_ISSUE_TYPE}] {EXISTING_EPIC_KEY or EPIC_SUMMARY}")
    for story in stories:
        print(f"  └── [{STORY_ISSUE_TYPE}] {story['sa_id']}: {story['summary']}")
        for task in story["tasks"]:
            print(f"      └── [{TASK_ISSUE_TYPE}] {task['sa_id']}: {task['summary']}")


# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    # ── --update-only: skip straight to phase 4 ──────────────────────────────
    if UPDATE_ONLY:
        print(bold("Update-only mode — loading existing mapping and updating markdown files.\n"))
        if not MAPPING_FILE.exists():
            sys.exit(red(f"Mapping file not found: {MAPPING_FILE}\nRun the full import first."))
        mapping = json.loads(MAPPING_FILE.read_text(encoding="utf-8"))
        print(f"  Loaded {len(mapping) - 1} issue mappings from {MAPPING_FILE.name}")
        print(bold("\n── Phase 4: Updating markdown files ──"))
        update_markdown_files(mapping)
        return

    # ── Normal / dry-run flow ─────────────────────────────────────────────────
    if DRY_RUN:
        print(bold(yellow("DRY-RUN MODE — no API calls will be made, no files will be modified.\n")))
    else:
        print(bold(f"JIRA Cloud Import — {EPIC_SUMMARY}\n"))

        # Abort if a mapping file already exists to prevent duplicate issues.
        if MAPPING_FILE.exists() and not FORCE:
            sys.exit(
                red(
                    f"\nAborting: {MAPPING_FILE.name} already exists.\n"
                    "Running again would create duplicate issues in JIRA.\n\n"
                    "Options:\n"
                    "  --update-only   Re-run only the markdown file update (phase 4)\n"
                    "  --force         Overwrite the mapping and create new issues anyway\n"
                    "  Delete the mapping file manually if you want a clean re-import.\n"
                )
            )

        validate_connection()
        if EXISTING_EPIC_KEY:
            validate_existing_epic(EXISTING_EPIC_KEY)

    # Phase 1: parse
    print(bold("\n── Phase 1: Parsing JIRA/ directory ──"))
    stories = collect_items()
    total_tasks = sum(len(s["tasks"]) for s in stories)
    print(f"  Found {len(stories)} stories, {total_tasks} tasks")

    if DRY_RUN:
        print_dry_run_preview(stories)

        # Show which markdown files would be updated (uses placeholder keys).
        print(bold("\n── Phase 4 preview: Markdown files that would be updated ──"))
        dummy_mapping = {}
        for story in stories:
            dummy_mapping[story["sa_id"]] = f"{JIRA_PROJECT_KEY}-???"
            for task in story["tasks"]:
                dummy_mapping[task["sa_id"]] = f"{JIRA_PROJECT_KEY}-???"
        update_markdown_files(dummy_mapping)
        return

    # Phase 2: create
    print(bold("\n── Phase 2: Creating JIRA issues ──"))
    mapping = create_all_issues(stories)

    # Phase 3: save mapping
    print(bold("\n── Phase 3: Saving mapping file ──"))
    save_mapping(mapping)

    # Phase 4: update markdown
    print(bold("\n── Phase 4: Updating markdown files ──"))
    update_markdown_files(mapping)

    # Summary
    print(bold("\n══ Import complete ══"))
    print(f"  Epic:    {mapping['EPIC']}")
    print(f"  Stories: {len(stories)}")
    print(f"  Tasks:   {total_tasks}")
    print(f"  Mapping: {MAPPING_FILE}")
    print(f"\n  View project: {JIRA_URL}/jira/software/projects/{JIRA_PROJECT_KEY}/boards")


if __name__ == "__main__":
    main()
