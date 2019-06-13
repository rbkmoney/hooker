ALTER TABLE hook.message ADD COLUMN payment_content_type CHARACTER VARYING;
ALTER TABLE hook.message ADD COLUMN payment_content_data bytea;