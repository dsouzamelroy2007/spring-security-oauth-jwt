# 0001. Backend-for-Frontend, not a token-holding SPA

## Status

Accepted.

## Context

The SPA needs to call an OAuth2/OIDC-protected API. The two mainstream shapes
for this are: (a) the SPA itself runs the code+PKCE flow (a "public client")
and holds the access token in memory or `sessionStorage`, calling the
resource server directly; or (b) a server-side component runs the flow on
the SPA's behalf and the browser only ever holds an opaque session cookie.

Option (a) means the access token exists somewhere JavaScript can reach it.
Any XSS on the page -- a single vulnerable dependency, a markdown renderer
that doesn't escape correctly, a future contributor's mistake -- can exfiltrate
the token directly. `sessionStorage`/`localStorage` are both readable by any
script running on the page; there is no way to mark them `HttpOnly` the way
a cookie can be.

## Decision

`bff-client` is a confidential OAuth2 client that runs the entire
authorization_code + PKCE flow server-side, holds tokens in a Redis-backed
session, and relays them to `resource-server` via a declarative
`@HttpExchange` client. The browser receives only an `HttpOnly`,
`SameSite=Lax` session cookie -- there is no code path in `app.js` that could
read a token even if it wanted to, because no token is ever sent to it.

This also means the SPA is same-origin with its own backend (`bff-client`
serves both the static files and the API it calls), which sidesteps CORS
entirely for the app's own traffic -- CORS in this repo exists only to
demonstrate the one genuinely cross-origin case (`[FEATURE D1]`), not because
the SPA needs it for its own operation.

## Consequences

- One more moving part than a pure SPA: `bff-client` is a real server
  process with its own session store (Redis) and its own deployment.
- The BFF becomes a single point that must stay available for the SPA to
  work at all -- a pure SPA could, in principle, talk to a resource server
  directly if the BFF were ever down.
- Token relay, refresh rotation, and logout all become the BFF's
  responsibility rather than the browser's, which is exactly the point: it
  moves the entire OAuth2 client role off a platform (the browser) that
  cannot keep a secret.

See `docs/flows/code-pkce-via-bff.md` for the full sequence and
`docs/flows/filter-chain-ordering.md` for how `bff-client`'s own filter
chains are organised.
