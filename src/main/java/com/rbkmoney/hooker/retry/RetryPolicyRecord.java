package com.rbkmoney.hooker.retry;

/**
 * Created by jeckep on 18.04.17.
 */
public abstract class RetryPolicyRecord {
     public abstract boolean isFailed();
     public abstract void reset();
     public abstract RetryPolicyType getType();
}
