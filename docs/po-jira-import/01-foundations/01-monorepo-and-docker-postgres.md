# Monorepo scaffold and Dockerised Postgres

## What

- Create the monorepo layout: `backend/` (Spring Boot 3.3, Java 21, Maven), `frontend/` (placeholder for Story 5), `docker-compose.yml`, root `.gitignore`.
- `backend/` generated from Spring Initializr with: Spring Web, Spring Data JPA, Flyway, Validation, PostgreSQL driver, Lombok. Maven wrapper (`./mvnw`) committed.
- `docker-compose.yml` — one `postgres:16` service, named volume, healthcheck, env for DB name/user/password.
- `backend/src/main/resources/application.yml` — datasource pointing at the compose Postgres, `spring.jpa.hibernate.ddl-auto: validate` (schema owned by Flyway), Flyway enabled.

## Notes

- `ddl-auto: validate` (not `update`) is deliberate — schema changes go through Flyway migrations only. See `../../CLAUDE.md` invariant on migrations.
- Keep credentials in env vars / compose defaults; no secrets committed.

## Acceptance

- `docker-compose up -d` brings up a healthy Postgres 16 container.
- `cd backend && ./mvnw spring-boot:run` starts and connects to it with no schema errors.
- `./mvnw test` runs (even with zero real tests yet) and the build is green.
