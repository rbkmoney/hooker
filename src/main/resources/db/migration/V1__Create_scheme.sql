create schema if not exists hook;

CREATE TYPE hook.RetryPolicyType AS ENUM ('SIMPLE');

CREATE TYPE hook.EventType AS ENUM (
    'INVOICE_CREATED',
    'INVOICE_STATUS_CHANGED',
    'INVOICE_PAYMENT_STARTED',
    'INVOICE_PAYMENT_STATUS_CHANGED');

CREATE SEQUENCE hook.seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Table: hook.webhook
CREATE TABLE hook.webhook
(
    id bigint NOT NULL DEFAULT nextval('hook.seq'::regclass),
    party_id character varying(40) NOT NULL,
    url character varying(512) NOT NULL,
    retry_policy hook.RetryPolicyType NOT NULL DEFAULT 'SIMPLE',
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT pk_webhook PRIMARY KEY (id)
);

create index webhook_party_id_key on hook.webhook(party_id);

COMMENT ON TABLE hook.webhook
    IS 'Table with webhooks';

-- Table: hook.webhook_to_events
CREATE TABLE hook.webhook_to_events
(
    hook_id bigint NOT NULL,
    event_type hook.EventType NOT NULL,
    invoice_shop_id int,
    invoice_status character varying(32),
    invoice_payment_status character varying(32),
    CONSTRAINT pk_webhook_to_events PRIMARY KEY (hook_id, event_type),
    CONSTRAINT fk_webhook_to_events FOREIGN KEY (hook_id) REFERENCES hook.webhook(id)
);

COMMENT ON TABLE hook.webhook_to_events
    IS 'Implementation of one-to-many relation between weebhooks end events(codes)';

CREATE TABLE hook.party_key
(
    id bigint NOT NULL DEFAULT nextval('hook.seq'::regclass),
    party_id character varying(40) NOT NULL,
    pub_key character VARYING NOT NULL,
    priv_key character VARYING NOT NULL,
    CONSTRAINT pk_party_key PRIMARY KEY (id)
);

create unique index key_party_id_key on hook.party_key (party_id);


CREATE TABLE hook.message
(
    id bigint NOT NULL DEFAULT nextval('hook.seq'::regclass),
    event_type hook.EventType NOT NULL,
    type character varying(40) NOT NULL,
    invoice_id character varying(40) NOT NULL,
    event_id int NOT NULL,
    party_id character varying(40) NOT NULL,
    payment_id character varying(40),
    shop_id int NOT NULL,
    amount numeric NOT NULL,
    currency character varying(10) NOT NULL,
    created_at character varying(80) NOT NULL,
    content_type character varying,
    content_data bytea,
    status character varying(80) NOT NULL,
    product character varying(80) NOT NULL,
    description character varying(512) NOT NULL,
    CONSTRAINT message_pkey PRIMARY KEY (id)
);

CREATE TABLE hook.scheduled_task
(
    message_id bigint NOT NULL,
    hook_id bigint NOT NULL,
    CONSTRAINT scheduled_task_pkey PRIMARY KEY (message_id, hook_id),
    CONSTRAINT scheduled_task_fkey1 FOREIGN KEY (message_id) REFERENCES hook.message(id),
    CONSTRAINT scheduled_task_fkey2 FOREIGN KEY (hook_id) REFERENCES hook.webhook(id)
);


CREATE TABLE hook.simple_retry_policy
(
    hook_id bigint NOT NULL,
    fail_count int NOT NULL DEFAULT 0,
    last_fail_time BIGINT,
    CONSTRAINT simple_retry_policy_pkey PRIMARY KEY (hook_id)
);


COMMENT ON TABLE hook.message
    IS 'Table for saving messages for POST';
