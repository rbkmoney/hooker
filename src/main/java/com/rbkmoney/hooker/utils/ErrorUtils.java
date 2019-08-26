package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.domain.SubFailure;
import com.rbkmoney.swag_webhook_events.model.PaymentError;
import com.rbkmoney.swag_webhook_events.model.SubError;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorUtils {

    public static PaymentError getPaymentError(OperationFailure operationFailure) {
        if (operationFailure.isSetFailure()) {
            Failure failure = operationFailure.getFailure();
            PaymentError paymentError = new PaymentError();
            paymentError.setCode(failure.getCode());
            paymentError.setMessage(failure.getCode());
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

    private static SubError getSubError(SubFailure sub) {
        SubError paymentErrorSubError = new SubError();
        paymentErrorSubError.setCode(sub.getCode());
        if (sub.isSetSub()) {
            paymentErrorSubError.setSubError(getSubError(sub.getSub()));
        }
        return paymentErrorSubError;
    }

    public static String toStringFailure(PaymentError paymentError) {
        StringBuilder sb = new StringBuilder(paymentError.getCode());
        SubError subError = paymentError.getSubError();
        while (subError != null) {
            sb.append(":").append(subError.getCode());
            subError = subError.getSubError();
        }
        return sb.toString();
    }

    public static PaymentError toPaymentError(String failure, String failureReason) {
        String[] codes = failure.split(":");

        return new PaymentError().code(codes[0]).message(failureReason).subError(getSubErrorTree(codes));
    }

    private static SubError getSubErrorTree(String[] codes) {
        if (codes.length == 1)
            return null;

        List<SubError> subErrors = Arrays.stream(codes)
                .map(code -> new SubError().code(code))
                .collect(Collectors.toList());

        for (int i = 1; i < subErrors.size() - 1; i++) {
            subErrors.get(i).setSubError(subErrors.get(i + 1));
        }

        return subErrors.get(1);
    }
}
