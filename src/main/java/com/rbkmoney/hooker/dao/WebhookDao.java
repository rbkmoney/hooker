package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.Hook;

import java.util.Collection;
import java.util.List;

/**
 * Created by inal on 28.11.2016.
 */
public interface WebhookDao {
    List<Hook> getPartyWebhooks(String partyId);
    Hook getWebhookById(long id);
    Hook create(Hook hook);
    boolean delete(long id);
    void disable(long id);
    List<Hook> getWithPolicies(Collection<Long> ids);
}
