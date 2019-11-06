package com.rbkmoney.hooker.retry;

import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicy;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by jeckep on 18.04.17.
 */

@Service
@RequiredArgsConstructor
public class RetryPoliciesService {

    private final SimpleRetryPolicy simpleRetryPolicy;
    private final SimpleRetryPolicyDao simpleRetryPolicyDao;

    public RetryPolicy getRetryPolicyByType(RetryPolicyType type) {
        if (RetryPolicyType.SIMPLE.equals(type)) {
            return simpleRetryPolicy;
        } else {
            throw new UnsupportedOperationException("Retry policy for type: " + type.toString() + " not found");
        }
    }

    public void update(RetryPolicyRecord record) {
        if (RetryPolicyType.SIMPLE.equals(record.getType())) {
            simpleRetryPolicyDao.update((SimpleRetryPolicyRecord) record);
        } else {
            throw new UnsupportedOperationException("Retry policy DAO for type: " + record.getType().toString() + " not found");
        }
    }
}
