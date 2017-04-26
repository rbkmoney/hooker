package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Created by jeckep on 13.04.17.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hook {
    private long id;
    private String partyId;
    private Set<WebhookAdditionalFilter> filters;
    private String url;
    private String pubKey;
    private String privKey;
    private boolean enabled;
    private RetryPolicyType retryPolicyType;
    private RetryPolicyRecord retryPolicyRecord;

    @Override
    public String toString() {
        return "Hook{" +
                "id=" + id +
                ", partyId='" + partyId + '\'' +
                ", url='" + url + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
