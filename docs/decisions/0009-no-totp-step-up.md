# 0009. No TOTP / step-up authentication

## Status

Accepted -- out of scope.

## Context

A second factor (TOTP codes, or step-up re-authentication before a sensitive
action like approving a large reimbursement) is a common, realistic addition
to a login flow, and would sit naturally alongside `[FEATURE A1]`'s form
login.

## Decision

Not implemented. A TOTP flow needs its own enrolment UI (QR code generation,
secret storage, backup codes) and its own test story (a deterministic way to
generate a valid code in CI) -- a half-day of real plumbing whose lesson is
"add another step to the login form," a skill `[FEATURE C4]`'s custom
authorization checks already demonstrate a more interesting version of. Step-
up auth specifically would also need a policy for *which* actions require it,
which this domain has no natural candidate for beyond "approve a report,"
already gated by `[FEATURE C2]`'s role hierarchy.

## Consequences

- Login remains single-factor (password, upgraded bcrypt-to-argon2 on
  successful login -- `[FEATURE A4]`) for every seeded user.
- A reader wanting to see step-up auth would need to add a second
  `AuthenticationProvider` in the chain and a policy for triggering it --
  the existing `FormLoginSecurityConfig`'s multi-provider chain
  (`[FEATURE A3]`) is the natural place that pattern would extend from.
