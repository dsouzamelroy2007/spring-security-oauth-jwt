-- org_slug / submitter_subject are plain JWT-claim-sourced strings, not FKs:
-- this service owns no organisations/users table (see CLAUDE.md's
-- architectural invariant). Tenancy and ownership checks compare these
-- columns against the caller's own token claims, never a local join.
CREATE TABLE expense_reports (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_slug          varchar(50) NOT NULL,
    submitter_subject varchar(100) NOT NULL,
    status            varchar(20) NOT NULL,
    title             varchar(200) NOT NULL,
    description       varchar(1000),
    currency          varchar(3) NOT NULL,
    total_amount      numeric(12, 2) NOT NULL DEFAULT 0,
    version           bigint NOT NULL DEFAULT 0,
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_expense_reports_org_slug ON expense_reports (org_slug);
CREATE INDEX idx_expense_reports_submitter_subject ON expense_reports (submitter_subject);
