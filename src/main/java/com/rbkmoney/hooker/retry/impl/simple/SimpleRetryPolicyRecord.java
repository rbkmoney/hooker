package com.rbkmoney.hooker.retry.impl.simple;

import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.retry.RetryPolicyType;

/**
 * Created by jeckep on 18.04.17.
 */
public class SimpleRetryPolicyRecord extends RetryPolicyRecord {
    public static RetryPolicyType type = RetryPolicyType.SIMPLE;

    long queueId;
    int failCount;
    long lastFailTime;

    public SimpleRetryPolicyRecord(long queueId, int failCount, long lastFailTime) {
        this.queueId = queueId;
        this.failCount = failCount;
        this.lastFailTime = lastFailTime;
    }

    public SimpleRetryPolicyRecord() {
    }

    public static void setType(RetryPolicyType type) {
        SimpleRetryPolicyRecord.type = type;
    }

    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public long getLastFailTime() {
        return lastFailTime;
    }

    public void setLastFailTime(long lastFailTime) {
        this.lastFailTime = lastFailTime;
    }

    @Override
    public boolean isFailed() {
        return failCount > 0;
    }

    @Override
    public void reset() {
        failCount = 0;
    }

    @Override
    public RetryPolicyType getType() {
        return type;
    }
}
