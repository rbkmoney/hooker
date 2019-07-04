CREATE UNIQUE INDEX IF NOT EXISTS message_uniq_idx ON hook.message(invoice_id, sequence_id, change_id);
ALTER TABLE hook.message ADD CONSTRAINT message_uniq_constraint UNIQUE USING INDEX message_uniq_idx;

CREATE SEQUENCE hook.event_id_seq
    INCREMENT 1
    START 380000000
    MINVALUE 380000000
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER TABLE hook.message ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE hook.message ADD COLUMN new_event_id bigint DEFAULT nextval('hook.event_id_seq'::regclass);