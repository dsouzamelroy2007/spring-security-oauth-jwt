# 0005. No passkeys / WebAuthn

## Status

Accepted -- out of scope.

## Context

WebAuthn is the modern, phishing-resistant answer to password login and
would be a strong addition to `authorization-server`'s authentication
mechanisms.

## Decision

Not implemented. WebAuthn's ceremony requires a physical or platform
authenticator (a security key, Touch ID, Windows Hello) to actually
demonstrate, and most browsers restrict it to secure contexts (HTTPS, or
`localhost` with caveats) -- neither of which fits "clone the repo, run one
command, see it work" without the reader's own hardware and manual
interaction. It also has no meaningful automated-test story: there is no way
for CI to "press the security key."

## Consequences

- Login stays password-based (`[FEATURE A1]`, `[FEATURE A4]`), which is also
  what the rest of the authentication mechanism list (A2, A3, A5) is built
  to compare against.
- A reader wanting to see WebAuthn would need a browser + authenticator
  session no CI runner can reproduce -- the honest reason this is a cut, not
  a lesser-priority TODO.
