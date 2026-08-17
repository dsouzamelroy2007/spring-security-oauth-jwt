# Client Credentials (M2M)

`[FEATURE B2]` No end user, no browser, no redirect. A worker process
authenticates as itself and calls the API directly.

```mermaid
sequenceDiagram
    participant Worker as export-worker
    participant AS as authorization-server
    participant RS as resource-server

    Worker->>AS: POST /oauth2/token<br/>grant_type=client_credentials<br/>scope=expenses.export<br/>Basic export-worker:export-worker-secret
    AS-->>Worker: access_token (SCOPE_expenses.export, no org claim)
    Worker->>RS: GET /api/v1/reports/export<br/>Authorization: Bearer <access_token>
    RS->>RS: @PreAuthorize("hasAuthority('SCOPE_expenses.export')")
    RS-->>Worker: 200 report export (all tenants -- not org-scoped)
```

The access token carries no `org` claim: `OrgRoleClaimsTokenCustomizer` only
stamps `org`/`roles` when the token's principal is an `AppUserPrincipal`
(a human), which client_credentials never has. The export endpoint is
deliberately the one place in the API that is *not* tenant-scoped -- an
export job runs across every organisation by design.

Related: `docs/decisions/0003-no-password-grant.md`.
