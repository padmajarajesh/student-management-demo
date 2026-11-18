-- Application users (people who log in and manage their own students)
CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT uk_users_email UNIQUE (email)
);
