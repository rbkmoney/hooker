package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.retry.RetryPolicyRecord;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class Queue {
    private long id;
    private Hook hook;
    private RetryPolicyRecord retryPolicyRecord;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Hook getHook() {
        return hook;
    }

    public void setHook(Hook hook) {
        this.hook = hook;
    }

    public RetryPolicyRecord getRetryPolicyRecord() {
        return retryPolicyRecord;
    }

    public void setRetryPolicyRecord(RetryPolicyRecord retryPolicyRecord) {
        this.retryPolicyRecord = retryPolicyRecord;
    }

    @Override
    public String toString() {
        return "Queue{" +
                "id=" + id +
                ", hook=" + hook +
                ", retryPolicyRecord=" + retryPolicyRecord +
                '}';
    }
}
