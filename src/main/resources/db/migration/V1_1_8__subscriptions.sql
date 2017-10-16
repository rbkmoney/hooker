CREATE TYPE hook.customer_status AS ENUM ('ready','unready');
CREATE TYPE hook.customer_binding_status AS ENUM ('created','succeeded','failed');
CREATE TYPE hook.payment_payer_type AS ENUM ('CustomerPayer','PaymentResourcePayer');
CREATE TYPE hook.payment_tool_details_type AS ENUM ('PaymentToolDetailsBankCard', 'PaymentToolDetailsPaymentTerminal');
CREATE TYPE hook.payment_terminal_provider AS ENUM ('euroset');
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
    binding_payment_terminal_provider hook.payment_terminal_provider,
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
ALTER TABLE hook.message ADD COLUMN payment_terminal_provider hook.payment_terminal_provider;

