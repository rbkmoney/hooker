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
        String paramsAsString = null;
        Object eventForPost = getEventForPost(event);
        try {
            paramsAsString = new ObjectMapper().writeValueAsString(eventForPost);
        } catch (JsonProcessingException e) {
            log.error("Couldn't get JSON from context", e);
        }
        String partyId = null;
        try {
            partyId = getPartyId(eventForPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Webhook> webhookList = webhookDao.getWebhooksByCode(getCode(), partyId);
        if (webhookList != null) {
            log.info("Start AbstractEventHandler: event_id {}", eventId);
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
        log.info("End AbstractEventHandler: event_id {}", eventId);
    }

    protected abstract EventTypeCode getCode();

    protected abstract String getPartyId(Object eventForPost) throws Exception;

    protected abstract Object getEventForPost(Event event) throws Exception;
}
