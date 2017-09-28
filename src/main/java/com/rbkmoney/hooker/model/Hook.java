package com.rbkmoney.hooker.model;

import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.retry.RetryPolicyType;

import java.util.Set;

/**
 * Created by jeckep on 13.04.17.
 */

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

    public Hook(long id, String partyId, Set<WebhookAdditionalFilter> filters, String url, String pubKey, String privKey, boolean enabled, RetryPolicyType retryPolicyType, RetryPolicyRecord retryPolicyRecord) {
        this.id = id;
        this.partyId = partyId;
        this.filters = filters;
        this.url = url;
        this.pubKey = pubKey;
        this.privKey = privKey;
        this.enabled = enabled;
        this.retryPolicyType = retryPolicyType;
        this.retryPolicyRecord = retryPolicyRecord;
    }

    public Hook() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public Set<WebhookAdditionalFilter> getFilters() {
        return filters;
    }

    public void setFilters(Set<WebhookAdditionalFilter> filters) {
        this.filters = filters;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPrivKey() {
        return privKey;
    }

    public void setPrivKey(String privKey) {
        this.privKey = privKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RetryPolicyType getRetryPolicyType() {
        return retryPolicyType;
    }

    public void setRetryPolicyType(RetryPolicyType retryPolicyType) {
        this.retryPolicyType = retryPolicyType;
    }

    public RetryPolicyRecord getRetryPolicyRecord() {
        return retryPolicyRecord;
    }

    public void setRetryPolicyRecord(RetryPolicyRecord retryPolicyRecord) {
        this.retryPolicyRecord = retryPolicyRecord;
    }

    @Override
    public String toString() {
        return "Hook{" +
                "id=" + id +
                ", partyId='" + partyId + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
