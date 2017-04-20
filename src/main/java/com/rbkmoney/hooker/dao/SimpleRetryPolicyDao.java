package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;

/**
 * Created by jeckep on 17.04.17.
 */
public interface SimpleRetryPolicyDao {
    void update(SimpleRetryPolicyRecord record);
}
