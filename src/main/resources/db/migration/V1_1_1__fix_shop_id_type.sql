ALTER TABLE hook.message ALTER COLUMN shop_id TYPE varchar(40);
ALTER TABLE hook.webhook_to_events ALTER COLUMN invoice_shop_id TYPE varchar(40);
