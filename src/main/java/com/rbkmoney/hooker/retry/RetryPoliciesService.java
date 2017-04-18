package com.rbkmoney.hooker.retry;

import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jeckep on 18.04.17.
 */

@Service
public class RetryPoliciesService {

    @Autowired
    SimpleRetryPolicy simpleRetryPolicy;

    RetryPolicy getRetryPolicyByType(RetryPolicyType type){
        if(RetryPolicyType.SIMPLE.equals(type)){
            return simpleRetryPolicy;
        } else {
            throw new UnsupportedOperationException("Retry policy for type: " + type.toString() + " not found");
        }
    }

    public List<Hook> filter(Collection<Hook> hooks){
        return hooks.stream().
                filter(h -> getRetryPolicyByType(h.getRetryPolicyType()).isActive(h.getRetryPolicyRecord()))
                .collect(Collectors.toList());
    }
}
