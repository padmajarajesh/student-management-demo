-- Refresh tokens are random values; only their SHA-256 hash is stored.
-- Deleting a row revokes the token (used by logout and token rotation).
CREATE TABLE refresh_tokens (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
