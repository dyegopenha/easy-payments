--
-- PostgreSQL Schema for Easy Payment
--
-- -----------------------------------------------------
-- Table: payments
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    card_number_encrypted VARCHAR(256) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Index on external_id for quick idempotency checks
CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_external_id ON payments (external_id);


-- -----------------------------------------------------
-- Table: webhooks
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS webhooks (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(500) UNIQUE NOT NULL
);


-- -----------------------------------------------------
-- Table: failed_messages
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS failed_messages (
    id BIGSERIAL PRIMARY KEY,
    payload TEXT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);