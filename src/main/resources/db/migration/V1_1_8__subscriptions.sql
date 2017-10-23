-- add values in enum (https://github.com/flyway/flyway/issues/350)
-- rename the old enum
ALTER TYPE hook.EventType rename TO OldEventType;
-- create the new enum
CREATE TYPE hook.EventType AS ENUM (
    'INVOICE_CREATED',
    'INVOICE_STATUS_CHANGED',
    'INVOICE_PAYMENT_STARTED',
    'INVOICE_PAYMENT_STATUS_CHANGED',
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

-- drop the old enum
DROP TYPE hook.OldEventType;

CREATE TYPE hook.customer_message_type AS ENUM ('customer','binding');
CREATE TYPE hook.customer_status AS ENUM ('ready','unready');
CREATE TYPE hook.customer_binding_status AS ENUM ('pending','succeeded','failed');
CREATE TYPE hook.payment_payer_type AS ENUM ('CustomerPayer','PaymentResourcePayer');
CREATE TYPE hook.payment_tool_details_type AS ENUM ('PaymentToolDetailsBankCard', 'PaymentToolDetailsPaymentTerminal');
CREATE TABLE hook.customer_message
(
    id bigserial NOT NULL,
    event_id bigint NOT NULL,
    type customer_message_type NOT NULL,
    event_type hook.EventType NOT NULL,
    occured_at character varying NOT NULL,
    party_id character varying NOT NULL,
    customer_id character varying NOT NULL,
    customer_shop_id character varying NOT NULL,
    customer_status hook.customer_status NOT NULL,
    customer_email character varying,
    customer_phone character varying,
    customer_metadata character varying,
    binding_id character varying,
    binding_payment_tool_token character varying,
    binding_payment_session character varying,
    binding_payment_tool_details_type hook.payment_tool_details_type,
    binding_payment_card_number_mask character varying,
    binding_payment_card_system character varying,
    binding_payment_terminal_provider character varying,
    binding_client_ip character varying,
    binding_client_fingerprint character varying,
    binding_status hook.customer_binding_status,
    binding_error_code character varying,
    binding_error_message character varying,
    CONSTRAINT pk_customer PRIMARY KEY (id)
);

ALTER TABLE hook.message ADD COLUMN payment_customer_id character varying;
ALTER TABLE hook.message ADD COLUMN payment_payer_type hook.payment_payer_type;
ALTER TABLE hook.message ADD COLUMN payment_tool_details_type hook.payment_tool_details_type;
ALTER TABLE hook.message ADD COLUMN payment_card_number_mask character varying;
ALTER TABLE hook.message ADD COLUMN payment_system character varying;
ALTER TABLE hook.message ADD COLUMN payment_terminal_provider character varying;

ALTER TABLE hook.scheduled_task DROP CONSTRAINT scheduled_task_fkey1;
CREATE TYPE hook.message_topic AS ENUM ('InvoicesTopic','CustomersTopic');
ALTER TABLE hook.scheduled_task ADD COLUMN message_type hook.message_topic;
UPDATE hook.scheduled_task SET message_type = 'InvoicesTopic';
