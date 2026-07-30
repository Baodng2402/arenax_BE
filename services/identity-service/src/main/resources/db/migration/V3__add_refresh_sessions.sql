alter table users
    add column token_version integer not null default 0;

create table refresh_sessions (
    id uuid primary key,
    user_id uuid not null,
    token_hash varchar(128) not null,
    expires_at timestamp with time zone not null,
    revoked_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_refresh_sessions_user foreign key (user_id) references users (id),
    constraint uk_refresh_sessions_token_hash unique (token_hash)
);

create index idx_refresh_sessions_user on refresh_sessions (user_id);
