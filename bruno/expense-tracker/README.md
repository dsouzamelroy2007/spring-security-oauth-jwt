# Expense Tracker -- Bruno collection

Open this folder as a collection in Bruno, select the **local** environment,
run `make up` first.

Covers the same endpoint x authorization-style map as `../../demo.http` at
the repo root -- prefer that file if you don't have Bruno installed, it needs
no tooling beyond a text editor and an HTTP client extension.

The interactive authorization_code + PKCE login (username/password +
consent) is a multi-step browser flow that doesn't reduce to a single Bruno
request. Log in at `http://localhost:8080/` with a seeded user
(`alice`/`alice-demo-pw`, `bob`/`bob-demo-pw`, `carol`/`carol-demo-pw`,
`dana`/`dana-demo-pw`), then paste either the `SESSION` cookie (from
DevTools) into the `sessionCookie` environment variable to call `bff-client`,
or a user access token into `userToken` to call `resource-server` directly.
The fully automated, assertion-backed version of this flow is
`integration-tests`' `CodeGrantEndToEndIT`.

`auth/client-credentials-token.bru` is the one request in this collection
that's fully self-contained -- run it, copy `access_token` from the response
into the `m2mToken` variable, and `reports/export.bru` works immediately.
