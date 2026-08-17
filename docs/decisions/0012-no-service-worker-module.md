# 0012. No dedicated `service-worker` module

## Status

Accepted -- out of scope.

## Context

A sixth Maven module and compose service was considered, purely to run a
background job on a schedule and call `resource-server`'s export endpoint as
a demonstration of `[FEATURE B2]`'s client_credentials grant "for real,"
outside of a test.

## Decision

Not implemented. `[FEATURE B2]` is already fully proven: `export-worker` is a
seeded, real registered client, `ReportExportScopeTest` exercises it at the
resource-server layer, and `integration-tests`' `RealTokenAgainstResourceServerIT`
exercises it end to end against two real, running processes. A whole extra
module -- its own POM, its own Docker Compose service, its own scheduling
concern -- would exist only to wrap that same, already-proven call in a cron
job. `demo.http` (repo root) documents the same request for anyone who wants
to run it manually.

## Consequences

- `client_credentials` is proven by tests and a documented manual request,
  not by a standing service. Anyone wanting an actual scheduled worker has a
  fully working client_credentials example to build it from.
- The module count stays at five, matching `SPEC.md`'s architecture table --
  no module exists solely to demonstrate a grant type already covered
  elsewhere.
