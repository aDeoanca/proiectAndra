# Current-user resolution and access enforcement

## What

- A security filter / interceptor that loads the session user into a request-scoped `CurrentUser` (or Spring Security context) for every `/api/**` call.
- A way for controllers to inject the current user (custom `@CurrentUser` argument resolver or Spring Security `@AuthenticationPrincipal`).
- Reject unauthenticated access to protected endpoints with `401` (allowlist: `/api/users`, `/api/auth/login`).
- This is the component that *supplies the actor* to the workflow service, which then enforces role-owns-state and no-self-approval (logic lives in Story 2; this wires the real user in).

## Notes

- Keep it minimal — Spring Security with a custom session filter, or a plain `OncePerRequestFilter`. No JWT, no method-level `@PreAuthorize` needed since the workflow service owns authorization.
- Anyone authenticated can create; role only gates reviewing.
- Depends on **POM-04-01** (session) and **POM-02-02** (the guards it feeds).

## Acceptance

- A request without a session to `/api/pos` → `401`.
- A controller receives the correct `CurrentUser` matching the logged-in session.
- A logged-in manager can approve a manager-stage PO they didn't create; the same call on their own PO → `403` (guard fed by the resolved user).
