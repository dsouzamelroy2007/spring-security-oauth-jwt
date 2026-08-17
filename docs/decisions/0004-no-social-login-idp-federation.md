# 0004. No social login / external IdP federation

## Status

Accepted -- out of scope.

## Context

Federating `authorization-server` with Google, GitHub, or another external
IdP would be a natural OIDC feature to demonstrate, and Spring Security's
OAuth2 client support makes the client side of it straightforward.

## Decision

Not implemented. Every external IdP requires a real, registered OAuth
application and real client credentials obtained from that provider's
console. That breaks two of this repo's hard constraints at once: it can no
longer run fully offline from a fresh clone (`CLAUDE.md` rule 7), and CI has
no way to exercise it without checking a real secret into a public repo or
depending on an external network call that isn't this project's own stack.

## Consequences

- `authorization-server` is the only IdP in this system, which is also more
  honest about what the repo is actually demonstrating: being an OIDC
  provider, not consuming one.
- A reader wanting to see federation would need to register their own OAuth
  app with a provider and wire in the client registration themselves --
  straightforward given Spring Security's existing support, just outside
  what this repo can ship runnable by default.
