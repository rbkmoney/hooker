package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.domain.OperationTimeout;
import com.rbkmoney.damsel.domain.SubFailure;
import com.rbkmoney.swag_webhook_events.PaymentError;
import com.rbkmoney.swag_webhook_events.PaymentErrorSubError;
import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorUtilsTest {

    @Test
    public void getPaymentError() {
        OperationFailure operationFailure = new OperationFailure();
        operationFailure.setOperationTimeout(new OperationTimeout());
        assertEquals("408", ErrorUtils.getPaymentError(operationFailure).getCode());

        OperationFailure operationFailure1 = new OperationFailure();
        Failure value = new Failure();
        operationFailure1.setFailure(value);
        value.setCode("code");
        value.setReason("reason");
        SubFailure sub = new SubFailure();
        value.setSub(sub);
        sub.setCode("sub_code");
        assertEquals("sub_code", ErrorUtils.getPaymentError(operationFailure1).getSubError().getCode());
    }

    @Test
    public void toStringFailure() {
        PaymentError paymentError = new PaymentError();
        paymentError.setCode("code");
        paymentError.setMessage("mess");
        PaymentErrorSubError subError = new PaymentErrorSubError();
        subError.setCode("sub_error");
        paymentError.setSubError(subError);
        assertEquals("code:sub_error", ErrorUtils.toStringFailure(paymentError));
    }

    @Test
    public void toPaymentError() {
        assertEquals("sub_code", ErrorUtils.toPaymentError("code:sub_code", "message").getSubError().getCode());
    }
}