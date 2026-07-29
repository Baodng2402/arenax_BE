create table users(
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(255) not null,
    status varchar(30) not null,
    full_name varchar(120),
    email_verified_at timestamp with time zone,
    avatar_url varchar(500),
    last_login_at timestamp with time zone,
    password_changed_at timestamp with time zone not null,
    failed_login_attempts integer not null default 0,
    locked_until timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    version bigint not null,
    constraint uk_users_email unique (email)
);