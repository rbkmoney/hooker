-- add values in enum (https://github.com/flyway/flyway/issues/350)
-- rename the old enum
ALTER TYPE hook.payment_tool_details_type rename TO old_payment_tool_details_type;
-- create the new enum
CREATE TYPE hook.payment_tool_details_type AS ENUM ('PaymentToolDetailsBankCard', 'PaymentToolDetailsPaymentTerminal', 'PaymentToolDetailsDigitalWallet');

ALTER TABLE hook.customer_message
  ALTER COLUMN binding_payment_tool_details_type TYPE hook.payment_tool_details_type USING binding_payment_tool_details_type::text::hook.payment_tool_details_type;

ALTER TABLE hook.message
  ALTER COLUMN payment_tool_details_type TYPE hook.payment_tool_details_type USING payment_tool_details_type::text::hook.payment_tool_details_type;

-- drop the old enum
DROP TYPE hook.old_payment_tool_details_type;


ALTER TABLE hook.message ADD COLUMN payment_digital_wallet_provider CHARACTER VARYING;
ALTER TABLE hook.message ADD COLUMN payment_digital_wallet_id CHARACTER VARYING;

ALTER TABLE hook.customer_message ADD COLUMN binding_payment_digital_wallet_provider CHARACTER VARYING;
ALTER TABLE hook.customer_message ADD COLUMN binding_payment_digital_wallet_id CHARACTER VARYING;
