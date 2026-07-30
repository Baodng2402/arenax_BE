create table password_reset_tokens (
    id uuid primary key,
    user_id uuid not null,
    token_hash varchar(128) not null,
    expires_at timestamp with time zone not null,
    consumed_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_password_reset_tokens_user foreign key (user_id) references users (id),
    constraint uk_password_reset_tokens_token_hash unique (token_hash)
);

create index idx_password_reset_tokens_user on password_reset_tokens (user_id);
