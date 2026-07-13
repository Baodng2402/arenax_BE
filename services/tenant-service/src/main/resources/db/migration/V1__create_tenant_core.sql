create table accounts (
    id uuid primary key,
    owner_user_id uuid not null,
    name varchar(120) not null,
    type varchar(30) not null,
    status varchar(30) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index uk_accounts_owner_type on accounts (owner_user_id, type);

create table memberships (
    id uuid primary key,
    account_id uuid not null,
    user_id uuid not null,
    role varchar(30) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index uk_memberships_account_user on memberships (account_id, user_id);

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

create unique index uk_outbox_events_type_correlation on outbox_events (event_type, correlation_id);
