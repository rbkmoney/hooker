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
@JsonPropertyOrder({ "eventId", "eventTime", "eventType", "payload"})
public class MessageJson {

    private static Map<String, String> invoiceStatuses = new HashMap<>();
    static {
        invoiceStatuses.put("unpaid", "InvoiceCreated");
        invoiceStatuses.put("paid", "InvoicePaid");
        invoiceStatuses.put("cancelled", "InvoiceCancelled");
        invoiceStatuses.put("fulfilled", "InvoiceFulfilled");
    }

    private static Map<String, String> paymentStatuses = new HashMap<>();
    static {
        paymentStatuses.put("pending", "PaymentStarted");
        paymentStatuses.put("processed", "PaymentProcessed");
        paymentStatuses.put("captured", "PaymentCaptured");
        paymentStatuses.put("cancelled", "PaymentCancelled");
        paymentStatuses.put("failed", "PaymentFailed");
    }

    private long eventId;
    private String eventTime;
    private String eventType;
    private Payload payload;

    public MessageJson() {
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public static String buildMessageJson(Message message) throws JsonProcessingException {
        MessageJson messageJson = new MessageJson();
        messageJson.eventId = message.getEventId();
        messageJson.eventTime = message.getEventTime();
        boolean isInvoice = AbstractInvoiceEventHandler.INVOICE.equals(message.getType());
        messageJson.eventType = isInvoice ?  invoiceStatuses.get(message.getStatus()) : paymentStatuses.get(message.getStatus()) ;
        AbstaractInvoicingPayload invPayload = isInvoice ? new InvoicePayload(message.getProduct(), message.getDescription()) : new PaymentPayload(message.getPaymentId());
        invPayload.setAmount(message.getAmount());
        invPayload.setCreatedAt(message.getCreatedAt());
        invPayload.setCurrency(message.getCurrency());
        if (message.getMetadata() != null) {
            Content metadata = new Content();
            metadata.setType(message.getMetadata().getType());
            metadata.setData(message.getMetadata().getData());
            invPayload.setMetadata(metadata);
        }
        invPayload.setShopId(message.getShopId());
        invPayload.setPartyId(message.getPartyId());
        invPayload.setInvoiceId(message.getInvoiceId());
        invPayload.setStatus(message.getStatus());
        messageJson.payload = invPayload;
        return new ObjectMapper().writeValueAsString(messageJson);
    }
}

class Payload {
    private String payloadType;

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }
}

class Content {
    public String type;
    public byte[] data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

abstract class AbstaractInvoicingPayload extends Payload {
    private long amount;
    private String createdAt;
    private String currency;
    private String invoiceId;
    private Content metadata;
    private int shopId;
    private String partyId;
    private String status;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Content getMetadata() {
        return metadata;
    }

    public void setMetadata(Content metadata) {
        this.metadata = metadata;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

@JsonPropertyOrder({"payloadType", "amount", "createdAt", "currency", "invoiceId", "metadata", "shopId", "partyId", "status", "product", "description"})
class InvoicePayload extends AbstaractInvoicingPayload{
    private String product;
    private String description;

    public InvoicePayload() {
    }

    public InvoicePayload(String product, String description) {
        this.setPayloadType("InvoiceInfo");
        this.product = product;
        this.description = description;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

@JsonPropertyOrder({"payloadType", "amount", "createdAt", "currency", "invoiceId", "metadata", "paymentId", "shopId", "partyId", "status"})
class PaymentPayload extends AbstaractInvoicingPayload {
    private String paymentId;

    public PaymentPayload() {
    }

    public PaymentPayload(String paymentId) {
        this.setPayloadType("PaymentInfo");
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}
