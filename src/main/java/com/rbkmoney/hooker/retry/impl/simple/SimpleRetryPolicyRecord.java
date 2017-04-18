package com.rbkmoney.hooker.retry.impl.simple;

import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jeckep on 18.04.17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleRetryPolicyRecord extends RetryPolicyRecord{
    long hookId;
    int failCount;
    long lastFailTime;
}
