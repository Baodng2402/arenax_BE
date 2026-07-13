create table subscriptions (
    id uuid primary key,
    account_id uuid not null,
    plan varchar(30) not null,
    status varchar(30) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_subscriptions_account_id unique (account_id)
);

create table outbox_events (
    id uuid primary key,
    event_type varchar(120) not null,
    event_version integer not null,
    correlation_id uuid not null,
    producer varchar(80) not null,
    occurred_at timestamp with time zone not null,
    payload clob not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_subscription_outbox_type_correlation on outbox_events (event_type, correlation_id);
