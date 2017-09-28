package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@JsonPropertyOrder({"type", "data"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceContent {
    public String type;
    public byte[] data;

    public InvoiceContent(InvoiceContent other) {
        this.type = other.type;
        this.data = other.data;
    }

    public InvoiceContent() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
