package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.rbkmoney.damsel.base.Content;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
@JsonPropertyOrder({ "event_type", "invoice_id", "payment_id", "shop_id", "amount", "currency", "created_at", "metadata", "status" })
@JsonIgnoreProperties({ "id", "eventId", "partyId", "eventType"})

public class Message {
    private long id;
    private long eventId;
    @JsonProperty("event_type")
    private String type;
    @JsonProperty("invoice_id")
    private String invoiceId;
    @JsonProperty("payment_id")
    private String paymentId;
    private String partyId;
    @JsonProperty("shop_id")
    private int shopId;
    private long amount;
    private String currency;
    @JsonProperty("created_at")
    private String createdAt;
    private Content metadata;
    private String status;
    private EventType eventType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public long getEventId() {
        return eventId;
    }
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Content getMetadata() {
        return metadata;
    }

    @JsonIgnore
    public void setMetadata(Content metadata) {
        this.metadata = metadata;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
