ALTER TABLE hook.message ALTER COLUMN invoice_amount DROP NOT NULL;
ALTER TABLE hook.message ALTER COLUMN invoice_currency DROP NOT NULL;
ALTER TABLE hook.message ALTER COLUMN invoice_created_at DROP NOT NULL;
ALTER TABLE hook.message ALTER COLUMN invoice_product DROP NOT NULL;
ALTER TABLE hook.customer_message ALTER COLUMN customer_status DROP NOT NULL;