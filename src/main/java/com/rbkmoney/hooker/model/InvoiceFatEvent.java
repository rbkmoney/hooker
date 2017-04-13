package com.rbkmoney.hooker.model;

import com.rbkmoney.damsel.base.Content;

/**
 * Created by jeckep on 13.04.17.
 */

public class InvoiceFatEvent extends Event {
    private String paymentId;
    private String partyId;
    private int shopId;
    private long amount;
    private String currency;
    private String createdAt;
    private Content metadata;
    private String description;

    public InvoiceFatEvent(long id, String code, String status, String invoceId, String paymentId, String partyId, int shopId, long amount, String currency, String createdAt, Content metadata, String description) {
        super(id, code, status, invoceId);
        this.paymentId = paymentId;
        this.partyId = partyId;
        this.shopId = shopId;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = createdAt;
        this.metadata = metadata;
        this.description = description;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPartyId() {
        return partyId;
    }

    public int getShopId() {
        return shopId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Content getMetadata() {
        return metadata;
    }

    public String getDescription() {
        return description;
    }
}
