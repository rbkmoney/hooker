package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.swag_webhook_events.model.*;

import java.util.*;

/**
 * Created by jeckep on 25.04.17.
 */
public class BuildUtils {
    private static int messageId = 1;

    public static InvoicingMessage buildMessage(String type, String invoiceId, String partyId, EventType eventType, InvoiceStatusEnum invoiceStatus,  PaymentStatusEnum paymentStatus) {
        return buildMessage(type, invoiceId, partyId, eventType, invoiceStatus, paymentStatus, null, 0);
    }

    public static InvoicingMessage buildMessage(String type, String invoiceId, String partyId, EventType eventType, InvoiceStatusEnum invoiceStatus,  PaymentStatusEnum paymentStatus, Long sequenceId, Integer changeId) {
        InvoicingMessage message = new InvoicingMessage();
        message.setId((long) messageId++);
        message.setEventId((long) messageId++);
        message.setEventTime("time");
        message.setType(type);
        message.setPartyId(partyId);
        message.setEventType(eventType);
        message.setInvoiceId(invoiceId);
        message.setShopID("123");
        message.setInvoiceStatus(invoiceStatus);
        if (message.isPayment() || message.isRefund()) {
            message.setPaymentId("123");
            message.setPaymentStatus(paymentStatus);
            message.setPaymentFee(1L);
        }

        if (message.isRefund()) {
            message.setRefundId("123");
            message.setRefundAmount(115L);
            message.setRefundCurrency("RUB");
            message.setRefundStatus(RefundStatusEnum.succeeded);
        }
        message.setSequenceId(sequenceId);
        message.setChangeId(changeId);
        return message;
    }

    public static CustomerMessage buildCustomerMessage(Long eventId, String partyId, EventType eventType, String type, String custId, String shopId, Customer.StatusEnum custStatus){
        CustomerMessage customerMessage = new CustomerMessage();
        customerMessage.setEventId(eventId);
        customerMessage.setPartyId(partyId);
        customerMessage.setOccuredAt("time");
        customerMessage.setEventType(eventType);
        customerMessage.setType(type);
        customerMessage.setCustomer(new Customer()
                .id(custId)
                .shopID(shopId)
                .status(custStatus)
                .contactInfo(new ContactInfo().phoneNumber("1234").email("aaa@mail.ru"))
                .metadata(CustomerUtils.getJsonObject("{\"field1\":\"value1\",\"field2\":[123,123,123]}")));

        if (customerMessage.isBinding()) {
            CustomerBinding customerBinding = new CustomerBinding();
            customerBinding.status(CustomerBinding.StatusEnum.PENDING);
            customerMessage.setCustomerBinding(customerBinding
                    .id("12456")
                    .paymentResource(new PaymentResource()
                            .paymentToolToken("shjfbergiwengriweno")
                            .paymentSession("wrgnjwierngweirngi")
                            .clientInfo(new ClientInfo().ip("127.0.0.1").fingerprint("finger"))
                            .paymentToolDetails(new PaymentToolDetailsBankCard()
                                    .bin("440088")
                                    .lastDigits("1234")
                                    .cardNumberMask("440088******1234")
                                    .paymentSystem("visa")
                                    .tokenProvider(PaymentToolDetailsBankCard.TokenProviderEnum.APPLEPAY)
                                    .detailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSBANKCARD)
            )));
        }
        return customerMessage;
    }

    public static Hook buildHook(String partyId, String url, EventType... types) {
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setTopic(Event.TopicEnum.INVOICESTOPIC.getValue());
        hook.setUrl(url);

        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        for (EventType type : types) {
            webhookAdditionalFilters.add(WebhookAdditionalFilter.builder().eventType(type).build());
        }
        hook.setFilters(webhookAdditionalFilters);
        return hook;
    }
}
