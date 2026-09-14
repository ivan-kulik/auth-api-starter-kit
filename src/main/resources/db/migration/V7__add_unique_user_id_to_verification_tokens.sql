ALTER TABLE verification_tokens
    ADD CONSTRAINT uq_verification_tokens_user_id UNIQUE (user_id);