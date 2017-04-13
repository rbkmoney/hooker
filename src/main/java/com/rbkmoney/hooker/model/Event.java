package com.rbkmoney.hooker.model;

/**
 * Created by jeckep on 12.04.17.
 */

public class Event {
    long id;
    String code;
    String status;
    String invoceId;

    public Event(long id, String code, String status, String invoceId) {
        this.id = id;
        this.code = code;
        this.status = status;
        this.invoceId = invoceId;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getInvoceId() {
        return invoceId;
    }
}
