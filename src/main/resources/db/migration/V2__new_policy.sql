-- clean table
delete from hook.simple_retry_policy;
delete from hook.scheduled_task;

-- drop constraint
ALTER TABLE hook.simple_retry_policy DROP CONSTRAINT simple_retry_policy_pkey;

-- drop column
ALTER TABLE hook.simple_retry_policy DROP COLUMN hook_id;

-- create invoicing_queue table
CREATE TABLE hook.invoicing_queue
(
    id bigserial NOT NULL,
    hook_id bigint NOT NULL,
    invoice_id CHARACTER VARYING NOT NULL,
    CONSTRAINT invoicing_queue_pkey PRIMARY KEY (id)
);

-- create customer_queue table
CREATE TABLE hook.customer_queue
(
    id bigserial NOT NULL,
    hook_id bigint NOT NULL,
    customer_id bigint NOT NULL,
    CONSTRAINT customer_queue_pkey PRIMARY KEY (id)
);

-- add queue_id column
ALTER TABLE hook.simple_retry_policy ADD COLUMN queue_id bigint NOT NULL;
ALTER TABLE hook.simple_retry_policy ADD CONSTRAINT simple_retry_policy_pkey PRIMARY KEY (queue_id);

-- replace hook_id to queue_id
ALTER TABLE hook.scheduled_task DROP CONSTRAINT  scheduled_task_pkey;
ALTER TABLE hook.scheduled_task DROP CONSTRAINT  scheduled_task_fkey2;
ALTER TABLE hook.scheduled_task DROP COLUMN hook_id;
ALTER TABLE hook.scheduled_task ADD COLUMN queue_id bigint NOT NULL;
ALTER TABLE hook.scheduled_task ADD CONSTRAINT scheduled_task_pkey PRIMARY KEY (message_id, queue_id, message_type);

