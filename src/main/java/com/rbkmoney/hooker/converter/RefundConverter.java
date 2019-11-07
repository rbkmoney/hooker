package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.TimeUtils;
import com.rbkmoney.swag_webhook_events.model.Refund;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RefundConverter implements Converter<InvoicePaymentRefund, Refund> {
    @Override
    public Refund convert(InvoicePaymentRefund source) {
        return new Refund()
                .id(source.getId())
                .createdAt(TimeUtils.toOffsetDateTime(source.getCreatedAt()))
                .reason(source.getReason())
                .status(Refund.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()))
                .error(source.getStatus().isSetFailed() ? ErrorUtils.getRefundError(source.getStatus().getFailed().getFailure()) : null);
    }
}
