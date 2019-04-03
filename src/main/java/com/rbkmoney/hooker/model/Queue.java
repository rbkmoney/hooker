package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
@Data
@NoArgsConstructor
public class Queue {
    private long id;
    private Hook hook;
    private RetryPolicyRecord retryPolicyRecord;
}
