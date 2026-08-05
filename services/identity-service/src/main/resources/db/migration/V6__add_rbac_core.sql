create table permissions (
    id uuid primary key,
    code varchar(80) not null,
    name varchar(120) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_permissions_code unique (code)
);

create table roles (
    id uuid primary key,
    code varchar(80) not null,
    name varchar(120) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_roles_code unique (code)
);

create table role_permissions (
    role_id uuid not null,
    permission_id uuid not null,
    primary key (role_id, permission_id)
);

create table role_assignments (
    id uuid primary key,
    user_id uuid not null,
    account_id uuid not null,
    role_code varchar(80) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_role_assignments_user_account_role unique (user_id, account_id, role_code)
);
