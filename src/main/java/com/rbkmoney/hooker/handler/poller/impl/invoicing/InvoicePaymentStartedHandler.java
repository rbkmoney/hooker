package com.rbkmoney.hooker.handler.poller.impl.invoicing;

import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.LegacyPayerDetails;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.PaymentContactInfo;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.swag_webhook_events.*;
import org.springframework.stereotype.Component;

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
    protected void modifyMessage(InvoiceChange ic, Event event, Message message) {
        InvoicePayment paymentOrigin = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentStarted().getPayment();
        Payment payment = new Payment();
        message.setPayment(payment);
        payment.setId(paymentOrigin.getId());
        payment.setCreatedAt(paymentOrigin.getCreatedAt());
        payment.setStatus(paymentOrigin.getStatus().getSetField().getFieldName());
        payment.setAmount(paymentOrigin.getCost().getAmount());
        payment.setCurrency(paymentOrigin.getCost().getCurrency().getSymbolicCode());
        LegacyPayerDetails payerDetails = paymentOrigin.getPayerDetails();
        payment.setPaymentToolToken(PaymentToolUtils.getPaymentToolToken(payerDetails.getPaymentTool()));
        payment.setPaymentSession(payerDetails.getSessionId());
        payment.setContactInfo(new PaymentContactInfo(payerDetails.getContactInfo().getEmail(), payerDetails.getContactInfo().getPhoneNumber()));
        payment.setIp(payerDetails.getClientInfo().getIpAddress());
        payment.setFingerprint(payerDetails.getClientInfo().getFingerprint());
        if (paymentOrigin.getPayer().isSetPaymentResource()) {
            com.rbkmoney.damsel.domain.PaymentResourcePayer payerOrigin = paymentOrigin.getPayer().getPaymentResource();
            DisposablePaymentResource resourceOrigin = payerOrigin.getResource();
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
            payment.setPayer(payer);

            if (resourceOrigin.getPaymentTool().isSetPaymentTerminal()) {
                payer.setPaymentToolDetails(new PaymentToolDetailsPaymentTerminal()
                        .provider(PaymentToolDetailsPaymentTerminal.ProviderEnum.fromValue(resourceOrigin.getPaymentTool().getPaymentTerminal().getTerminalType().name()))
                        .detailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSPAYMENTTERMINAL));
            } else if (resourceOrigin.getPaymentTool().isSetBankCard()) {
                BankCard bankCard = resourceOrigin.getPaymentTool().getBankCard();
                payer.setPaymentToolDetails(new PaymentToolDetailsBankCard()
                        .cardNumberMask(bankCard.getMaskedPan())
                        .paymentSystem(bankCard.getPaymentSystem().name())
                        .detailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSBANKCARD));
            }
        } else if (paymentOrigin.getPayer().isSetCustomer()) {
            payment.setPayer(new CustomerPayer()
                    .customerID(paymentOrigin.getPayer().getCustomer().getCustomerId())
                    .payerType(Payer.PayerTypeEnum.CUSTOMERPAYER));
        }
    }

    @Override
    protected Message getMessage(String invoiceId) {
        return messageDao.getAny(invoiceId, INVOICE);
    }
}
