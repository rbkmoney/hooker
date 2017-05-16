package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"id", "shopID", "createdAt", "status", "reason", "dueDate", "amount", "currency", "metadata", "product", "description"})
public class Invoice implements Serializable{
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
}
