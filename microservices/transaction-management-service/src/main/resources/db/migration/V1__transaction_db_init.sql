CREATE TABLE IF NOT EXISTS transaction (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(19, 2) NOT NULL,
    description VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reference_number UUID NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    from_wallet_id BIGINT NOT NULL,
    to_wallet_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL
);

CREATE INDEX idx_transaction_from_wallet ON transaction(from_wallet_id);
CREATE INDEX idx_transaction_to_wallet ON transaction(to_wallet_id);
CREATE INDEX idx_transaction_reference ON transaction(reference_number);
