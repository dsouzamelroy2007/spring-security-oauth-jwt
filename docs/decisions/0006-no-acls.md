# 0006. No Spring Security ACLs

## Status

Accepted -- out of scope.

## Context

Spring Security's ACL module gives per-object, per-principal permissions
backed by dedicated `acl_*` tables (object identity, SID, entry, class) --
a heavier-weight alternative to `[FEATURE C4]`'s custom
`AuthorizationManager` for expressing "this report belongs to this org, and
within that org, only its submitter may edit it."

## Decision

Not implemented. `[FEATURE C4]`'s `ReportAccessAuthorizationManager`
(tenant-scoping + ownership) demonstrates the same underlying skill --
custom, data-driven authorization decisions -- at a small fraction of the
schema and configuration cost. Four extra ACL tables and their maintenance
would be teaching the same lesson twice.

## Consequences

- Authorization decisions in this repo are expressed as ordinary Spring
  Security `AuthorizationManager` beans plus method security
  (`[FEATURE C3]`), not ACL entries -- simpler to read, and the pattern
  scales to this domain's actual needs (org + ownership, not arbitrary
  per-object grants to arbitrary principals).
- A domain that genuinely needed "share this specific report with these
  three named users" would be a real case for ACLs; the Expense Tracker
  domain never needs that shape of rule.
