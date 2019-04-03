package com.rbkmoney.hooker.retry;

import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicy;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Queue> filter(Collection<? extends Queue> queues) {
        return queues.stream().
                filter(q -> getRetryPolicyByType(q.getHook().getRetryPolicyType()).isActive(q.getRetryPolicyRecord()))
                .collect(Collectors.toList());
    }

    public void update(RetryPolicyRecord record) {
        if (RetryPolicyType.SIMPLE.equals(record.getType())) {
            simpleRetryPolicyDao.update((SimpleRetryPolicyRecord) record);
        } else {
            throw new UnsupportedOperationException("Retry policy DAO for type: " + record.getType().toString() + " not found");
        }
    }
}
