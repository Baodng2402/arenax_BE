create table user_identifiers (
    id uuid primary key,
    user_id uuid not null,
    type varchar(30) not null,
    normalized_value varchar(320) not null,
    is_primary boolean not null,
    verified_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_user_identifiers_user foreign key (user_id) references users (id),
    constraint uk_user_identifiers_type_value unique (type, normalized_value)
);

create index idx_user_identifiers_user on user_identifiers (user_id);

insert into user_identifiers (id, user_id, type, normalized_value, is_primary, verified_at, created_at, updated_at)
select id, id, 'EMAIL', email, true, email_verified_at, created_at, updated_at
from users;

alter table email_verification_tokens add column user_identifier_id uuid;

update email_verification_tokens evt
set user_identifier_id = (
    select ui.id
    from user_identifiers ui
    where ui.user_id = evt.user_id
      and ui.type = 'EMAIL'
      and ui.is_primary = true
);

alter table email_verification_tokens alter column user_identifier_id set not null;
alter table email_verification_tokens add constraint fk_email_verification_tokens_identifier
    foreign key (user_identifier_id) references user_identifiers (id);

create index idx_email_verification_tokens_identifier on email_verification_tokens (user_identifier_id);
