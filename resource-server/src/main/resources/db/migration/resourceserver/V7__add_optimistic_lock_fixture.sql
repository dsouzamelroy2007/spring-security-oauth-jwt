-- Dedicated fixture for the optimistic-locking test: mutating a report used
-- by other seeded-data assertions (e.g. ExpenseReportPostFilterTest's title
-- checks) would make those tests order-dependent on this one having run.
INSERT INTO expense_reports (id, org_slug, submitter_subject, status, title, description, currency, total_amount) VALUES
    ('e6666666-6666-6666-6666-666666666666', 'acme', 'alice', 'DRAFT', 'Optimistic lock test fixture', 'Mutated by ExpenseReportOptimisticLockingTest', 'USD', 0);
