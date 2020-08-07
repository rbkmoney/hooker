alter table hook.message add column wtime TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc');
create index message_wtime_idx on hook.message(wtime);
