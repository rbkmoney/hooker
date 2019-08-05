package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by inalarsanukaev on 27.09.17.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceCartPosition {
    @JsonIgnore
    private Long messageId;
    private String product;
    private Long price;
    private int quantity;
    private Long cost;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TaxMode taxMode;
}
