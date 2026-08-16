# CLAUDE.md â€” spring-security-oauth-jwt

Personal portfolio project. Public GitHub repo. Optimise for **readable, idiomatic,
verifiable** code over breadth or cleverness.

## What this is

A multi-module Spring Boot reference application demonstrating Spring Security 7:
OAuth2, OpenID Connect and JWT, with a small **Expense Tracker** REST API as the
business domain.

- Full specification: `.claude/rules/SPEC.md`
- Milestones + acceptance criteria: `.claude/rules/MILESTONES.md`
- Progress / handoff notes: `.claude/rules/STATE.md`  â† read first, update last

## Modules

| Module | Role |
|---|---|
| `shared-security` | Library. Reusable auto-configuration: RFC 9457 errors, JWT converters, audit, org resolution. No Boot repackaging. |
| `authorization-server` | OIDC Provider. The only module that authenticates humans. |
| `resource-server` | The Expense Tracker API. The main showcase. |
| `bff-client` | Server-side OAuth2 client. Holds tokens in a Redis session, serves the static SPA, exposes only an HttpOnly cookie to the browser. |
| `integration-tests` | Cross-module black-box tests. |

Architectural invariant: the resource server owns no users, no `UserDetailsService`,
no login page. If that appears there, the architecture has been misunderstood.

## Commands

```bash
mvn -q validate                             # cheapest check; run after ANY pom edit
mvn -q -DskipTests dependency:go-offline    # resolves every coordinate, all modules
mvn -q -pl <module> -am test                # unit + slice tests for one module
mvn -q verify                               # full build incl. failsafe IT tests
mvn -pl <module> -am dependency:tree
docker compose up -d                           # postgres + redis + services
make up                                        # same, plus seed data + health wait
```

## Non-negotiable rules

1. **Never invent a version number or an artifact ID.** If unsure a coordinate
   exists, stop and say so. Never derive one by incrementing a digit in another.
   See `.claude/rules/maven.md`.
2. **Never write `<version>` for anything the Spring Boot BOM manages.**
3. **Run `mvn -q validate` before calling any POM change done.**
4. **Run the relevant tests before calling any Java change done.** A plausible diff
   you did not execute is not done.
5. **No `TODO`, no stubbed security check, no commented-out authorization.** If
   something is out of scope, add it to the out-of-scope section of `README.md`.
6. **One milestone per session.** Never start the next milestone unprompted.
7. **Nothing in this repo may depend on a credential I have to obtain.** It must run
   fully offline from a fresh clone with one command, and in CI.
8. **No secrets, no real client IDs, no employer-internal names.** This repo is
   public. Demo credentials are generated or obviously fake.

## Spring Security 7 API constraints

Your training data is mostly Spring Security 5/6. Those APIs are removed. Use only:

- `authorizeHttpRequests(...)` â€” `authorizeRequests()` does not exist
- Lambda DSL throughout; `.and()` is gone from `HttpSecurity`
- `PathPatternRequestMatcher` â€” `AntPathRequestMatcher` / `MvcRequestMatcher` are gone
- `SecurityFilterChain` beans â€” never `WebSecurityConfigurerAdapter`
- `@EnableMethodSecurity` + `@PreAuthorize` / `@PostAuthorize` â€” never
  `@EnableGlobalMethodSecurity`, never `@Secured`
- `http.with(OAuth2AuthorizationServerConfigurer.authorizationServer(), Customizer.withDefaults())`
- Jackson 3 APIs â€” do not import `jackson2` modules
- `BearerTokenAuthenticationConverter`, not the deprecated filter setters
- No OAuth2 password grant. Ever. Spec support was removed.

Boot 4 renamed starters: `spring-boot-starter-web` â†’ `spring-boot-starter-webmvc`,
and the security/oauth2 starters gained a `security-` segment. `spring-security-test`
alone no longer makes `@WithMockUser` work â€” the Boot security test starter is needed
alongside it. Verify each starter name against the BOM before use.

If an API you want is deprecated or removed, use the replacement and note it in a
comment. If you cannot find a replacement, stop and ask.

## Code conventions

- Java 21. Records for DTOs, sealed types where the hierarchy is closed.
- Constructor injection only. No field `@Autowired`.
- Package by feature, not by layer.
- Every non-obvious security decision gets a comment explaining **the threat it
  addresses**, not what the code does.
- Every implemented feature carries a `// [FEATURE B3] ...` marker comment, so the
  README conceptâ†’file table can be generated from a grep.
- Prefer explicit configuration over `spring.security.*` properties where the
  explicit form teaches more; note the property equivalent in a comment.

## Working agreement

- Plan mode before editing. Show the plan, wait for approval.
- Never "explore the codebase" unbounded. Scope reads to named files, or delegate the
  search to a subagent.
- After each milestone: summarise what you built, what you decided, what I should
  review â€” then update `.claude/rules/STATE.md` and stop.
- Commit messages: imperative mood, one logical change, no issue-tracker references.