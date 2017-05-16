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
@JsonPropertyOrder({"type", "data"})
public class InvoiceContent implements Serializable {
    public String type;
    public byte[] data;
}
