CREATE TYPE hook.customer_status AS ENUM ('ready','unready');
CREATE TYPE hook.customer_binding_status AS ENUM ('created','succeeded','failed');
CREATE TYPE hook.message_topic AS ENUM ('InvoicesTopic','CustomersTopic');
CREATE TABLE hook.customer
(
    id bigserial not null,
    message_id bigint not null,
    customer_id character varying not null,
    shop_id character varying not null,
    customer_status hook.customer_status  not null,
    email character varying,
    phone character varying,
    binding_id character varying,
    payment_tool_token character varying,
    payment_session character varying,
    payment_tool_details character varying,
    payment_card_number_mask character varying,
    payment_system character varying,
    payment_terminal_provider character varying,
    ip character varying,
    fingerprint character varying,
    binding_status hook.customer_binding_status,
    binding_error_code character varying,
    binding_error_message character varying,
    CONSTRAINT pk_customer PRIMARY KEY (id),
    CONSTRAINT fk_cust_to_message FOREIGN KEY (message_id) REFERENCES hook.message(id)
);

ALTER TABLE hook.message ADD COLUMN payment_customer_id character varying;
ALTER TABLE hook.message ADD COLUMN topic message_topic;
UPDATE hook.message SET topic='InvoicesTopic';
