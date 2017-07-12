package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.handler.poller.impl.AbstractInvoiceEventHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"eventID", "occuredAt", "topic", "eventType", "invoice"})
public class MessageJson {
    public static final String INVOICES_TOPIC = "InvoicesTopic";
    private static Map<String, String> invoiceStatusesMapping = new HashMap<>();
    static {
        invoiceStatusesMapping.put("unpaid", "InvoiceCreated");
        invoiceStatusesMapping.put("paid", "InvoicePaid");
        invoiceStatusesMapping.put("cancelled", "InvoiceCancelled");
        invoiceStatusesMapping.put("fulfilled", "InvoiceFulfilled");
    }

    private static Map<String, String> paymentStatusesMapping = new HashMap<>();
    static {
        paymentStatusesMapping.put("pending", "PaymentStarted");
        paymentStatusesMapping.put("processed", "PaymentProcessed");
        paymentStatusesMapping.put("captured", "PaymentCaptured");
        paymentStatusesMapping.put("cancelled", "PaymentCancelled");
        paymentStatusesMapping.put("failed", "PaymentFailed");
    }

    private long eventID;
    private String occuredAt;
    private String topic;
    private String eventType;
    private Invoice invoice;

    public static String buildMessageJson(Message message) throws JsonProcessingException {
        boolean isInvoice = AbstractInvoiceEventHandler.INVOICE.equals(message.getType());
        MessageJson messageJson = isInvoice ?  new InvoiceMessageJson() : new PaymentMessageJson(message.getPayment());
        messageJson.eventID = message.getEventId();
        messageJson.occuredAt = message.getEventTime();
        messageJson.topic = INVOICES_TOPIC;
        messageJson.invoice = message.getInvoice();

        messageJson.eventType = isInvoice ? invoiceStatusesMapping.get(message.getInvoice().getStatus()) : paymentStatusesMapping.get(message.getPayment().getStatus()) ;
        return new ObjectMapper().writeValueAsString(messageJson);
    }

    @Data
    @AllArgsConstructor
    static class InvoiceMessageJson extends MessageJson{
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class PaymentMessageJson extends MessageJson {
        Payment payment;
    }
}

