package com.rbkmoney.hooker.handler.poller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.hooker.dao.EventTypeCode;
import com.rbkmoney.hooker.dao.WebhookDao;
import com.rbkmoney.hooker.handler.poller.PollingEventHandler;
import com.rbkmoney.hooker.service.EventService;
import com.rbkmoney.hooker.service.WebhookHttpPostSender;
import com.rbkmoney.hooker.service.crypt.KeyPair;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * Created by inalarsanukaev on 06.04.17.
 */
public abstract class AbstractEventHandler implements PollingEventHandler<StockEvent> {
    Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    EventService eventService;
    @Autowired
    Signer signer;
    @Autowired
    WebhookHttpPostSender webhookHttpPostSender;
    @Autowired
    WebhookDao webhookDao;

    @Override
    public void handle(StockEvent value) throws Exception {
        Event event = value.getSourceEvent().getProcessingEvent();
        long eventId = event.getId();
        String invoiceId = event.getSource().getInvoice();
        String partyId = null;
        try {
            partyId = getPartyId(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String paramsAsString = null;
        try {
            paramsAsString = new ObjectMapper().writeValueAsString(getEventForPost(event));
        } catch (JsonProcessingException e) {
            log.error("Couldn't get JSON from context", e);
        }
        List<Webhook> webhookList = webhookDao.getWebhooksByCode(getCode(), partyId);
        if (webhookList != null) {
            log.info("Start InvoiceCreatedHandler: event_id {}, invoiceId {}", eventId, invoiceId);
            for (Webhook webhook : webhookList) {
                KeyPair keyPair = webhookDao.getPairKey(partyId);
                final String signature = signer.sign(paramsAsString, keyPair.getPrivKey());
                try {
                    webhookHttpPostSender.doPost(webhook.getUrl(), paramsAsString, signature);
                } catch (IOException e) {
                    log.error("Couldn't send post-request", e);
                }
            }
        }
        try {
            eventService.setLastEventId(eventId);
        } catch (Exception e) {
            log.error("Exception: not save Last id. Reason: " + e.getMessage());
        }
        log.info("End InvoiceCreatedHandler: event_id {}, invoiceId {}", eventId, invoiceId);
    }

    protected abstract EventTypeCode getCode();

    protected abstract String getPartyId(Event event) throws Exception;

    protected abstract Object getEventForPost(Event event);
}
