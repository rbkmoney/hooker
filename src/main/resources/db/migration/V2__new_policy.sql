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
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT invoicing_queue_pkey PRIMARY KEY (id),
    CONSTRAINT invoicing_queue_pkey2 UNIQUE (hook_id, invoice_id)
);

-- create customer_queue table
CREATE TABLE hook.customer_queue
(
    id bigserial NOT NULL,
    hook_id bigint NOT NULL,
    customer_id CHARACTER VARYING NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT customer_queue_pkey PRIMARY KEY (id),
    CONSTRAINT customer_queue_pkey2 UNIQUE (hook_id, customer_id)
);

-- add queue_id column
ALTER TABLE hook.simple_retry_policy ADD COLUMN queue_id bigint NOT NULL;
ALTER TABLE hook.simple_retry_policy ADD COLUMN message_type hook.message_topic;
ALTER TABLE hook.simple_retry_policy ADD CONSTRAINT simple_retry_policy_pkey PRIMARY KEY (queue_id, message_type);

-- replace hook_id to queue_id
ALTER TABLE hook.scheduled_task DROP CONSTRAINT  scheduled_task_pkey;
ALTER TABLE hook.scheduled_task DROP CONSTRAINT  scheduled_task_fkey2;
ALTER TABLE hook.scheduled_task DROP COLUMN hook_id;
ALTER TABLE hook.scheduled_task ADD COLUMN queue_id bigint NOT NULL;
ALTER TABLE hook.scheduled_task ADD CONSTRAINT scheduled_task_pkey PRIMARY KEY (message_id, queue_id, message_type);

-- create indices
CREATE INDEX IF NOT EXISTS message_invoice_id_idx ON hook.message USING btree(invoice_id);
CREATE INDEX IF NOT EXISTS customer_message_customer_id_idx ON hook.customer_message USING btree(customer_id);

-- add column topic to webhook
ALTER TABLE hook.webhook ADD COLUMN topic hook.message_topic;
UPDATE hook.webhook w SET topic='InvoicesTopic'  where exists (select * from hook.webhook_to_events wh where wh.hook_id = w.id AND wh.event_type in ('INVOICE_CREATED', 'INVOICE_STATUS_CHANGED', 'INVOICE_PAYMENT_STARTED', 'INVOICE_PAYMENT_STATUS_CHANGED'));
UPDATE hook.webhook w SET topic='CustomersTopic' where exists (select * from hook.webhook_to_events wh where wh.hook_id = w.id AND wh.event_type in ('CUSTOMER_CREATED', 'CUSTOMER_DELETED', 'CUSTOMER_READY', 'CUSTOMER_BINDING_STARTED', 'CUSTOMER_BINDING_SUCCEEDED', 'CUSTOMER_BINDING_FAILED'));
