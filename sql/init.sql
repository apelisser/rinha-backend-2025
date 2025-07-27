set statement_timeout = 0;
set lock_timeout = 0;
set idle_in_transaction_session_timeout = 5000;

-- TYPES
CREATE TYPE payment_processor AS ENUM ('DEFAULT', 'FALLBACK');
CREATE TYPE payment_status AS ENUM ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED');

SET session_replication_role = 'replica';

-- TABLES
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    correlation_id UUID NOT NULL UNIQUE,
    amount NUMERIC(15, 2) NOT NULL,
    processor payment_processor,
    status payment_status NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE scheduler_locks (
    lock_name VARCHAR(255) PRIMARY KEY,
    last_execution TIMESTAMPTZ NOT NULL
);

CREATE TABLE health_check_status (
    processor_name payment_processor PRIMARY KEY,
    is_failing BOOLEAN NOT NULL,
    min_response_time BIGINT NOT NULL,
    last_checked TIMESTAMPTZ NOT NULL
);

-- INSERTS
INSERT INTO scheduler_locks (lock_name, last_execution)
VALUES ('health_check_leader', NOW() - INTERVAL '1 minute');

INSERT INTO health_check_status (processor_name, is_failing, min_response_time, last_checked)
VALUES ('DEFAULT', false, 0, NOW()),
       ('FALLBACK', false, 0, NOW());

SET session_replication_role = 'origin';

-- INDEXES
CREATE INDEX idx_payments_pending_work ON payment (requested_at) WHERE status IN ('PENDING', 'FAILED');
CREATE INDEX idx_payments_summary ON payment (processor, requested_at) WHERE status = 'PROCESSED';
CREATE INDEX idx_outbox_event_created_at ON outbox_event (created_at);
CREATE INDEX idx_outbox_event_payment_id ON outbox_event (payment_id);
