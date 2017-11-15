-- clean table
delete from hook.simple_retry_policy;

-- drop constraint
ALTER TABLE hook.simple_retry_policy DROP CONSTRAINT simple_retry_policy_pkey;

-- drop column
ALTER TABLE hook.simple_retry_policy DROP COLUMN hook_id;

-- createWithPolicy queue table
CREATE TABLE hook.queue_invoicing
(
    id bigserial NOT NULL,
    hook_id bigint NOT NULL,
    invoice_id CHARACTER VARYING NOT NULL,
    CONSTRAINT queue_invoicing_pkey PRIMARY KEY (id)
);

-- createWithPolicy queue_id
ALTER TABLE hook.simple_retry_policy ADD COLUMN queue_id bigint NOT NULL;
ALTER TABLE hook.simple_retry_policy ADD CONSTRAINT simple_retry_policy_pkey PRIMARY KEY (queue_id, hook_id);
