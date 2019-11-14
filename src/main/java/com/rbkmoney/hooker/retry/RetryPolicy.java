package com.rbkmoney.hooker.retry;

/**
 * Created by jeckep on 17.04.17.
 */
public interface RetryPolicy<T> {
    boolean shouldDisable(T record);

    RetryPolicyType getType();

    void updateFailed(T record);
}
