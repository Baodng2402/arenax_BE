create table sports (
    id uuid primary key,
    name varchar(120) not null unique,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table matches (
    id uuid primary key,
    sport_id uuid not null,
    match_type varchar(20) not null,
    status varchar(20) not null,
    team1_score integer,
    team2_score integer,
    finished_at timestamp with time zone,
    version bigint not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table match_participants (
    id uuid primary key,
    match_id uuid not null,
    user_id uuid not null,
    team_number integer not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index uk_match_participants_match_user on match_participants (match_id, user_id);

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

create unique index uk_competition_outbox_type_correlation on outbox_events (event_type, correlation_id);
