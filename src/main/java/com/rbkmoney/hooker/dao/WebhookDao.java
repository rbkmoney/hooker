package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.service.crypt.KeyPair;

import java.util.Collection;
import java.util.List;

/**
 * Created by inal on 28.11.2016.
 */
public interface WebhookDao {
    List<Hook> getPartyWebhooks(String partyId);
    Hook getWebhookById(long id);
    @Deprecated
    List<Hook> getWebhooksBy(EventType typeCode, String partyId);
    List<Hook> getWebhooksBy(Collection<String> eventTypeCodes, Collection<String> partyIds);
    Hook save(Hook hook);
    boolean delete(long id);
    KeyPair getPairKey(String partyId);
    KeyPair createPairKey(String partyId);
}
