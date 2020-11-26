package com.rbkmoney.hooker.retry.impl.simple;

import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SimpleRetryPolicyRecord extends RetryPolicyRecord {
    public static RetryPolicyType type = RetryPolicyType.SIMPLE;

    private Long queueId;
    private String messageType;
    private Integer failCount;
    private Long lastFailTime;
    private Long nextFireTime;

    @Override
    public boolean isFailed() {
        return failCount > 0;
    }

    @Override
    public void reset() {
        failCount = 0;
        lastFailTime = null;
        nextFireTime = null;
    }

    @Override
    public RetryPolicyType getType() {
        return type;
    }
}
