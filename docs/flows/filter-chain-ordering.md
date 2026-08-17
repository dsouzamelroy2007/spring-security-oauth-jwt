# Security Filter Chain Ordering

`[FEATURE D5]` Every module but `resource-server` needs more than one
`SecurityFilterChain` bean, because different URL spaces in the same app need
fundamentally different treatment (JWT bearer vs. session vs. Basic vs.
API-key). Spring Security tries chains in ascending `@Order`, first match on
`securityMatcher` wins -- so the narrowest chains must come first, or a
broader catch-all further down would shadow them.

```mermaid
flowchart TD
    subgraph AS["authorization-server"]
        direction TB
        AS1["@Order(1) AuthorizationServerConfig<br/>securityMatcher: SAS endpoints only<br/>(/oauth2/authorize, /oauth2/token, /oauth2/jwks, /userinfo, ...)<br/>anyRequest().authenticated()"]
        AS2["@Order(2) FormLoginSecurityConfig<br/>everything else: /login, /oauth2/consent, /actuator/health<br/>formLogin() + two DaoAuthenticationProviders (JPA, in-memory)"]
        AS1 -.->|"no match, falls through"| AS2
    end

    subgraph BFF["bff-client"]
        direction TB
        B0["@Order(0) ActuatorSecurityConfig<br/>separate child context (management.server.port)<br/>securityMatcher: EndpointRequest.toAnyEndpoint()<br/>httpBasic(), ROLE_ACTUATOR"]
        B1["@Order(1) BasicAuthSecurityConfig<br/>securityMatcher: /internal/basic/**<br/>httpBasic(), ROLE_OPS, stateless"]
        B2["@Order(2) ApiKeySecurityConfig<br/>securityMatcher: /internal/api-key/**<br/>addFilterBefore(ApiKeyAuthenticationFilter), stateless"]
        B3["@Order(3) BffSecurityConfig<br/>no securityMatcher -- catch-all<br/>oauth2Login(), CSRF, CORS, headers, session mgmt"]
        B0 -.-> B1 -.-> B2 -.-> B3
    end

    subgraph RS["resource-server"]
        direction TB
        R1["SecurityConfig -- the only chain<br/>no @Order needed: nothing else to shadow or be shadowed by<br/>stateless, oauth2ResourceServer(jwt), CSRF disabled"]
    end
```

Two contexts, one chain list: `bff-client`'s management child context and its
main application context are resolved *ancestor-inclusively* by Spring
Security's `WebSecurityConfiguration` -- `ActuatorSecurityConfig`'s chain in
the child context is still visible from, and can shadow or be shadowed by,
the three chains declared in the parent. `@Order(0)` keeps it ahead of
`BffSecurityConfig`'s any-request catch-all regardless of which context
"wins" visibility.

Related: `docs/decisions/0001-why-bff.md`.
