# 0013. No `SecurityContext` propagation across `@Async` / virtual threads (D9)

## Status

Accepted -- deferred, cut for time (`MILESTONES.md` names this a stretch
goal explicitly droppable in M5).

## Context

By default, Spring Security's `SecurityContext` is thread-bound
(`ThreadLocal`-backed) and does not automatically follow execution onto a
`@Async` method's worker thread or a spawned virtual thread. Spring Security
6+ ships real support for this --
`DelegatingSecurityContextExecutor`/`-AsyncTaskExecutor` and
`SecurityContextChangedListener` hooks that can propagate the context
correctly, including onto virtual threads. This is a genuine, demonstrable
feature, not a workaround for a limitation.

## Decision

Not implemented in this session. Nothing in the Expense Tracker domain
currently has an `@Async` boundary that needs an authenticated
`SecurityContext` on the other side of it -- there is no code path this would
fix a real bug in today. Building one specifically to host the demo would be
scope invented to justify the feature, which the project's own conventions
(`CLAUDE.md`: "don't design for hypothetical future requirements") argue
against. `MILESTONES.md` explicitly names this the first thing to drop if
time runs short, and M5 -- already the largest milestone -- ran into exactly
that.

## Consequences

- No `@Async` method in this codebase can currently see the caller's
  authenticated principal on its worker thread; none currently needs to.
- The moment a real async boundary is added to the domain (an email
  notification on report approval, an async export job), this is the first
  place to revisit -- Spring Security's own propagation support means it is
  a configuration addition, not a redesign, when that day comes.
