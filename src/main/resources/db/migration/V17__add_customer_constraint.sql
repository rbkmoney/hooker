CREATE UNIQUE INDEX IF NOT EXISTS customer_message_uniq_idx ON hook.customer_message(customer_id, sequence_id, change_id);
