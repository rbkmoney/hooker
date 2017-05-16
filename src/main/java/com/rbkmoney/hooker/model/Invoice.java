package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"id", "shopID", "createdAt", "status", "reason", "dueDate", "amount", "currency", "metadata", "product", "description"})
public class Invoice {
    private String id;
    private int shopID;
    private String createdAt;
    private String status;
    private String reason;
    private String dueDate;
    private long amount;
    private String currency;
    private InvoiceContent metadata;
    private String product;
    private String description;

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
    }
}
