create schema if not exists hook;

-- Table: hook.webhook
CREATE TABLE hook.webhook
(
    id character varying(40) NOT NULL,
    party_id character varying(40) NOT NULL,
    code character varying(256) NOT NULL,
    url character varying(512) NOT NULL,
    CONSTRAINT pk_webhook PRIMARY KEY (id)
);

create index webhook_party_id_key on hook.webhook (party_id);

COMMENT ON TABLE hook.webhook
    IS 'Table with webhooks';

CREATE SEQUENCE hook.seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE hook.key
(
    id bigint NOT NULL DEFAULT nextval('hook.seq'::regclass),
    party_id character varying(40) NOT NULL,
    pub_key character VARYING NOT NULL,
    priv_key character VARYING NOT NULL
);

create index key_party_id_key on hook.key (party_id);

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

create unique index invoice_id_key on hook.invoice (invoice_id);
create index invoice_event_id_key on hook.invoice (event_id);
create index invoice_party_id_key on hook.invoice (party_id);

COMMENT ON TABLE hook.invoice
    IS 'Table for saving invoice info';
