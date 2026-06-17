ALTER TABLE journal_entry
    ADD COLUMN IF NOT EXISTS transaction_type VARCHAR(30);

CREATE INDEX idx_journal_entry_type ON journal_entry(transaction_type);
