create table onboarding_progress (
    correlation_id uuid primary key,
    user_id uuid not null,
    account_id uuid,
    authorization_ready boolean not null,
    subscription_ready boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table authorization_projections (
    id uuid primary key,
    user_id uuid not null,
    account_id uuid not null,
    roles_csv varchar(500) not null,
    permissions_csv varchar(2000) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index uk_authorization_projections_user_account
    on authorization_projections (user_id, account_id);
