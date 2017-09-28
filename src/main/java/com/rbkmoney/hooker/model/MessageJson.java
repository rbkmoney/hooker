package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.handler.poller.impl.AbstractInvoiceEventHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
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
        paymentStatusesMapping.put("refunded", "PaymentRefunded");
        paymentStatusesMapping.put("failed", "PaymentFailed");
    }

    private long eventID;
    private String occuredAt;
    private String topic;
    private String eventType;
    private Invoice invoice;

    public MessageJson() {
    }

    public long getEventID() {
        return eventID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    public String getOccuredAt() {
        return occuredAt;
    }

    public void setOccuredAt(String occuredAt) {
        this.occuredAt = occuredAt;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

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

    static class InvoiceMessageJson extends MessageJson{
    }

    static class PaymentMessageJson extends MessageJson {
        Payment payment;

        public PaymentMessageJson(Payment payment) {
            this.payment = payment;
        }

        public PaymentMessageJson() {
        }

        public Payment getPayment() {
            return payment;
        }

        public void setPayment(Payment payment) {
            this.payment = payment;
        }
    }
}

