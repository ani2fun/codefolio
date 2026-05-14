# Endpoint wiring helpers make the error contract unforgettable

Every fallible API endpoint must do the same two things: attach the uniform `(StatusCode, ApiError)` error
output, and map its pipeline's `HandlerFailure` to HTTP via `ApiErrors.toHttp`. Until this ADR, both the
server (`ApiRoutes`) and the client (`ApiClient`) repeated this wiring by hand, per endpoint — ~8 endpoints
each. Forgetting the server's `errorOut` pairing turns a failure into a bodyless 500; the onboarding book
lists this as a known mistake. Nothing enforced the pattern — it was a formula a newcomer reverse-engineered.

**Decision**: the wiring is a helper on each side of the wire. Server-side, `ApiRoutes.handlerEndpoint` takes
a derived endpoint plus logic typed `I => IO[HandlerFailure, O]` and *always* applies `errorOut` +
`mapError(toHttp)` — the only way to produce a fallible `ZServerEndpoint` here is through the helper, so an
endpoint that compiles is an endpoint wired correctly. Client-side, `ApiClient.callable` turns a generated
endpoint into an `I => Future[O]`, building the sttp request once and folding a non-2xx `Left` into a failed
`Future` with a human-readable message. Adding an endpoint is one `handlerEndpoint` / `callable` line plus a
one-line method, not a multi-line formula.

The trade is two small private helpers for **compile-time enforcement of the transport policy**: the error
plumbing can't be forgotten because it isn't written per-endpoint at all. `healthEndpoint` stays on the
generated endpoint directly — it never fails (`UIO[HealthStatus]`; degradation is part of the success
body), so it has no error channel to wire.
