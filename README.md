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

> **Status:** Design complete; application code is not scaffolded yet. The setup below
> describes the intended workflow once `backend/` and `frontend/` exist.

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

## Testing

```bash
cd backend && ./mvnw test       # unit + integration (needs Docker for Testcontainers)
cd frontend && npm test         # light component/validation smoke tests
```

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
