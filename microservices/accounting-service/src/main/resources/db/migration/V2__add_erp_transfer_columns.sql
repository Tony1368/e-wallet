ALTER TABLE journal_entry
    ADD COLUMN IF NOT EXISTS erp_transferred BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS erp_transferred_at TIMESTAMP;

CREATE INDEX idx_journal_entry_erp ON journal_entry(erp_transferred);
