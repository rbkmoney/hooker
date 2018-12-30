--message_data--
CREATE TABLE IF NOT EXISTS hook.message_data
(
  id bigserial not null,
  type character varying NOT NULL,
  invoice_id character varying NOT NULL,
  party_id character varying NOT NULL,
  payment_id character varying,
  shop_id character varying NOT NULL,
  invoice_amount numeric NOT NULL,
  invoice_currency character varying NOT NULL,
  invoice_created_at character varying NOT NULL,
  invoice_content_type character varying,
  invoice_content_data bytea,
  invoice_product character varying NOT NULL,
  invoice_description character varying,
  invoice_due_date character varying,
  payment_created_at character varying,
  payment_amount numeric,
  payment_currency character varying,
  payment_tool_token character varying,
  payment_session character varying,
  payment_email character varying,
  payment_phone character varying,
  payment_ip character varying,
  payment_fingerprint character varying,
  payment_customer_id character varying,
  payment_payer_type hook.payment_payer_type,
  payment_tool_details_type hook.payment_tool_details_type,
  payment_card_number_mask character varying,
  payment_system character varying,
  payment_terminal_provider character varying,
  payment_digital_wallet_provider character varying,
  payment_digital_wallet_id character varying,
  refund_id character varying,
  refund_created_at character varying,
  refund_amount numeric,
  refund_currency character varying,
  refund_reason character varying,
  payment_card_bin character varying,
  payment_card_last_digits character varying,
  payment_card_token_provider character varying,
  payment_recurrent_parent_invoice_id character varying,
  payment_recurrent_parent_payment_id character varying,
  CONSTRAINT message_data_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS message_data_invoice_id_idx ON hook.message_data USING btree(invoice_id);

INSERT INTO hook.message_data(id, type, invoice_id, party_id, payment_id, shop_id, invoice_amount, invoice_currency, invoice_created_at, invoice_content_type, invoice_content_data, invoice_product, invoice_description, invoice_due_date, payment_created_at, payment_amount, payment_currency, payment_tool_token, payment_session, payment_email, payment_phone, payment_ip, payment_fingerprint, payment_customer_id, payment_payer_type, payment_tool_details_type, payment_card_number_mask, payment_system, payment_terminal_provider, payment_digital_wallet_provider, payment_digital_wallet_id, refund_id, refund_created_at, refund_amount, refund_currency, refund_reason, payment_card_bin, payment_card_last_digits, payment_card_token_provider, payment_recurrent_parent_invoice_id, payment_recurrent_parent_payment_id)
SELECT                        id, type, invoice_id, party_id, payment_id, shop_id, invoice_amount, invoice_currency, invoice_created_at, invoice_content_type, invoice_content_data, invoice_product, invoice_description, invoice_due_date, payment_created_at, payment_amount, payment_currency, payment_tool_token, payment_session, payment_email, payment_phone, payment_ip, payment_fingerprint, payment_customer_id, payment_payer_type, payment_tool_details_type, payment_card_number_mask, payment_system, payment_terminal_provider, payment_digital_wallet_provider, payment_digital_wallet_id, refund_id, refund_created_at, refund_amount, refund_currency, refund_reason, payment_card_bin, payment_card_last_digits, payment_card_token_provider, payment_recurrent_parent_invoice_id, payment_recurrent_parent_payment_id
FROM hook.message;
--message--

ALTER TABLE hook.message DROP COLUMN type;
ALTER TABLE hook.message DROP COLUMN payment_id;
ALTER TABLE hook.message DROP COLUMN invoice_amount;
ALTER TABLE hook.message DROP COLUMN invoice_currency;
ALTER TABLE hook.message DROP COLUMN invoice_created_at;
ALTER TABLE hook.message DROP COLUMN invoice_content_type;
ALTER TABLE hook.message DROP COLUMN invoice_content_data;
ALTER TABLE hook.message DROP COLUMN invoice_product;
ALTER TABLE hook.message DROP COLUMN invoice_description;
ALTER TABLE hook.message DROP COLUMN invoice_due_date;
ALTER TABLE hook.message DROP COLUMN payment_created_at;
ALTER TABLE hook.message DROP COLUMN payment_amount;
ALTER TABLE hook.message DROP COLUMN payment_currency;
ALTER TABLE hook.message DROP COLUMN payment_tool_token;
ALTER TABLE hook.message DROP COLUMN payment_session;
ALTER TABLE hook.message DROP COLUMN payment_email;
ALTER TABLE hook.message DROP COLUMN payment_phone;
ALTER TABLE hook.message DROP COLUMN payment_ip;
ALTER TABLE hook.message DROP COLUMN payment_fingerprint;
ALTER TABLE hook.message DROP COLUMN payment_customer_id;
ALTER TABLE hook.message DROP COLUMN payment_payer_type;
ALTER TABLE hook.message DROP COLUMN payment_tool_details_type;
ALTER TABLE hook.message DROP COLUMN payment_card_number_mask;
ALTER TABLE hook.message DROP COLUMN payment_system;
ALTER TABLE hook.message DROP COLUMN payment_terminal_provider;
ALTER TABLE hook.message DROP COLUMN payment_digital_wallet_provider;
ALTER TABLE hook.message DROP COLUMN payment_digital_wallet_id;
ALTER TABLE hook.message DROP COLUMN refund_id;
ALTER TABLE hook.message DROP COLUMN refund_created_at;
ALTER TABLE hook.message DROP COLUMN refund_amount;
ALTER TABLE hook.message DROP COLUMN refund_currency;
ALTER TABLE hook.message DROP COLUMN refund_reason;
ALTER TABLE hook.message DROP COLUMN payment_card_bin;
ALTER TABLE hook.message DROP COLUMN payment_card_last_digits;
ALTER TABLE hook.message DROP COLUMN payment_card_token_provider;
ALTER TABLE hook.message DROP COLUMN payment_recurrent_parent_invoice_id;
ALTER TABLE hook.message DROP COLUMN payment_recurrent_parent_payment_id;

ALTER TABLE hook.message ADD COLUMN message_data_id bigint;

UPDATE hook.message SET message_data_id = id;

CREATE INDEX IF NOT EXISTS cart_message_data_id_idx ON hook.cart_position(message_id);

ALTER TABLE hook.cart_position DROP CONSTRAINT fk_cart_to_message;

ALTER TABLE hook.message ADD CONSTRAINT message_data_fk FOREIGN KEY (message_data_id) REFERENCES hook.message_data(id);
