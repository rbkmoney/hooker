package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@JsonPropertyOrder({"id", "shopID", "createdAt", "status", "reason", "dueDate", "amount", "currency", "metadata", "product", "description"})
public class Invoice {
    private String id;
    private String shopID;
    private String createdAt;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;
    private String dueDate;
    private long amount;
    private String currency;
    @JsonSerialize(using = MetadataSerializer.class)
    private InvoiceContent metadata;
    private String product;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<InvoiceCartPosition> cart;

    public Invoice(Invoice other) {
        this.id = other.id;
        this.shopID = other.shopID;
        this.createdAt = other.createdAt;
        this.status = other.status;
        this.reason = other.reason;
        this.dueDate = other.dueDate;
        this.amount = other.amount;
        this.currency = other.currency;
        if (other.metadata != null) {
            this.metadata = new InvoiceContent(other.metadata);
        }
        this.product = other.product;
        this.description = other.description;
        if (other.cart != null && !other.cart.isEmpty()) {
            this.cart = other.cart.stream().map(c -> new InvoiceCartPosition(c)).collect(Collectors.toList());
        }
    }

    public Invoice() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
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

    public InvoiceContent getMetadata() {
        return metadata;
    }

    public void setMetadata(InvoiceContent metadata) {
        this.metadata = metadata;
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

    public List<InvoiceCartPosition> getCart() {
        return cart;
    }

    public void setCart(List<InvoiceCartPosition> cart) {
        this.cart = cart;
    }
}
