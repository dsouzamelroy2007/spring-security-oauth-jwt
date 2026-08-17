# 0002. Self-contained JWTs, not opaque tokens + introspection

## Status

Accepted.

## Context

OAuth2 access tokens can be self-contained (a signed JWT the resource server
can verify locally) or opaque (a random string the resource server must call
back to the authorization server to introspect, per RFC 7662, on every
request). Both are legitimate, widely-used designs.

Opaque tokens have one real advantage this project cannot claim to need:
instant, server-side revocation. Revoking a JWT before its `exp` is only
possible with an extra mechanism (a deny-list, short TTLs plus refresh) --
`resource-server` has no such mechanism here, deliberately (see below).
Opaque tokens also keep claims off the wire entirely, which matters more for
third-party or public clients than for this repo's fully-owned two-service
topology.

## Decision

Access tokens are RSA-signed JWTs (`[FEATURE B5]`), validated locally by
`resource-server`'s `JwtDecoder` against issuer, audience, and signature
(`[FEATURE B8]`) -- no network call back to authorization-server on the
request path. `resource-server` is intentionally stateless and JWT-only,
which is also why it owns no `UserDetailsService` and no login page (see the
architectural invariant in `CLAUDE.md`).

The tradeoff is made explicit rather than hidden: a compromised or
inappropriately-scoped JWT remains valid until it expires (15 minutes for
`bff-client`'s tokens -- see `RegisteredClientSeeder.bffClient()`). Refresh
token rotation (`[FEATURE B3]`, `docs/flows/refresh-rotation.md`) bounds how
long a stolen *refresh* token is useful, and `/oauth2/revoke`
(exercised by `integration-tests`' `CodeGrantEndToEndIT`) lets a client
proactively kill a refresh token, but there is no fast-revocation path for an
already-issued access token itself.

## Consequences

- Every request to `resource-server` is verified with zero extra network
  hops or shared state -- horizontally scalable without a session store,
  and the actual mechanism this repo sets out to demonstrate.
- Short access-token TTLs are the mitigation for the "can't revoke early"
  gap, not a workaround for a missing feature -- 15 minutes is a deliberate,
  visible choice in `RegisteredClientSeeder`, not a default left untouched.
- Introspection-per-request (opaque tokens) would have added real latency
  and a hard runtime dependency from `resource-server` back to
  `authorization-server` for every single API call, which JWTs avoid by
  design.
