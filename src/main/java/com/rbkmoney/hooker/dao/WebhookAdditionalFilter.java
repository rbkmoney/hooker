package com.rbkmoney.hooker.dao;

/**
 * Created by inalarsanukaev on 18.04.17.
 */
public class WebhookAdditionalFilter {
    private EventTypeCode eventTypeCode;
    private Integer invoiceShopId;
    private String invoiceStatus;
    private String invoicePaymentStatus;

    public WebhookAdditionalFilter(EventTypeCode eventTypeCode, Integer invoiceShopId, String invoiceStatus, String invoicePaymentStatus) {
        this.eventTypeCode = eventTypeCode;
        this.invoiceShopId = invoiceShopId;
        this.invoiceStatus = invoiceStatus;
        this.invoicePaymentStatus = invoicePaymentStatus;
    }

    public WebhookAdditionalFilter(EventTypeCode eventTypeCode, Integer invoiceShopId) {
        this.eventTypeCode = eventTypeCode;
        this.invoiceShopId = invoiceShopId;
    }

    public WebhookAdditionalFilter(EventTypeCode eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }

    public WebhookAdditionalFilter() {

    }

    public EventTypeCode getEventTypeCode() {
        return eventTypeCode;
    }

    public void setEventTypeCode(EventTypeCode eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }

    public Integer getInvoiceShopId() {
        return invoiceShopId;
    }

    public void setInvoiceShopId(Integer invoiceShopId) {
        this.invoiceShopId = invoiceShopId;
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
