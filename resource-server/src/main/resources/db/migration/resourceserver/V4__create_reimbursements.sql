-- org_slug is denormalized from the parent report so the tenant-scoped list
-- endpoint (GET /api/v1/reimbursements) never needs a join just to filter by
-- caller org.
CREATE TABLE reimbursements (
    id        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id uuid NOT NULL UNIQUE REFERENCES expense_reports (id),
    org_slug  varchar(50) NOT NULL,
    iban      varchar(34) NOT NULL,
    amount    numeric(12, 2) NOT NULL,
    currency  varchar(3) NOT NULL,
    paid_at   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_reimbursements_org_slug ON reimbursements (org_slug);
