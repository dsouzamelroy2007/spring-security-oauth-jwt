# Refresh Token Rotation

`[FEATURE B3]` `bff-client`'s registered client has `reuseRefreshTokens(false)`
(`RegisteredClientSeeder.bffClient()`) -- every refresh grant invalidates the
refresh token it consumed and issues a brand new one. A stolen, already-used
refresh token is worthless to an attacker the moment the legitimate client
refreshes first.

```mermaid
sequenceDiagram
    participant BFF as bff-client
    participant AS as authorization-server
    participant RS as resource-server

    Note over BFF: access_token is 15 minutes old, about to expire
    BFF->>RS: GET /api/v1/reports (Bearer <old access_token>)
    RS-->>BFF: 401 (expired)
    BFF->>AS: POST /oauth2/token<br/>grant_type=refresh_token<br/>refresh_token=<rt-1><br/>Basic bff-client:bff-client-secret
    AS->>AS: invalidate rt-1, mint rt-2 + new access_token
    AS-->>BFF: new access_token, refresh_token=rt-2, id_token
    BFF->>BFF: DefaultOAuth2AuthorizedClientManager persists rt-2 in session
    BFF->>RS: GET /api/v1/reports (Bearer <new access_token>)
    RS-->>BFF: 200

    Note over BFF,AS: If rt-1 is replayed after this point (stolen, race, retry)...
    BFF->>AS: POST /oauth2/token (grant_type=refresh_token, refresh_token=rt-1)
    AS-->>BFF: 400 invalid_grant -- rt-1 already consumed
```

The rotation itself is Boot's stock `DefaultOAuth2AuthorizedClientManager` --
no bespoke refresh code in this repo. The revoked/replayed rejection is
proven end to end by `integration-tests`' `CodeGrantEndToEndIT` against a
real, running authorization-server.

Related: `docs/flows/code-pkce-via-bff.md`.
