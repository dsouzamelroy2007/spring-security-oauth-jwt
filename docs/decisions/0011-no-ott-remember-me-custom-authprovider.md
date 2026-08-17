# 0011. No One-Time-Token magic link, remember-me, or a bespoke `AuthenticationProvider`

## Status

Accepted -- out of scope.

## Context

Three smaller authentication mechanisms were considered alongside
`[FEATURE A1]`-`[FEATURE A5]`: magic-link (One-Time-Token) login,
remember-me (persistent login cookies), and a fully custom
`AuthenticationProvider` written from scratch rather than composed from
Spring Security's own building blocks (`DaoAuthenticationProvider`, the
pre-authenticated filter pattern).

## Decision

Not implemented, as a group. Each is roughly half a day of real work for a
narrow slice of the same underlying story `[FEATURE A1]`-`[FEATURE A5]`
already tell in full: A1 covers form login end to end (custom success/failure
handlers), A2 covers a second mechanism on its own ordered chain, A3 covers
swapping the `UserDetailsService` implementation, A4 covers password
encoding migration, and A5 covers a fully custom, framework-bypassing
authentication filter (`addFilterBefore`, no DSL entry point) -- which is
already the harder version of "write a custom `AuthenticationProvider`."
Adding magic links or remember-me cookies on top would be three more
mechanisms demonstrating the same `AuthenticationManager`/
`AuthenticationProvider` composition pattern a fifth, sixth, and seventh
time.

## Consequences

- Five authentication mechanisms (A1-A5) is the deliberate ceiling for this
  repo -- broad enough to show the `AuthenticationManager` abstraction from
  several angles without diminishing returns.
- A reader wanting remember-me specifically can look at `[FEATURE D4]`'s
  session configuration (`bff-client`'s `BffSecurityConfig`) as the nearest
  existing analogue -- persistent authentication is already a solved problem
  in this repo, just via a long-lived session rather than a remember-me
  cookie.
