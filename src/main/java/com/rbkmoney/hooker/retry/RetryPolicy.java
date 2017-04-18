package com.rbkmoney.hooker.retry;

/**
 * Created by jeckep on 17.04.17.
 */
public interface RetryPolicy<T> {
    void onFail(T record);
    //returns false if we should wait timeout to send message to this hook
    boolean isActive(T record);
    RetryPolicyType getType();
}
