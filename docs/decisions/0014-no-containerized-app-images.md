# 0014. No containerized app images (yet)

## Status

Accepted -- deferred, this session's own scoping decision for M5.

## Context

`docker-compose.yml` runs Postgres and Redis but none of the three
application modules; `make up` was extended in M5 to build and launch them
as local background JVM processes instead of containers
(`authorization-server`, `resource-server`, `bff-client`).

Each app's issuer/audience/discovery configuration is baked to a fixed
`localhost` address: `authorization-server`'s issuer (`http://localhost:9000`)
is stamped into every JWT's `iss` claim; `resource-server`'s
`JwtDecoderConfig` uses the *same* `issuer-uri` value for both OIDC discovery
(an outbound HTTP call) and inbound `iss`-claim string validation, with no
split between the two. Containerizing all three would put the browser,
`resource-server`'s discovery call, and the literal `iss` string each
resolving `authorization-server` through a different address (a
container-network hostname for service-to-service calls, `localhost` or
`host.docker.internal`/`host-gateway` for the browser) -- and unless every
one of those addresses is textually identical, JWT validation fails, or
discovery fails, or the browser's redirect breaks.

## Decision

Not solved in this session. This is a real, well-known category of problem
(host vs. container vs. browser addressing for a callback-heavy protocol),
not a shortcut -- solving it properly needs either a single consistent
hostname reachable from all three contexts (a `/etc/hosts` entry or a
compose network alias with matching browser-side DNS) or splitting
`resource-server`'s discovery address from its `iss` validation address, and
either change deserves its own focused session rather than a rushed addition
to an already-large milestone.

## Consequences

- `make up` runs the real system as local JVM processes, which satisfies
  "one command, zero manual steps" without solving the addressing problem at
  all -- sidestepping it rather than fixing it.
- CI's Trivy step scans the filesystem/dependencies (`trivy fs`), not
  container images, since none exist yet.
- Adding Dockerfiles for the three app modules and fixing the issuer/discovery
  split in `JwtDecoderConfig` is the natural next piece of work, and now has
  a clean, isolated starting point instead of being buried inside M5.
