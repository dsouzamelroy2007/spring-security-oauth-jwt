CREATE TABLE users (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id         uuid NOT NULL REFERENCES organisations (id),
    username       varchar(100) NOT NULL UNIQUE,
    password_hash  varchar(200) NOT NULL,
    full_name      varchar(200) NOT NULL,
    enabled        boolean NOT NULL DEFAULT true,
    created_at     timestamptz NOT NULL DEFAULT now()
);

-- Authority values are stored ROLE_-prefixed so UserDetails.getAuthorities()
-- needs no runtime string munging.
CREATE TABLE user_authorities (
    user_id    uuid NOT NULL REFERENCES users (id),
    authority  varchar(50) NOT NULL,
    PRIMARY KEY (user_id, authority)
);
