CREATE TABLE IF NOT EXISTS wallet (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    iban VARCHAR(255) NOT NULL UNIQUE,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    bank_info VARCHAR(500)
);

CREATE INDEX idx_wallet_user_id ON wallet(user_id);
CREATE INDEX idx_wallet_iban ON wallet(iban);
