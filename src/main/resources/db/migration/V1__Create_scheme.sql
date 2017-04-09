create schema if not exists hook;

create table hook.last_event_id
(
  id int not null,
  event_id bigint,
  constraint event_pkey primary key (id)
);

CREATE TABLE hook.webhook_type
(
    id int NOT NULL,
    code character varying(300) NOT NULL,
    description character varying(300),
    CONSTRAINT pk_webhook_type PRIMARY KEY (id)
);

COMMENT ON TABLE hook.webhook_type
    IS 'Types of webhooks (for example "invoice status changed", "payment started")';

-- Table: hook.webhook

CREATE TABLE hook.webhook
(
    id character varying(40) NOT NULL,
    party_id character varying(40) NOT NULL,
    type int NOT NULL,
    url character varying(512) NOT NULL,
    CONSTRAINT pk_webhook PRIMARY KEY (id)
);

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

CREATE TABLE hook.invoice
(
    id bigint NOT NULL DEFAULT nextval('hook.seq'::regclass),
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

COMMENT ON TABLE hook.invoice
    IS 'Table for saving invoice info';

insert into hook.last_event_id (id, event_id) values (1, 1);

insert into hook.webhook_type(id, code, description) values(1, 'source_event.processing_event.payload.invoice_event.invoice_created.payment', 'Создание инвойса');
insert into hook.webhook_type(id, code, description) values(2, 'source_event.processing_event.payload.invoice_event.invoice_status_changed.status', 'Изменение статуса инвойса');
insert into hook.webhook_type(id, code, description) values(3, 'source_event.processing_event.payload.invoice_event.invoice_payment_event.invoice_payment_started.payment', 'Создание платежа');
insert into hook.webhook_type(id, code, description) values(4, 'source_event.processing_event.payload.invoice_event.invoice_payment_event.invoice_payment_status_changed.status', 'Изменение статуса платежа');
