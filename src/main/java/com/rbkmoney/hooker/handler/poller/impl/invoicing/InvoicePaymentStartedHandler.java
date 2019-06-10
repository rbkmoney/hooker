package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.RecurrentPayer;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.swag_webhook_events.*;
import org.springframework.stereotype.Component;

import static com.rbkmoney.hooker.utils.PaymentToolUtils.getPaymentToolDetails;

@Component
public class InvoicePaymentStartedHandler extends NeedReadInvoiceEventHandler {

    private Filter filter;
    private EventType eventType = EventType.INVOICE_PAYMENT_STARTED;

    public InvoicePaymentStartedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected String getMessageType() {
        return PAYMENT;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, Event event, InvoicingMessage message) {
        InvoicePayment paymentOrigin = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentStarted().getPayment();
        Payment payment = new Payment();
        message.setPayment(payment);
        payment.setId(paymentOrigin.getId());
        payment.setCreatedAt(paymentOrigin.getCreatedAt());
        payment.setStatus(paymentOrigin.getStatus().getSetField().getFieldName());
        payment.setAmount(paymentOrigin.getCost().getAmount());
        payment.setCurrency(paymentOrigin.getCost().getCurrency().getSymbolicCode());
        Content metadata = new Content();
        if (paymentOrigin.isSetContext()) {
            metadata.setType(paymentOrigin.getContext().getType());
            metadata.setData(paymentOrigin.getContext().getData());
        }
        payment.setMetadata(metadata);
        if (paymentOrigin.getPayer().isSetPaymentResource()) {
            com.rbkmoney.damsel.domain.PaymentResourcePayer payerOrigin = paymentOrigin.getPayer().getPaymentResource();
            DisposablePaymentResource resourceOrigin = payerOrigin.getResource();
            PaymentTool paymentTool = resourceOrigin.getPaymentTool();
            payment.setPaymentToolToken(PaymentToolUtils.getPaymentToolToken(paymentTool));
            payment.setPaymentSession(resourceOrigin.getPaymentSessionId());
            payment.setContactInfo(new PaymentContactInfo(payerOrigin.getContactInfo().getEmail(), payerOrigin.getContactInfo().getPhoneNumber()));
            payment.setIp(resourceOrigin.getClientInfo().getIpAddress());
            payment.setFingerprint(resourceOrigin.getClientInfo().getFingerprint());
            com.rbkmoney.swag_webhook_events.PaymentResourcePayer payer = new com.rbkmoney.swag_webhook_events.PaymentResourcePayer()
                    .paymentSession(resourceOrigin.getPaymentSessionId())
                    .paymentToolToken(payment.getPaymentToolToken())
                    .contactInfo(new ContactInfo()
                            .email(payerOrigin.getContactInfo().getEmail())
                            .phoneNumber(payerOrigin.getContactInfo().getPhoneNumber()))
                    .clientInfo(new ClientInfo()
                            .ip(resourceOrigin.getClientInfo().getIpAddress())
                            .fingerprint(resourceOrigin.getClientInfo().getFingerprint()));
            payer.payerType(Payer.PayerTypeEnum.PAYMENTRESOURCEPAYER);
            payer.setPaymentToolDetails(PaymentToolUtils.getPaymentToolDetails(paymentTool));
            payment.setPayer(payer);
        } else if (paymentOrigin.getPayer().isSetCustomer()) {
            com.rbkmoney.damsel.domain.CustomerPayer customerPayerOrigin = paymentOrigin.getPayer().getCustomer();
            payment.setPaymentToolToken(PaymentToolUtils.getPaymentToolToken(customerPayerOrigin.getPaymentTool()));
            payment.setContactInfo(new PaymentContactInfo(customerPayerOrigin.getContactInfo().getEmail(), customerPayerOrigin.getContactInfo().getPhoneNumber()));
            payment.setPayer(new CustomerPayer()
                    .customerID(paymentOrigin.getPayer().getCustomer().getCustomerId())
                    .payerType(Payer.PayerTypeEnum.CUSTOMERPAYER));
        } else if (paymentOrigin.getPayer().isSetRecurrent()) {
            RecurrentPayer recurrentParentOrigin = paymentOrigin.getPayer().getRecurrent();
            payment.setContactInfo(new PaymentContactInfo(recurrentParentOrigin.getContactInfo().getEmail(), recurrentParentOrigin.getContactInfo().getPhoneNumber()));
            payment.setPayer(new com.rbkmoney.swag_webhook_events.RecurrentPayer()
                    .recurrentParentPayment(new PaymentRecurrentParent()
                            .invoiceID(recurrentParentOrigin.getRecurrentParent().getInvoiceId())
                            .paymentID(recurrentParentOrigin.getRecurrentParent().getPaymentId()))
                    .contactInfo(new ContactInfo()
                            .email(recurrentParentOrigin.getContactInfo().getEmail())
                            .phoneNumber(recurrentParentOrigin.getContactInfo().getPhoneNumber()))
                    .payerType(Payer.PayerTypeEnum.RECURRENTPAYER));
            payment.setContactInfo(new PaymentContactInfo(recurrentParentOrigin.getContactInfo().getEmail(), recurrentParentOrigin.getContactInfo().getPhoneNumber()));
        }
    }

    @Override
    protected InvoicingMessage getMessage(String invoiceId, InvoiceChange ic) {
        return messageDao.getInvoice(invoiceId);
    }
}
