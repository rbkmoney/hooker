package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by inalarsanukaev on 16.05.17.
 */
@JsonPropertyOrder({"code", "message"})
public class StatusError {
    private String code;
    private String message;

    public StatusError(StatusError other) {
        this.code = other.code;
        this.message = other.message;
    }

    public StatusError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public StatusError() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
