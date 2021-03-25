-- create indices
CREATE INDEX IF NOT EXISTS invoicing_queue_invoice_id_idx ON hook.invoicing_queue USING btree(invoice_id);
CREATE INDEX IF NOT EXISTS customer_queue_customer_id_idx ON hook.customer_queue USING btree(customer_id);