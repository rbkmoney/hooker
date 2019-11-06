package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.InvoicePaymentCaptured;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.swag_webhook_events.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class PaymentConverter implements Converter<InvoicePayment, Payment> {

    private final MetadataDeserializer deserializer;

    @Override
    public Payment convert(InvoicePayment source) {
        Payment target = new Payment()
                .createdAt(OffsetDateTime.parse(source.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME))
                .status(Payment.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()))
                .amount(source.getCost().getAmount())
                .currency(source.getCost().getCurrency().getSymbolicCode())
                .metadata(source.isSetContext() ? deserializer.deserialize(source.getContext().getData()) : null);

        if (source.getStatus().isSetFailed()) {
            target.setError(ErrorUtils.getPaymentError(source.getStatus().getFailed().getFailure()));
        } else if (source.getStatus().isSetCaptured()) {
            InvoicePaymentCaptured invoicePaymentCaptured = source.getStatus().getCaptured();
            if (invoicePaymentCaptured.isSetCost()) {
                target.setAmount(invoicePaymentCaptured.getCost().getAmount());
                target.setCurrency(invoicePaymentCaptured.getCost().getCurrency().getSymbolicCode());
            }
        }

        if (source.getPayer().isSetPaymentResource()) {
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
                                    .ip(resourceOrigin.getClientInfo().getIpAddress())
                                    .fingerprint(resourceOrigin.getClientInfo().getFingerprint()))
                            .paymentToolDetails(PaymentToolUtils.getPaymentToolDetails(paymentTool)));
        } else if (source.getPayer().isSetCustomer()) {
            com.rbkmoney.damsel.domain.CustomerPayer customerPayerOrigin = source.getPayer().getCustomer();
            target.paymentToolToken(PaymentToolUtils.getPaymentToolToken(customerPayerOrigin.getPaymentTool()))
                    .contactInfo(new PaymentContactInfo()
                            .email(customerPayerOrigin.getContactInfo().getEmail())
                            .phoneNumber(customerPayerOrigin.getContactInfo().getPhoneNumber()))
                    .payer(new CustomerPayer()
                            .customerID(source.getPayer().getCustomer().getCustomerId()));
        } else if (source.getPayer().isSetRecurrent()) {
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
        return target;
    }
}
