# Purchase Order (PO) Management System

A full-stack web app that automates company procurement: create a Purchase Order, route it
through a **conditional approval workflow** (manager → IT → finance, with stages skipped
based on amount and category), and mark it invoiced. Rejected POs loop back to their creator
as *Needs Rework* and restart approval on resubmit.

- **Backend:** Spring Boot 3.3 / Java 21 / Maven
- **Frontend:** Next.js 15 / TypeScript / Tailwind + shadcn/ui
- **Database:** PostgreSQL 16

See [`docs/design.md`](./docs/design.md) for the full design and [`docs/adr/`](./docs/adr)
for the key decisions.

## Prerequisites

Install these before running the project:

| Tool | Version | Notes |
|------|---------|-------|
| **JDK** | 21 | e.g. Temurin/Adoptium. Required for the backend. |
| **Maven** | 3.9+ | Or use the bundled `./mvnw` wrapper. |
| **Node.js** | 20 LTS+ | Includes npm, for the frontend. |
| **Docker Desktop** | latest | Runs Postgres and the Testcontainers-based tests. |
| **Git** | latest | |

## Running locally

```bash
# 1. Start the database
docker-compose up -d            # PostgreSQL 16 on localhost:5432

# 2. Start the backend (applies Flyway migrations + seeds demo users)
cd backend
./mvnw spring-boot:run          # API on http://localhost:8080

# 3. Start the frontend (in a second terminal)
cd frontend
npm install
npm run dev                     # UI on http://localhost:3000
```

Open http://localhost:3000 and pick a seeded user to log in.

## Demo users

One seeded user per role (no real passwords — this is a minimal demo auth setup):

| User | Role | Can do |
|------|------|--------|
| alice | CREATOR | create & resubmit POs |
| bob | MANAGER | approve/reject at the manager stage |
| carol | IT_REP | validate IT Equipment POs |
| dave | FINANCE | final approval → invoiced |

Use the **switch user** affordance to walk a PO through the whole workflow.

## Demo script — walk a PO through the full workflow

After all three processes are running:

```bash
# Log in as Alice (creator) and create a PO that needs full approval
# URL: http://localhost:3000
# 1. Click "New PO" and submit:
#      Title:    "New Monitors"
#      Amount:   1200
#      Category: IT Equipment
# → Status becomes PENDING_MANAGER_APPROVAL

# 2. Switch user → bob (MANAGER)
#    Open the PO → click Approve
# → Status becomes PENDING_IT_VALIDATION

# 3. Switch user → carol (IT_REP)
#    Open the PO → click Approve
# → Status becomes PENDING_FINANCE_APPROVAL

# 4. Switch user → dave (FINANCE)
#    Open the PO → click Reject, enter comment "wrong budget code"
# → Status becomes NEEDS_REWORK

# 5. Switch user → alice (CREATOR)
#    Open the PO → edit amount to 900, click Resubmit
# → Status becomes PENDING_MANAGER_APPROVAL (workflow restarts)

# 6. Switch user → bob → Approve
# 7. Switch user → carol → Approve
# 8. Switch user → dave → Approve
# → Status becomes INVOICED
```

Manager-bypass shortcut: create a PO with **amount < 100** (any category except
IT Equipment) — it skips the Manager and IT stages and lands directly at
`PENDING_FINANCE_APPROVAL`.

## Testing

```bash
cd backend && ./mvnw test       # unit + integration (needs Docker for Testcontainers)
cd frontend && npm test         # Zod schema validation smoke tests
```

## Troubleshooting

| Problem | Fix |
|---------|-----|
| **Backend won't start — port 5432 in use** | `docker-compose down` then `docker-compose up -d` |
| **Flyway migration failure on fresh DB** | The first `spring-boot:run` applies all migrations automatically. If you see a checksum error from a partial previous run, drop the `public` schema and restart: `docker exec -it po_postgres psql -U po -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"` |
| **401 on every API call after login** | The frontend must be served from `localhost:3000` and the backend from `localhost:8080`. Cookies are `SameSite=Lax` — cross-origin requests require the `credentials: 'include'` flag (already set in `frontend/lib/api.ts`). Check that your browser isn't blocking third-party cookies. |
| **CORS error in browser console** | Ensure the backend CORS config (`WebConfig`) allows `http://localhost:3000`. If you changed the frontend port, update `ALLOWED_ORIGINS` in `WebConfig.java`. |
| **Testcontainers / backend tests fail — no Docker** | Backend integration tests use Testcontainers, which requires a running Docker daemon. Start Docker Desktop before running `./mvnw test`. |
| **`npm test` fails — module not found** | Run `npm install` inside the `frontend/` directory first. |

## Project layout

```
backend/            Spring Boot app (Maven)
frontend/           Next.js app
docker-compose.yml  Postgres
docs/
  design.md         full design document
  adr/              architecture decision records
  req.txt           original requirements
CLAUDE.md           operating manual / invariants for contributors & AI agents
```
