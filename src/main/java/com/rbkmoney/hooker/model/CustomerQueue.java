package com.rbkmoney.hooker.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
@Getter
@Setter
public class CustomerQueue extends Queue {
    private String customerId;
}
