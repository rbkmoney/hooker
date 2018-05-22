ALTER TABLE hook.message ADD COLUMN payment_card_token_provider character varying;
ALTER TABLE hook.customer_message ADD COLUMN binding_payment_card_token_provider character varying;