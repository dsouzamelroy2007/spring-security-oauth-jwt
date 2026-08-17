# 0007. No Dynamic Client Registration (RFC 7591)

## Status

Accepted -- out of scope.

## Context

RFC 7591 lets a client register itself with the authorization server at
runtime via an API call, instead of an operator pre-registering it.
`authorization-server` already persists registered clients in JDBC
(`[FEATURE B9]`, `JdbcRegisteredClientRepository`), which is most of the
storage-layer work dynamic registration would also need.

## Decision

Not implemented. This repo has exactly two clients (`bff-client`,
`export-worker`), both known at build time and seeded idempotently by
`RegisteredClientSeeder`. Dynamic registration solves a problem -- clients
appearing that the operator didn't anticipate -- that doesn't exist here, and
adding a self-registration API to a demo authorization server would need its
own authorization story (who's allowed to register a client?) that would be
pure scaffolding for a feature nothing in this repo exercises.

## Consequences

- Clients are added by writing a new factory method on
  `RegisteredClientSeeder`, not by calling an API -- appropriate for a system
  with a small, known set of first-party clients.
- `[FEATURE B9]`'s JDBC persistence already proves the storage half of what
  dynamic registration would need; only the self-service registration
  endpoint itself is the part being cut.
