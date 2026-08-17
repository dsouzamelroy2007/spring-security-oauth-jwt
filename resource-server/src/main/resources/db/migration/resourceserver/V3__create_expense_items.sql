CREATE TABLE expense_items (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id    uuid NOT NULL REFERENCES expense_reports (id),
    category_id  uuid NOT NULL REFERENCES categories (id),
    amount       numeric(12, 2) NOT NULL,
    currency     varchar(3) NOT NULL,
    expense_date date NOT NULL,
    note         varchar(500),
    created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_expense_items_report_id ON expense_items (report_id);
