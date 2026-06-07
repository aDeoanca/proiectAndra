# Next.js scaffold, API client, and query setup

## What

- Scaffold `frontend/` — Next.js 15 (App Router), TypeScript, Tailwind, shadcn/ui initialised.
- A typed API client (fetch wrapper) with the backend base URL, **credentials included** (sends the session cookie), and centralised error parsing of the `{ error: { code, message } }` shape.
- TanStack Query provider at the app root; query keys for POs, PO detail, current user, users list.
- An auth/session context: on load, call `GET /api/auth/me`; expose `currentUser` + login/logout/switch-user helpers; redirect unauthenticated users to the login page.
- Shared TypeScript types/enums mirroring the backend (`Status`, `Category`, `Role`) and a Zod schema module reused by forms.

## Notes

- `fetch` must use `credentials: 'include'`; configure the backend CORS to allow the frontend origin with credentials (pairs with POM-04-01).
- Keep enum string values identical to the backend.
- Depends on **POM-03** (contract) and **POM-04-01** (auth endpoints).

## Acceptance

- The app builds and runs (`npm run dev`) and talks to the backend with the session cookie attached.
- An unauthenticated visit redirects to login; an authenticated one resolves `currentUser`.
- API errors surface their `code`/`message` to the caller.
