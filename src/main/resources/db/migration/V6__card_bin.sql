ALTER TABLE hook.message ADD COLUMN payment_card_bin character varying;
ALTER TABLE hook.message ADD COLUMN payment_card_last_digits character varying;
ALTER TABLE hook.customer_message ADD COLUMN binding_payment_card_bin character varying;
ALTER TABLE hook.customer_message ADD COLUMN binding_payment_card_last_digits character varying;

UPDATE hook.message
SET payment_card_last_digits = payment_card_number_mask,
    payment_card_number_mask = '******' || payment_card_number_mask;

UPDATE hook.customer_message
SET binding_payment_card_last_digits = binding_payment_card_number_mask,
    binding_payment_card_number_mask = '******' || binding_payment_card_number_mask;