package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.AdditionalTransactionInfo;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.hooker.model.FeeType;
import com.rbkmoney.hooker.utils.CashFlowUtils;
import com.rbkmoney.hooker.utils.TimeUtils;
import com.rbkmoney.swag_webhook_events.model.Refund;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static com.rbkmoney.hooker.utils.ErrorUtils.getRefundError;

@Component
public class RefundConverter implements Converter<InvoicePaymentRefund, Refund> {
    @Override
    public Refund convert(InvoicePaymentRefund sourceWrapper) {
        var source = sourceWrapper.getRefund();

        return new Refund()
                .id(source.getId())
                .createdAt(TimeUtils.toOffsetDateTime(source.getCreatedAt()))
                .reason(source.getReason())
                .status(Refund.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()))
                .error(source.getStatus().isSetFailed() ? getRefundError(source.getStatus().getFailed().getFailure()) : null)
                .amount(getAmount(sourceWrapper))
                .currency(getCurrency(sourceWrapper))
                .rrn(getRrn(sourceWrapper));
    }

    private Long getAmount(InvoicePaymentRefund sourceWrapper) {
        return sourceWrapper.isSetCashFlow() ? CashFlowUtils.getFees(sourceWrapper.getCashFlow()).getOrDefault(FeeType.AMOUNT, null) : null;
    }

    private String getCurrency(InvoicePaymentRefund sourceWrapper) {
        return sourceWrapper.isSetCashFlow() ? CashFlowUtils.getCurrency(sourceWrapper.getCashFlow()).getOrDefault(FeeType.AMOUNT, null) : null;
    }

    private String getRrn(InvoicePaymentRefund sourceWrapper) {
        return isSetAdditionalInfo(sourceWrapper) ? getAdditionalInfo(sourceWrapper).getRrn() : null;
    }

    private boolean isSetAdditionalInfo(InvoicePaymentRefund damselRefund) {
        return !damselRefund.getSessions().isEmpty()
                && damselRefund.getSessions().get(0).isSetTransactionInfo()
                && damselRefund.getSessions().get(0).getTransactionInfo().isSetAdditionalInfo()
                && damselRefund.getSessions().get(0).getTransactionInfo().getAdditionalInfo().isSetRrn();
    }

    private AdditionalTransactionInfo getAdditionalInfo(InvoicePaymentRefund damselRefund) {
        return damselRefund.getSessions().get(0).getTransactionInfo().getAdditionalInfo();
    }
}
