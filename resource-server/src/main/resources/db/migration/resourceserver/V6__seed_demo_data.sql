-- Matches authorization-server's seeded users (V7/V8 there): alice/bob in
-- acme, carol/dana in globex. submitter_subject is the JWT `sub` claim, i.e.
-- the username -- see resource-server's TestJwtSupport and STATE.md.

-- alice's DRAFT: exercises @PostFilter (visible to alice, hidden from bob
-- when bob lists acme's reports).
INSERT INTO expense_reports (id, org_slug, submitter_subject, status, title, description, currency, total_amount) VALUES
    ('e1111111-1111-1111-1111-111111111111', 'acme', 'alice', 'DRAFT', 'Conference trip planning', 'Not yet submitted', 'USD', 0),
    ('e2222222-2222-2222-2222-222222222222', 'acme', 'alice', 'SUBMITTED', 'Q1 client travel', 'Site visits to two clients', 'USD', 450.00),
    ('e3333333-3333-3333-3333-333333333333', 'acme', 'bob', 'APPROVED', 'Team offsite', 'Quarterly team offsite', 'USD', 1200.00),
    ('e4444444-4444-4444-4444-444444444444', 'globex', 'carol', 'APPROVED', 'Client dinner', 'Dinner with prospective client', 'EUR', 300.00),
    ('e5555555-5555-5555-5555-555555555555', 'globex', 'dana', 'DRAFT', 'Internal audit trip', 'Not yet submitted', 'EUR', 0);

-- id prefixes stay within a-f/0-9 (valid hex): "i" and "r" below would not
-- parse as a uuid literal.
INSERT INTO expense_items (id, report_id, category_id, amount, currency, expense_date, note) VALUES
    ('a1111111-1111-1111-1111-111111111111', 'e2222222-2222-2222-2222-222222222222', 'c1111111-1111-1111-1111-111111111111', 350.00, 'USD', '2026-01-10', 'Round-trip flight'),
    ('a1111111-1111-1111-1111-111111111112', 'e2222222-2222-2222-2222-222222222222', 'c2222222-2222-2222-2222-222222222222', 100.00, 'USD', '2026-01-10', 'Client lunch'),
    ('a2222222-2222-2222-2222-222222222221', 'e3333333-3333-3333-3333-333333333333', 'c1111111-1111-1111-1111-111111111111', 1200.00, 'USD', '2026-01-15', 'Venue and travel'),
    ('a3333333-3333-3333-3333-333333333331', 'e4444444-4444-4444-4444-444444444444', 'c2222222-2222-2222-2222-222222222222', 300.00, 'EUR', '2026-01-20', 'Dinner for four');

-- Demo-only, obviously fake IBANs -- public repo. Backs acceptance criterion
-- 4 (masked unless ROLE_FINANCE).
INSERT INTO reimbursements (id, report_id, org_slug, iban, amount, currency, paid_at) VALUES
    ('b1111111-1111-1111-1111-111111111111', 'e3333333-3333-3333-3333-333333333333', 'acme', 'GB29NWBK60161331926819', 1200.00, 'USD', now()),
    ('b2222222-2222-2222-2222-222222222222', 'e4444444-4444-4444-4444-444444444444', 'globex', 'DE89370400440532013000', 300.00, 'EUR', now());
