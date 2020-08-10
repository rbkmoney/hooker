alter table hook.invoicing_queue add column IF NOT EXISTS wtime TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc');
create index IF NOT EXISTS invoicing_queue_wtime_idx on hook.invoicing_queue(wtime);

alter table hook.customer_queue add column IF NOT EXISTS wtime TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc');
create index IF NOT EXISTS customer_queue_wtime_idx on hook.customer_queue(wtime);
