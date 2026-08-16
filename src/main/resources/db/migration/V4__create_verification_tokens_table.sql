CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_verification_tokens_token_hash UNIQUE (token_hash),

    CONSTRAINT fk_verification_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE
);