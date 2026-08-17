# 0003. No OAuth2 password grant, ever

## Status

Accepted.

## Context

The Resource Owner Password Credentials grant (RFC 6749 §4.3) lets a client
collect a user's username and password directly and exchange them for a
token, with no redirect to the authorization server at all. It looks
appealingly simple for a first-party client like `bff-client` and would let
the demo login screen live in the SPA itself instead of on
`authorization-server`.

It was formally deprecated in OAuth 2.1 and its IETF-recommended replacement
guidance is unambiguous: don't implement it in new software. It trains users
to type their password into any client that asks, defeats the entire point
of centralizing authentication at one identity provider, and is incompatible
with anything the authorization server might want to add later (MFA, consent,
step-up auth) since the client never sees that flow at all -- it only ever
sees a username and a password.

## Decision

This repo does not implement the password grant, in any module, for any
client. `bff-client` uses authorization_code + PKCE
(`docs/flows/code-pkce-via-bff.md`) even though it is a first-party,
fully-trusted client -- specifically to demonstrate that the "trusted first
party" argument for password grant doesn't actually hold up. `export-worker`
uses client_credentials (`docs/flows/client-credentials.md`), the correct
grant for a client acting as itself with no end user at all.

Spring Security 7 makes this decision easy to keep: password grant support
was removed from the specification support entirely (see `CLAUDE.md`'s API
constraints) -- there is no lambda-DSL path to it even if a future change
tried to add one back in by habit.

## Consequences

- Every login, including `bff-client`'s own, goes through
  `authorization-server`'s real login page -- one login UI, one place that
  ever sees a raw password, matching the architectural invariant that only
  `authorization-server` authenticates humans.
- No client anywhere in this repo, including future ones, can shortcut
  straight to a token with just a username/password pair.
