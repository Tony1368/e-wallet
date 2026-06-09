CREATE TABLE ledger (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE journal_entry (
    id BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL,
    ledger_id BIGINT NOT NULL REFERENCES ledger(id),
    from_wallet_id BIGINT NOT NULL,
    to_wallet_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_journal_entry_transaction_id ON journal_entry(transaction_id);

INSERT INTO ledger (name, description) VALUES ('GENERAL', 'General Ledger');
