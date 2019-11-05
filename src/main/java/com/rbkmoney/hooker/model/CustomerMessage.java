package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by inalarsanukaev on 13.10.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomerMessage extends Message {
    private CustomerMessageEnum type;
    private String customerId;
    private String bindingId;

    public boolean isBinding() {
        return type == CustomerMessageEnum.BINDING;
    }
}
