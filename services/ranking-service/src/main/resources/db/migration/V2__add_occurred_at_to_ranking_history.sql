alter table ranking_history add column occurred_at timestamp with time zone;

update ranking_history
set occurred_at = created_at
where occurred_at is null;

alter table ranking_history
alter column occurred_at set not null;

create index idx_ranking_history_occurred_at on ranking_history ( user_id, occurred_at desc );