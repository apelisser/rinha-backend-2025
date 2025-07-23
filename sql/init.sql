set statement_timeout = 0;
set lock_timeout = 0;
set idle_in_transaction_session_timeout = 0;

create table payment (
    id bigserial primary key,
    correlation_id uuid not null unique,
    amount numeric(10,2) not null,
    default_processor boolean not null,
    requested_at timestamptz not null default now()
);

create index idx_payment_summary on payment (requested_at, default_processor);
