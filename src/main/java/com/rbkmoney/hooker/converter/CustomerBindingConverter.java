package com.rbkmoney.hooker.converter;

import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.swag_webhook_events.model.ClientInfo;
import com.rbkmoney.swag_webhook_events.model.CustomerBinding;
import com.rbkmoney.swag_webhook_events.model.PaymentResource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerBindingConverter implements Converter<com.rbkmoney.damsel.payment_processing.CustomerBinding, CustomerBinding> {

    @Override
    public CustomerBinding convert(com.rbkmoney.damsel.payment_processing.CustomerBinding source) {
        return new CustomerBinding()
                .status(CustomerBinding.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()))
                .error(source.getStatus().isSetFailed() ? ErrorUtils.getCustomerBindingError(source.getStatus().getFailed().getFailure()) : null)
                .id(source.getId())
                .paymentResource(new PaymentResource()
                        .paymentSession(source.getPaymentResource().getPaymentSessionId())
                        .clientInfo(new ClientInfo()
                                .ip(source.getPaymentResource().getClientInfo().getIpAddress())
                                .fingerprint(source.getPaymentResource().getClientInfo().getFingerprint()))
                        .paymentToolDetails(PaymentToolUtils.getPaymentToolDetails(source.getPaymentResource().getPaymentTool())));
    }
}
