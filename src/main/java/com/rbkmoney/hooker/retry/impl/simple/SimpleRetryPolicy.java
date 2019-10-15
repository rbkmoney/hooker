package com.rbkmoney.hooker.retry.impl.simple;

import com.rbkmoney.hooker.retry.RetryPolicy;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Created by jeckep on 17.04.17.
 */

@Component
@RequiredArgsConstructor
public class SimpleRetryPolicy implements RetryPolicy<SimpleRetryPolicyRecord> {

    private long[] delays = {30, 300, 900, 3600,
            3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600,
            3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600,
            3600, 3600, 3600, 3600
    }; //in seconds

    @Override
    public RetryPolicyType getType() {
        return RetryPolicyType.SIMPLE;
    }

    @Override
    public void updateFailed(SimpleRetryPolicyRecord record) {
        record.setFailCount(record.getFailCount() + 1);
        record.setLastFailTime(System.currentTimeMillis());
        if (record.getFailCount() <= delays.length) {
            record.setNextFireTime(record.getLastFailTime() + (delays[record.getFailCount() - 1] * 1000));
        }
    }

    @Override
    public boolean shouldDisable(SimpleRetryPolicyRecord rp) {
        return rp.getFailCount() > delays.length;
    }
}
