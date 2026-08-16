CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,

    CONSTRAINT uq_roles_name UNIQUE (name)
);