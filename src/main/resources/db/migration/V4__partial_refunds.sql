-- add values in enum (https://github.com/flyway/flyway/issues/350)
-- rename the old enum
ALTER TYPE hook.EventType rename TO OldEventType;
-- create the new enum
CREATE TYPE hook.EventType AS ENUM (
    'INVOICE_CREATED',
    'INVOICE_STATUS_CHANGED',
    'INVOICE_PAYMENT_STARTED',
    'INVOICE_PAYMENT_STATUS_CHANGED',
    'INVOICE_PAYMENT_REFUND_STARTED',
    'INVOICE_PAYMENT_REFUND_STATUS_CHANGED',
    'CUSTOMER_CREATED',
    'CUSTOMER_DELETED',
    'CUSTOMER_READY',
    'CUSTOMER_BINDING_STARTED',
    'CUSTOMER_BINDING_SUCCEEDED',
    'CUSTOMER_BINDING_FAILED'
    );

ALTER TABLE hook.webhook_to_events
  ALTER COLUMN event_type TYPE hook.EventType USING event_type::text::hook.EventType;

ALTER TABLE hook.message
  ALTER COLUMN event_type TYPE hook.EventType USING event_type::text::hook.EventType;

ALTER TABLE hook.customer_message
  ALTER COLUMN event_type TYPE hook.EventType USING event_type::text::hook.EventType;

-- drop the old enum
DROP TYPE hook.OldEventType;

ALTER TABLE hook.message ADD COLUMN refund_id character varying;
ALTER TABLE hook.message ADD COLUMN refund_created_at character varying;
ALTER TABLE hook.message ADD COLUMN refund_status character varying;
ALTER TABLE hook.message ADD COLUMN refund_error_code character varying;
ALTER TABLE hook.message ADD COLUMN refund_error_message character varying;
ALTER TABLE hook.message ADD COLUMN refund_amount numeric;
ALTER TABLE hook.message ADD COLUMN refund_currency character varying;
ALTER TABLE hook.message ADD COLUMN refund_reason character varying;

ALTER TABLE hook.webhook_to_events ADD COLUMN invoice_payment_refund_status character varying;
