CREATE TABLE organisations (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar(200) NOT NULL,
    slug        varchar(50)  NOT NULL UNIQUE,
    created_at  timestamptz  NOT NULL DEFAULT now()
);
