package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@NoArgsConstructor
@Getter
@Setter
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
            this.cart = other.cart.stream().map(InvoiceCartPosition::new).collect(Collectors.toList());
        }
    }
}
