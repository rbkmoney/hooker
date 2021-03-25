ALTER TABLE hook.message ADD COLUMN sequence_id int;
ALTER TABLE hook.message ADD COLUMN change_id int;

ALTER TABLE hook.customer_message ADD COLUMN sequence_id int;
ALTER TABLE hook.customer_message ADD COLUMN change_id int;