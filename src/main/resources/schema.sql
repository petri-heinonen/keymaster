CREATE TABLE keys (
    id INTEGER NOT NULL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    key_id VARCHAR(255) NOT NULL UNIQUE,
    use VARCHAR(255),
    is_published BOOLEAN NOT NULL,
    jwk TEXT NOT NULL
);