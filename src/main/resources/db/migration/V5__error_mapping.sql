UPDATE hook.message SET payment_error_code = replace(payment_error_code, ':', '');
ALTER TABLE hook.message RENAME payment_error_code TO payment_failure;
ALTER TABLE hook.message RENAME payment_error_message TO payment_failure_reason;

UPDATE hook.message SET refund_error_code = replace(refund_error_code, ':', '');
ALTER TABLE hook.message RENAME refund_error_code TO refund_failure;
ALTER TABLE hook.message RENAME refund_error_message TO refund_failure_reason;