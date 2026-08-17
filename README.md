# Spring Security 7 Reference Application — Expense Tracker

A multi-module Spring Boot reference application demonstrating Spring Security 7:
OAuth2, OpenID Connect and JWT, with a small Expense Tracker REST API as the
business domain.

*Personal learning project. Not affiliated with or endorsed by any employer.*

**Status:** M3 in progress — authorization-server (OIDC provider) and resource-server
(Expense Tracker API, full authorization matrix) are functional. bff-client and the
SPA are still M1 skeleton. Full architecture docs and walkthroughs land in M5.

## Modules

| Module | Role |
|---|---|
| `shared-security` | Library. Reusable auto-configuration: RFC 9457 errors, JWT converters, audit, org resolution. No Boot repackaging. |
| `authorization-server` | OIDC Provider. The only module that authenticates humans. |
| `resource-server` | The Expense Tracker API. The main showcase. |
| `bff-client` | Server-side OAuth2 client. Holds tokens in a Redis session, serves the static SPA, exposes only an HttpOnly cookie to the browser. |
| `integration-tests` | Cross-module black-box tests. |

## Prerequisites

- Java 21
- Maven 3.9+
- Docker (for Postgres and Redis)

## Quick start

```bash
mvn -q validate                 # cheapest check
docker compose up -d            # postgres + redis
mvn -q verify                   # build + test all modules
```

Each application module exposes `GET /actuator/health` once started.

## License

MIT — see [LICENSE](LICENSE).
