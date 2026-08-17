# 0008. No RFC 8693 token exchange

## Status

Accepted -- out of scope.

## Context

Token exchange lets one service trade a token it holds for a new token
scoped to call a *different* downstream service on a user's behalf --
the standard shape for "service A calls service B calls service C" chains
where each hop needs its own appropriately-scoped credential.

## Decision

Not implemented. This repo's only service-to-service hop was
`bff-client -> resource-server`, and that's already solved by plain token
relay (`RelayClientConfig`'s `OAuth2ClientHttpRequestInterceptor`) --
`bff-client` already holds a token scoped correctly for `resource-server`
and simply forwards it, no exchange needed. Token exchange only earns its
complexity when there's a *third* service in the chain with its own,
narrower scope requirements, and no such service exists in this topology.

## Consequences

- Service-to-service auth here is "relay the token you already have,"
  which is simpler and sufficient for a two-hop system.
- A future third service (say, a dedicated notifications worker called by
  `resource-server` on a user's behalf) would be the natural point to
  revisit this and actually need RFC 8693.
