package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.domain.SubFailure;
import com.rbkmoney.swag_webhook_events.PaymentError;
import com.rbkmoney.swag_webhook_events.PaymentErrorSubError;

public class ErrorUtils {

    public static PaymentError getPaymentError(OperationFailure operationFailure) {
        if (operationFailure.isSetFailure()) {
            Failure failure = operationFailure.getFailure();
            PaymentError paymentError = new PaymentError();
            paymentError.setCode(failure.getCode());
            if (failure.isSetReason()) {
                paymentError.setMessage(failure.getReason());
            } else {
                paymentError.setMessage("Unknown error");
            }
            if (failure.isSetSub()) {
                SubFailure sub = failure.getSub();
                paymentError.setSubError(getSubError(sub));
            }
            return paymentError;
        } else if (operationFailure.isSetOperationTimeout()) {
            PaymentError paymentError = new PaymentError();
            paymentError.setCode("408");
            paymentError.setMessage("Operation timeout");
            return paymentError;
        }
        return null;
    }

    private static PaymentErrorSubError getSubError(SubFailure sub) {
        PaymentErrorSubError paymentErrorSubError = new PaymentErrorSubError();
        paymentErrorSubError.setCode(sub.getCode());
        if (sub.isSetSub()) {
            paymentErrorSubError.setSubError(getSubError(sub.getSub()));
        }
        return paymentErrorSubError;
    }

    public static String toStringFailure(PaymentError paymentError) {
        StringBuilder sb = new StringBuilder(paymentError.getCode());
        PaymentErrorSubError subError = paymentError.getSubError();
        while (subError != null) {
            sb.append(":").append(subError.getCode());
            subError = subError.getSubError();
        }
        return sb.toString();
    }

    public static PaymentError toPaymentError(String failure, String failureReason) {
        String[] codes = failure.split(":");
        PaymentError paymentError = new PaymentError();
        paymentError.setCode(codes[0]);
        paymentError.setMessage(failureReason);
        if (codes.length > 1) {
            PaymentErrorSubError previousSubError = null;
            for (int i = 1; i < codes.length; ++i){
                PaymentErrorSubError subError = new PaymentErrorSubError();
                subError.setCode(codes[i]);
                if (i == 1) {
                    paymentError.setSubError(subError);
                } else {
                    previousSubError.setSubError(subError);
                }
                previousSubError = subError;
            }
        }
        return paymentError;
    }
}
