create table player_rankings (
    id uuid primary key,
    user_id uuid not null unique,
    rating integer not null,
    wins integer not null,
    losses integer not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table ranking_history (
    id uuid primary key,
    match_id uuid not null,
    user_id uuid not null,
    previous_rating integer not null,
    new_rating integer not null,
    result varchar(20) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index uk_ranking_history_match_user on ranking_history (match_id, user_id);
