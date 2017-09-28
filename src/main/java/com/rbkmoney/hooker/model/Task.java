package com.rbkmoney.hooker.model;

/**
 * Created by jeckep on 17.04.17.
 */
public class Task {
    long hookId;
    long messageId;

    public Task(long hookId, long messageId) {
        this.hookId = hookId;
        this.messageId = messageId;
    }

    public long getHookId() {
        return hookId;
    }

    public void setHookId(long hookId) {
        this.hookId = hookId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
