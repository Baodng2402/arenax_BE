create table users (
    id uuid primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    display_name varchar(120) not null,
    status varchar(30) not null,
    active_account_id uuid,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table refresh_sessions (
    id uuid primary key,
    user_id uuid not null,
    token_hash varchar(128) not null unique,
    expires_at timestamp with time zone not null,
    revoked_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_refresh_sessions_user_id on refresh_sessions (user_id);

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

create index idx_outbox_events_type_occurred_at on outbox_events (event_type, occurred_at);
