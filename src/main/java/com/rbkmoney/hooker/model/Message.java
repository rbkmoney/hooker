package com.rbkmoney.hooker.model;

import com.rbkmoney.damsel.base.Content;
import org.springframework.beans.BeanUtils;

/**
 * Created by inalarsanukaev on 07.04.17.
 */

public class Message {
    private long id;
    private long eventId;
    private String eventTime;
    private String type;
    private String invoiceId;
    private String paymentId;
    private String partyId;
    private int shopId;
    private long amount;
    private String currency;
    private String createdAt;
    private Content metadata;
    private String status;
    private String product;
    private String description;
    private EventType eventType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public long getEventId() {
        return eventId;
    }
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return id == message.id;
    }


    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", eventType=" + eventType +
                '}';
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public Message copy(){
        Message message = new Message();
        BeanUtils.copyProperties(this, message);
        return message;
    }
}
