package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.AdditionalTransactionInfo;
import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.domain.InvoicePaymentCaptured;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.hooker.model.FeeType;
import com.rbkmoney.hooker.utils.CashFlowUtils;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.hooker.utils.TimeUtils;
import com.rbkmoney.swag_webhook_events.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentConverter implements Converter<InvoicePayment, Payment> {

    private final MetadataDeserializer deserializer;

    @Override
    public Payment convert(InvoicePayment sourceWrapper) {
        var source = sourceWrapper.getPayment();

        Payment target = new Payment()
                .id(source.getId())
                .createdAt(TimeUtils.toOffsetDateTime(source.getCreatedAt()))
                .status(Payment.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()))
                .amount(source.getCost().getAmount())
                .currency(source.getCost().getCurrency().getSymbolicCode())
                .metadata(getMetadata(source))
                .fee(getFee(sourceWrapper))
                .rrn(getRrn(sourceWrapper));

        if (source.getStatus().isSetFailed()) {
            setErrorDetails(source, target);
        } else if (source.getStatus().isSetCaptured()) {
            setCapturedParams(source, target);
        }

        if (source.getPayer().isSetPaymentResource()) {
            setResourcePaymentTool(source, target);
        } else if (source.getPayer().isSetCustomer()) {
            setCustomerPaymentTool(source, target);
        } else if (source.getPayer().isSetRecurrent()) {
            setRecurrentPaymentTool(source, target);
        }

        return target;
    }

    private Object getMetadata(com.rbkmoney.damsel.domain.InvoicePayment source) {
        return source.isSetContext() ? deserializer.deserialize(source.getContext().getData()) : null;
    }

    private Long getFee(InvoicePayment sourceWrapper) {
        return sourceWrapper.isSetCashFlow() ? CashFlowUtils.getFees(sourceWrapper.getCashFlow()).getOrDefault(FeeType.FEE, 0L) : 0L;
    }

    private String getRrn(InvoicePayment sourceWrapper) {
        return isSetAdditionalInfo(sourceWrapper) ? getAdditionalInfo(sourceWrapper).getRrn() : null;
    }

    private void setErrorDetails(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        target.setError(ErrorUtils.getPaymentError(source.getStatus().getFailed().getFailure()));
    }

    private void setCapturedParams(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        InvoicePaymentCaptured invoicePaymentCaptured = source.getStatus().getCaptured();
        if (invoicePaymentCaptured.isSetCost()) {
            target.setAmount(invoicePaymentCaptured.getCost().getAmount());
            target.setCurrency(invoicePaymentCaptured.getCost().getCurrency().getSymbolicCode());
        }
    }

    private void setResourcePaymentTool(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        com.rbkmoney.damsel.domain.PaymentResourcePayer payerOrigin = source.getPayer().getPaymentResource();
        DisposablePaymentResource resourceOrigin = payerOrigin.getResource();
        PaymentTool paymentTool = resourceOrigin.getPaymentTool();
        target.paymentToolToken(PaymentToolUtils.getPaymentToolToken(paymentTool))
                .paymentSession(resourceOrigin.getPaymentSessionId())
                .contactInfo(new PaymentContactInfo()
                        .email(payerOrigin.getContactInfo().getEmail())
                        .phoneNumber(payerOrigin.getContactInfo().getPhoneNumber()))
                .ip(resourceOrigin.isSetClientInfo() ? resourceOrigin.getClientInfo().getIpAddress() : null)
                .fingerprint(resourceOrigin.isSetClientInfo() ? resourceOrigin.getClientInfo().getFingerprint() : null)
                .payer(new PaymentResourcePayer()
                        .paymentSession(resourceOrigin.getPaymentSessionId())
                        .paymentToolToken(target.getPaymentToolToken())
                        .contactInfo(new ContactInfo()
                                .email(payerOrigin.getContactInfo().getEmail())
                                .phoneNumber(payerOrigin.getContactInfo().getPhoneNumber()))
                        .clientInfo(new ClientInfo()
                                .ip(resourceOrigin.isSetClientInfo() ? resourceOrigin.getClientInfo().getIpAddress() : null)
                                .fingerprint(resourceOrigin.isSetClientInfo() ? resourceOrigin.getClientInfo().getFingerprint() : null))
                        .paymentToolDetails(PaymentToolUtils.getPaymentToolDetails(paymentTool)));
    }

    private void setRecurrentPaymentTool(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        com.rbkmoney.damsel.domain.RecurrentPayer recurrentParentOrigin = source.getPayer().getRecurrent();
        target.contactInfo(new PaymentContactInfo()
                .email(recurrentParentOrigin.getContactInfo().getEmail())
                .phoneNumber(recurrentParentOrigin.getContactInfo().getPhoneNumber()))
                .payer(new RecurrentPayer()
                        .recurrentParentPayment(new PaymentRecurrentParent()
                                .invoiceID(recurrentParentOrigin.getRecurrentParent().getInvoiceId())
                                .paymentID(recurrentParentOrigin.getRecurrentParent().getPaymentId()))
                        .contactInfo(new ContactInfo()
                                .email(recurrentParentOrigin.getContactInfo().getEmail())
                                .phoneNumber(recurrentParentOrigin.getContactInfo().getPhoneNumber())))
                .contactInfo(new PaymentContactInfo()
                        .email(recurrentParentOrigin.getContactInfo().getEmail())
                        .phoneNumber(recurrentParentOrigin.getContactInfo().getPhoneNumber()));
    }

    private void setCustomerPaymentTool(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        com.rbkmoney.damsel.domain.CustomerPayer customerPayerOrigin = source.getPayer().getCustomer();
        target.paymentToolToken(PaymentToolUtils.getPaymentToolToken(customerPayerOrigin.getPaymentTool()))
                .contactInfo(new PaymentContactInfo()
                        .email(customerPayerOrigin.getContactInfo().getEmail())
                        .phoneNumber(customerPayerOrigin.getContactInfo().getPhoneNumber()))
                .payer(new CustomerPayer()
                        .customerID(source.getPayer().getCustomer().getCustomerId()));
    }

    private boolean isSetAdditionalInfo(InvoicePayment sourceWrapper) {
        return (!sourceWrapper.getSessions().isEmpty())
                && sourceWrapper.getSessions().get(0).isSetTransactionInfo()
                && sourceWrapper.getSessions().get(0).getTransactionInfo().isSetAdditionalInfo();
    }

    private AdditionalTransactionInfo getAdditionalInfo(InvoicePayment sourceWrapper) {
        return sourceWrapper.getSessions().get(0).getTransactionInfo().getAdditionalInfo();
    }
}
