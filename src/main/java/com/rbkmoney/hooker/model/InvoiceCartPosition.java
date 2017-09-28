package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by inalarsanukaev on 27.09.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceCartPosition {
    private String product;
    private Long price;
    private int quantity;
    private Long cost;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TaxMode taxMode;

    public InvoiceCartPosition(InvoiceCartPosition other) {
        this.product = other.product;
        this.price = other.price;
        this.quantity = other.quantity;
        this.cost = other.cost;
        if (other.taxMode != null) {
            this.taxMode = new TaxMode(other.taxMode.getRate());
        }
    }
}
