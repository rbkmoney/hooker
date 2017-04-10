package com.rbkmoney.hooker.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rbkmoney.damsel.base.Content;

/**
 * Created by inalarsanukaev on 07.04.17.
 */
public class InvoiceInfo {
    private long eventId;
    @JsonProperty("invoice_id")
    private String invoiceId;
    private String partyId;
    private int shopId;
    private long amount;
    private String currency;
    @JsonProperty("created_at")
    private String createdAt;
    private Content metadata;
    private String description;

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

    private String status;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setMetadata(Content metadata) {
        this.metadata = metadata;
    }
}
