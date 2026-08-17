CREATE TABLE categories (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar(100) NOT NULL UNIQUE,
    description varchar(500),
    created_at  timestamptz NOT NULL DEFAULT now()
);
