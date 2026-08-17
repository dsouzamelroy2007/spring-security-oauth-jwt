# 0010. No rate limiting / brute-force lockout

## Status

Accepted -- out of scope.

## Context

Rate limiting login attempts (or any endpoint) is a standard production
hardening measure, and `docker-compose.yml` already runs Redis, which is the
obvious backing store for a token-bucket or sliding-window limiter.

## Decision

Not implemented. Rate limiting is not a Spring Security concept -- there is
no framework primitive for it the way there is for authentication or
authorization, so this feature would be entirely bespoke application code
(a Redis Lua script or `RedisTemplate` counter logic, a filter to enforce
it, a policy for what counts as "one client"). That's real, valuable
production hardening, but it doesn't teach anything about the Spring
Security surface this repo exists to demonstrate, and the actual security
audit trail for failed attempts is already covered by `[FEATURE D8]`'s audit
events (`login_failure` rows).

## Consequences

- Repeated failed logins are recorded (`FormLoginFailureHandler` ->
  `login_failure` audit rows) but not throttled or locked out.
- A real deployment of this stack would need a rate limiter in front of
  `authorization-server`'s `/login` and `/oauth2/token` endpoints before
  going to production -- explicitly flagged here rather than silently
  assumed away.
