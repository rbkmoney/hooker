package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by inalarsanukaev on 27.09.17.
 */

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

    public InvoiceCartPosition() {
    }

    public InvoiceCartPosition(String product, Long price, int quantity, Long cost, TaxMode taxMode) {
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.cost = cost;
        this.taxMode = taxMode;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public TaxMode getTaxMode() {
        return taxMode;
    }

    public void setTaxMode(TaxMode taxMode) {
        this.taxMode = taxMode;
    }
}
