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
@JsonPropertyOrder({"type", "data"})
public class InvoiceContent {
    public String type;
    public byte[] data;

    public InvoiceContent(InvoiceContent other) {
        this.type = other.type;
        this.data = other.data;
    }
}
