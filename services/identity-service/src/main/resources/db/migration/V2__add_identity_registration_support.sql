create table email_verification_tokens (
    id uuid primary key,
    user_id uuid not null,
    token_hash varchar(128) not null,
    expires_at timestamp with time zone not null,
    consumed_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_email_verification_tokens_user foreign key (user_id) references users (id),
    constraint uk_email_verification_tokens_hash unique (token_hash)
);

create index idx_email_verification_tokens_user on email_verification_tokens (user_id);

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

create index idx_identity_outbox_events_type_correlation on outbox_events (event_type, correlation_id);
