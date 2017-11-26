package com.rbkmoney.hooker.retry.impl.simple;

import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.retry.RetryPolicy;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jeckep on 17.04.17.
 */

@Component
public class SimpleRetryPolicy implements RetryPolicy<SimpleRetryPolicyRecord> {

    @Autowired
    SimpleRetryPolicyDao simpleRetryPolicyDao;

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
    public boolean isFail(SimpleRetryPolicyRecord rp) {
        rp.setFailCount(rp.getFailCount() + 1);
        rp.setLastFailTime(System.currentTimeMillis());
        simpleRetryPolicyDao.update(rp);

        return rp.getFailCount() > delays.length;
    }

    @Override
    public boolean isActive(SimpleRetryPolicyRecord rp) {
        if (rp.getFailCount() == 0) {
            return true;
        } else if (rp.getFailCount() <= delays.length
                && System.currentTimeMillis() > (rp.getLastFailTime() + (delays[rp.getFailCount()-1] * 1000))) {
            return true;
        }

        return false;
    }
}
