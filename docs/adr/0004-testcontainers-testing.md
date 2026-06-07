# ADR-0004: Backend-heavy testing with Testcontainers Postgres

**Status:** Accepted — 2026-06-07

## Context

The conditional routing and workflow guards are the core risk. Test DB options: H2 (fast,
but dialect drift from Postgres) vs. Testcontainers Postgres (real engine). Frontend test
depth also needed bounding.

## Decision

- **Backend, thorough:** pure unit tests on the routing function (every bypass combo + the
  `100.00` boundary), workflow service guard tests, and `@SpringBootTest` integration tests
  for the happy paths + full reject→rework→resubmit loop.
- **Test DB: Testcontainers Postgres** (not H2) — matches Postgres 16 and exercises Flyway.
- **Frontend, light:** manual demo verification + optionally one Vitest/RTL smoke test on
  the create form's Zod validation.

## Consequences

- **+** No Postgres/H2 dialect surprises; migrations validated in CI.
- **+** Test effort concentrated where the logic risk is.
- **−** Testcontainers needs a Docker daemon available in CI/dev.
- **−** Frontend regressions rely on manual checking; acceptable at this scope.
