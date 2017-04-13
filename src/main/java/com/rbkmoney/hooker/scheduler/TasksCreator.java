package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.hooker.dao.EventDao;
import com.rbkmoney.hooker.dao.WebhookDao;
import com.rbkmoney.hooker.model.EventStatus;
import com.rbkmoney.hooker.model.InvoiceFatEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by jeckep on 12.04.17.
 */

@Service
public class TasksCreator {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EventDao eventDao;

    @Autowired
    WebhookDao webhookDao;

    @Scheduled(fixedDelayString = "${tasks.creator.delay}")
    public void start(){
        final List<InvoiceFatEvent> events = eventDao.getByStatus(EventStatus.RECEIVED);
        final Set<String> eventTypeCodes = getEventTypeCodes(events);
        final Set<String> partyIds = getPartyIds(events);
        final List<Webhook> participatedHooks = webhookDao.getWebhooksBy(eventTypeCodes, partyIds);


        //TODO create tasks, grooup by hooks, start execute tasks
    }

    private Set<String> getEventTypeCodes(List<InvoiceFatEvent> events){
        return events.stream().map(e -> e.getCode()).collect(Collectors.toSet());
    }

    private Set<String> getPartyIds(List<InvoiceFatEvent> events){
        return events.stream().map(e -> e.getPartyId()).collect(Collectors.toSet());
    }
}
