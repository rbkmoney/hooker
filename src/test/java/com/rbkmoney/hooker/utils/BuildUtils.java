package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.InvoiceStatus;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.swag_webhook_events.model.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class BuildUtils {
    private static int messageId = 1;

    public static InvoicingMessage buildMessage(String type, String invoiceId, String partyId, EventType eventType, InvoiceStatusEnum invoiceStatus,  PaymentStatusEnum paymentStatus) {
        return buildMessage(type, invoiceId, partyId, eventType, invoiceStatus, paymentStatus, null, 0);
    }

    public static InvoicingMessage buildMessage(String type, String invoiceId, String partyId, EventType eventType, InvoiceStatusEnum invoiceStatus,  PaymentStatusEnum paymentStatus, Long sequenceId, Integer changeId) {
        InvoicingMessage message = new InvoicingMessage();
        message.setId((long) messageId++);
        message.setEventId((long) messageId++);
        message.setEventTime("2016-03-22T06:12:27Z");
        message.setType(InvoicingMessageEnum.lookup(type));
        message.setPartyId(partyId);
        message.setEventType(eventType);
        message.setInvoiceId(invoiceId);
        message.setShopId("123");
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

    public static com.rbkmoney.damsel.payment_processing.Invoice buildInvoice(String partyId, String invoiceId, String paymentId, String refundId,
                                                                              InvoiceStatus invoiceStatus, InvoicePaymentStatus paymentStatus) throws IOException {
        MockTBaseProcessor tBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        return new com.rbkmoney.damsel.payment_processing.Invoice()
                .setInvoice(new MockTBaseProcessor(MockMode.RANDOM, 15, 1).process(new com.rbkmoney.damsel.domain.Invoice(),
                        new TBaseHandler<>(com.rbkmoney.damsel.domain.Invoice.class))
                        .setId(invoiceId)
                        .setOwnerId(partyId)
                        .setCreatedAt("2016-03-22T06:12:27Z")
                        .setContext(new Content("lel", ByteBuffer.wrap("{\"payment_id\": 271771960}".getBytes())))
                        .setDue("2016-03-22T06:12:27Z")
                        .setStatus(invoiceStatus))
                .setPayments(Collections.singletonList(new InvoicePayment()
                        .setPayment(tBaseProcessor.process(new com.rbkmoney.damsel.domain.InvoicePayment(),
                                new TBaseHandler<>(com.rbkmoney.damsel.domain.InvoicePayment.class))
                                .setCreatedAt("2016-03-22T06:12:27Z")
                                .setId(paymentId)
                                .setOwnerId(partyId)
                                .setStatus(paymentStatus))
                        .setRefunds(Collections.singletonList(tBaseProcessor.process(new com.rbkmoney.damsel.domain.InvoicePaymentRefund(),
                                new TBaseHandler<>(com.rbkmoney.damsel.domain.InvoicePaymentRefund.class))
                                .setReason("keksik")
                                .setCreatedAt("2016-03-22T06:12:27Z")
                                .setId(refundId)))));
    }

    public static CustomerMessage buildCustomerMessage(Long eventId, String partyId, EventType eventType, CustomerMessageEnum type, String custId, String shopId, Customer.StatusEnum custStatus){
        CustomerMessage customerMessage = new CustomerMessage();
        customerMessage.setEventId(eventId);
        customerMessage.setPartyId(partyId);
        customerMessage.setEventTime("2018-03-22T06:12:27Z");
        customerMessage.setEventType(eventType);
        customerMessage.setType(type);
        customerMessage.setCustomerId(custId);
        customerMessage.setShopId(shopId);

        if (customerMessage.isBinding()) {
            customerMessage.setBindingId("12456");
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
