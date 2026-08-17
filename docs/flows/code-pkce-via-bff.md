# Authorization Code + PKCE, via the BFF

`[FEATURE B1]` The browser never sees a token. `bff-client` runs the entire
OAuth2 dance server-side and hands the browser only an `HttpOnly` session
cookie.

```mermaid
sequenceDiagram
    participant Browser
    participant BFF as bff-client
    participant AS as authorization-server
    participant RS as resource-server

    Browser->>BFF: GET /
    BFF-->>Browser: index.html + app.js (unauthenticated)
    Browser->>BFF: fetch /whoami
    BFF-->>Browser: 401 (no session yet)
    Browser->>BFF: GET /oauth2/authorization/bff-client
    BFF-->>Browser: 302 to AS /oauth2/authorize<br/>+ code_challenge (S256), state, nonce
    Browser->>AS: GET /oauth2/authorize?...
    AS-->>Browser: 302 to /login (no session at AS)
    Browser->>AS: POST /login (username, password, csrf)
    AS-->>Browser: 302 back to /oauth2/authorize
    AS-->>Browser: 302 to /oauth2/consent (first grant only)
    Browser->>AS: POST /oauth2/consent (approve scopes)
    AS-->>Browser: 302 to BFF redirect_uri?code=...&state=...
    Browser->>BFF: GET /login/oauth2/code/bff-client?code=...
    BFF->>AS: POST /oauth2/token<br/>code + code_verifier + client Basic auth
    AS-->>BFF: access_token, refresh_token, id_token
    BFF->>BFF: store tokens in Redis-backed session
    BFF-->>Browser: 302 to / + Set-Cookie: SESSION (HttpOnly, SameSite=Lax)
    Browser->>BFF: fetch /whoami
    BFF-->>Browser: 200 (session cookie only, no token exposed)
    Browser->>BFF: fetch /api/reports
    BFF->>RS: GET /api/v1/reports<br/>Authorization: Bearer <access_token>
    RS-->>BFF: 200 report list
    BFF-->>Browser: 200 report list
```

Related: `docs/decisions/0001-why-bff.md`, `docs/flows/refresh-rotation.md`,
`docs/flows/rp-initiated-logout.md`.
