package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.service.crypt.KeyPair;

import java.util.List;

/**
 * Created by inal on 28.11.2016.
 */
public interface WebhookDao {
    List<Webhook> getPartyWebhooks(String partyId);
    Webhook getWebhookById(long id);
    List<Webhook> getWebhooksForInvoices(EventTypeCode typeCode, String partyId, Integer shopId);
    List<Webhook> getWebhooksForInvoiceStatusChanged(EventTypeCode typeCode, String partyId, Integer shopId, String status);
    List<Webhook> getWebhooksForInvoicePaymentStatusChanged(EventTypeCode typeCode, String partyId, Integer shopId, String status);
    Webhook addWebhook(WebhookParams webhookParams);
    boolean delete(long id);
    KeyPair getPairKey(String partyId);
    KeyPair createPairKey(String partyId);
}
