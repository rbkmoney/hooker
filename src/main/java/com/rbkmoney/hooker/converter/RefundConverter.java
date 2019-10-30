package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.swag_webhook_events.model.Refund;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RefundConverter implements Converter<InvoicePaymentRefund, Refund> {
    @Override
    public Refund convert(InvoicePaymentRefund source) {
        Refund target = new Refund();
        target.setCreatedAt(OffsetDateTime.parse(source.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME));
        target.setReason(source.getReason());
        target.setStatus(Refund.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()));
        if (source.getStatus().isSetFailed()) {
            target.setError(ErrorUtils.getRefundError(source.getStatus().getFailed().getFailure()));
        }
        return target;
    }
}
