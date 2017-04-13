create schema if not exists hook;

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
    event_code character varying(256) NOT NULL,
    CONSTRAINT pk_webhook_to_events PRIMARY KEY (hook_id, event_code),
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

CREATE TABLE hook.invoice
(
    id bigint NOT NULL DEFAULT nextval('hook.seq'::regclass),
    event_id int NOT NULL,
    invoice_id character varying(40) NOT NULL,
    party_id character varying(40) NOT NULL,
    shop_id int NOT NULL,
    amount numeric NOT NULL,
    currency character varying(10) NOT NULL,
    created_at character varying(80) NOT NULL,
    content_type character varying,
    content_data bytea,
    CONSTRAINT invoice_pkey PRIMARY KEY (id)
);

CREATE TYPE hook.EventStatus AS ENUM ('RECEIVED', 'SCHEDULED');

CREATE TABLE hook.event
(
    id bigint NOT NULL,
    code character varying(256) NOT NULL,
    status hook.EventStatus NOT NULL,
    --additional data required for different types of events
    invoice_id character varying(40) NOT NULL,
    CONSTRAINT event_pkey PRIMARY KEY (id)
);

CREATE TABLE hook.scheduled_task
(
    event_id bigint NOT NULL,
    hook_id character varying(256) NOT NULL,
    CONSTRAINT scheduled_task_pkey PRIMARY KEY (event_id, hook_id)
);

create unique index invoice_id_key on hook.invoice (invoice_id);
create index invoice_event_id_key on hook.invoice (event_id);
create index invoice_party_id_key on hook.invoice (party_id);

COMMENT ON TABLE hook.invoice
    IS 'Table for saving invoice info';
