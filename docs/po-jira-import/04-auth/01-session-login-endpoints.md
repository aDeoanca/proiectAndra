# Session login endpoints and user picker

## What

- `GET /api/users` — list seeded users (`id`, `name`, `role`) for the login/switch-user picker. Unauthenticated-accessible (it's the picker).
- `POST /api/auth/login` — body `{ userId }`; validates the user exists, stores the user id in the server-side `HttpSession`, returns the user. Sets an HTTP-only session cookie.
- `POST /api/auth/logout` — invalidates the session.
- `GET /api/auth/me` — returns the current user from the session, or `401`.

## Notes

- No passwords (or a single trivial shared one) — this is demo auth per ADR-0003.
- Session is server-side (`HttpSession`); cookie is `HttpOnly`, `SameSite=Lax`. Configure CORS to allow credentials from the Next.js origin.
- Depends on **POM-6** (seeded users exist).

## Acceptance

- `GET /api/users` returns the four seeded users.
- Login with a valid `userId` sets a session cookie; `GET /api/auth/me` then returns that user.
- After logout, `GET /api/auth/me` → `401`.
