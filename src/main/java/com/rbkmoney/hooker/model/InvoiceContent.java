package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@JsonPropertyOrder({"type", "data"})
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
public class InvoiceContent {
    public String type;
    public byte[] data;

    public InvoiceContent(InvoiceContent other) {
        this.type = other.type;
        this.data = other.data;
    }
}
