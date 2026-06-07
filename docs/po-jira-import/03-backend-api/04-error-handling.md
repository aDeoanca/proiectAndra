# Global error handling

## What

- A `@RestControllerAdvice` that maps exceptions to the consistent body `{ "error": { "code", "message", "details?" } }`:
  - `ValidationException` / `MethodArgumentNotValidException` → `400` with per-field `details`.
  - `ForbiddenActionException` → `403`.
  - `EntityNotFound` / missing PO → `404`.
  - `IllegalTransitionException` → `409`.
  - Fallback `Exception` → `500` with a generic message (no stack traces leaked).
- Stable machine-readable `code` strings (e.g. `ILLEGAL_TRANSITION`, `FORBIDDEN`, `VALIDATION_FAILED`, `NOT_FOUND`).

## Notes

- This is what makes the workflow service's typed exceptions HTTP-correct without coupling the service to the web layer.
- The frontend (Story 5) keys off `code` for user-facing messages.
- Depends on **POM-02-02** (defines the exception types).

## Acceptance

- Each exception type produces its documented status and body shape.
- Validation errors include a `details` map of field → message.
- A `500` never exposes a stack trace or internal class names in the response.
