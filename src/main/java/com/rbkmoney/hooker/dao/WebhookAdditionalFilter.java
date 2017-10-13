package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.EventType;

/**
 * Created by inalarsanukaev on 18.04.17.
 */
public class WebhookAdditionalFilter {
    private EventType eventType;
    private String shopId;
    private String invoiceStatus;
    private String invoicePaymentStatus;

    public WebhookAdditionalFilter(EventType eventType, String shopId, String invoiceStatus, String invoicePaymentStatus) {
        this.eventType = eventType;
        this.shopId = shopId;
        this.invoiceStatus = invoiceStatus;
        this.invoicePaymentStatus = invoicePaymentStatus;
    }

    public WebhookAdditionalFilter(EventType eventType, String shopId) {
        this.eventType = eventType;
        this.shopId = shopId;
    }

    public WebhookAdditionalFilter(EventType eventType) {
        this.eventType = eventType;
    }

    public WebhookAdditionalFilter() {

    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getInvoicePaymentStatus() {
        return invoicePaymentStatus;
    }

    public void setInvoicePaymentStatus(String invoicePaymentStatus) {
        this.invoicePaymentStatus = invoicePaymentStatus;
    }

}
