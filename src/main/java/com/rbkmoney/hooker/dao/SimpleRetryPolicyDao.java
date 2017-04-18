package com.rbkmoney.hooker.dao;

/**
 * Created by jeckep on 17.04.17.
 */
public interface SimpleRetryPolicyDao {
    void onFail(long hookId);
}
