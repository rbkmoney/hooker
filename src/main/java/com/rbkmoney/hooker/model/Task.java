package com.rbkmoney.hooker.model;

/**
 * Created by jeckep on 17.04.17.
 */
public class Task {

    long messageId;
    long queueId;

    public Task(long messageId, long queueId) {
        this.messageId = messageId;
        this.queueId = queueId;
    }

    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "Task{" +
                "messageId=" + messageId +
                ", queueId=" + queueId +
                '}';
    }
}
