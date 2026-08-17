-- Password hashes generated from this app's own PasswordEncoder bean
-- (see PasswordEncoderConfig), not hand-typed. Demo passwords only, obviously
-- fake -- this is a public portfolio repo.
--
-- Alice and Bob deliberately seeded with {bcrypt} hashes; Carol and Dana with
-- {argon2}, so DelegatingPasswordEncoder's mixed-format verification actually
-- has both formats to verify. Alice's login is the subject of the
-- bcrypt -> argon2 upgrade-on-login test.
INSERT INTO users (id, org_id, username, password_hash, full_name) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'alice',
     '{bcrypt}$2a$10$S3T2tq3VZOBgDDck08.OEu/iIZ2gmIzR0E3QyfBmEE8gRsmDRsVeG', 'Alice Employee'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', 'bob',
     '{bcrypt}$2a$10$B5oBcZZbMVilkKQXSWgxq.dh0b3gIC16h/DrVmTjdaB9HmPmkp17K', 'Bob Manager'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '22222222-2222-2222-2222-222222222222', 'carol',
     '{argon2}$argon2id$v=19$m=16384,t=2,p=1$yGjWq7Iks5R+botCqcxk5Q$NoVwBCXJhCvC11930+XrHXOFH7zHnMgKXtPnBGje3UU', 'Carol Finance'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', '22222222-2222-2222-2222-222222222222', 'dana',
     '{argon2}$argon2id$v=19$m=16384,t=2,p=1$NE/h7W23/8+o88MCHJMFpA$wAvR88hqGBPPgWPD+9XtcHIInFTUDO366sSxLBL4jDI', 'Dana OrgAdmin');

INSERT INTO user_authorities (user_id, authority) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ROLE_EMPLOYEE'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'ROLE_MANAGER'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'ROLE_FINANCE'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'ROLE_ORG_ADMIN');
