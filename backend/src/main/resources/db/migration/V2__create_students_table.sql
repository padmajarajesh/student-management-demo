-- Student records. Every student belongs to exactly one owner (a user).
CREATE TABLE students (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id      UUID         NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL,
    department    VARCHAR(100) NOT NULL,
    gender        VARCHAR(20)  NOT NULL,
    date_of_birth DATE         NOT NULL,
    address       VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT fk_students_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    -- A user cannot have two students with the same email address
    CONSTRAINT uk_students_owner_email UNIQUE (owner_id, email)
);

CREATE INDEX idx_students_owner_created ON students (owner_id, created_at DESC);
CREATE INDEX idx_students_owner_department ON students (owner_id, department);
