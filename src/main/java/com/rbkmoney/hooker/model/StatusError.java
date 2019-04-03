package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by inalarsanukaev on 16.05.17.
 */
@JsonPropertyOrder({"code", "message"})
@AllArgsConstructor
@Getter
@Setter
public class StatusError {
    private String code;
    private String message;

    public StatusError(StatusError other) {
        this.code = other.code;
        this.message = other.message;
    }
}
