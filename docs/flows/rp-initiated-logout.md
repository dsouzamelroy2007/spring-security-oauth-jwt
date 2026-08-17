# RP-Initiated Logout

`[FEATURE B4]` Logging out of the SPA also ends the session at
authorization-server -- not just at `bff-client`. Without this, a browser
that still holds an authorization-server session cookie could silently
re-authenticate without a login prompt on the very next `/oauth2/authorize`
redirect.

```mermaid
sequenceDiagram
    participant Browser
    participant BFF as bff-client
    participant AS as authorization-server

    Browser->>BFF: POST /logout (CSRF token)
    BFF->>BFF: invalidate local session (Redis), clear SESSION cookie
    BFF-->>Browser: 302 to AS end_session_endpoint<br/>(OidcClientInitiatedLogoutSuccessHandler,<br/>id_token_hint + post_logout_redirect_uri)
    Browser->>AS: GET /connect/logout?id_token_hint=...&post_logout_redirect_uri=...
    AS->>AS: invalidate its own session
    AS-->>Browser: 302 to post_logout_redirect_uri<br/>(only if pre-registered -- RegisteredClientSeeder.bffClient())
    Browser->>BFF: GET / (fully logged out at both ends)
```

An unregistered `post_logout_redirect_uri` is silently ignored by Spring
Authorization Server, not rejected with an error -- `RegisteredClientSeeder`
registers `http://127.0.0.1:8080/` explicitly for exactly this reason.

Related: `docs/flows/code-pkce-via-bff.md`.
