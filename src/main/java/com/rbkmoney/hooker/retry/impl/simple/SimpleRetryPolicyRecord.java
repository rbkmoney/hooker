package com.rbkmoney.hooker.retry.impl.simple;

import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by jeckep on 18.04.17.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SimpleRetryPolicyRecord extends RetryPolicyRecord {
    public static RetryPolicyType type = RetryPolicyType.SIMPLE;

    long queueId;
    String messageType;
    int failCount;
    long lastFailTime;

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
