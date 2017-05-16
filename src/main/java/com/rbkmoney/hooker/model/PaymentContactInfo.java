package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by inalarsanukaev on 16.05.17.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonPropertyOrder({"email", "phoneNumber"})
public class PaymentContactInfo implements Serializable {
    private String email;
    private String phoneNumber;
}
