alter table users add column username varchar(40);

create unique index uk_users_username on users (username);
