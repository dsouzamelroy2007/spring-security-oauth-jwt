-- Schema only for now; the writer (audit event listener) lands with D8 in M4.
CREATE TABLE audit_log (
    id           bigserial PRIMARY KEY,
    occurred_at  timestamptz NOT NULL DEFAULT now(),
    event_type   varchar(100) NOT NULL,
    principal    varchar(200),
    org_id       uuid REFERENCES organisations (id),
    ip_address   varchar(45),
    detail       jsonb
);
