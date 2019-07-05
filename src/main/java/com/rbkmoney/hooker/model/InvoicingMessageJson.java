package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rbkmoney.swag_webhook_events.Event;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
@JsonPropertyOrder({"eventID", "occuredAt", "topic", "eventType", "invoice"})
@NoArgsConstructor
@Getter
@Setter
public class InvoicingMessageJson {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);

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
        paymentStatusesMapping.put("refunded", "PaymentRefunded");
        paymentStatusesMapping.put("failed", "PaymentFailed");
    }

    private static Map<String, String> refundStatusesMapping = new HashMap<>();
    static {
        refundStatusesMapping.put("pending", "RefundCreated");
        refundStatusesMapping.put("succeeded", "RefundSucceeded");
        refundStatusesMapping.put("failed", "RefundFailed");
    }

    private Long eventID;
    private String occuredAt;
    private String topic;
    private String eventType;
    private Invoice invoice;

    public static String buildMessageJson(InvoicingMessage message) throws JsonProcessingException {
        InvoicingMessageJson invoicingMessageJson = null;
        if (message.isInvoice()) {
            invoicingMessageJson = new InvoiceMessageJson();
            invoicingMessageJson.eventType = invoiceStatusesMapping.get(message.getInvoice().getStatus());
        } else if (message.isPayment()) {
            invoicingMessageJson = new PaymentMessageJson(message);
            invoicingMessageJson.eventType = paymentStatusesMapping.get(message.getPayment().getStatus());
        } else if (message.isRefund()) {
            invoicingMessageJson = new RefundMessageJson(message);
            invoicingMessageJson.eventType = refundStatusesMapping.get(message.getRefund().getStatus());
        }
        if (invoicingMessageJson == null) {
            throw new NullPointerException("Message is not Refund or Payment or Invoice: " + message);
        }
        invoicingMessageJson.eventID = message.getEventId();
        invoicingMessageJson.occuredAt = message.getEventTime();
        invoicingMessageJson.topic = Event.TopicEnum.INVOICESTOPIC.getValue();
        invoicingMessageJson.invoice = message.getInvoice();

        return objectMapper.writeValueAsString(invoicingMessageJson);
    }

    private static class InvoiceMessageJson extends InvoicingMessageJson {
    }

    @NoArgsConstructor
    @Getter
    @Setter
    static class PaymentMessageJson extends InvoicingMessageJson {
        Payment payment;

        public PaymentMessageJson(InvoicingMessage message) {
            this.payment = message.getPayment();
        }
    }

    @Getter
    @Setter
    static class RefundMessageJson extends PaymentMessageJson {
        Refund refund;

        public RefundMessageJson(InvoicingMessage message) {
            super(message);
            refund = message.getRefund();
        }
    }
}

