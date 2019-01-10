package com.rbkmoney.hooker.listener;

import com.rbkmoney.eventstock.client.*;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.hooker.handler.poller.EventStockHandler;
import com.rbkmoney.hooker.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OnStart implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private EventPublisher eventPublisherMod0;

    @Autowired
    private EventPublisher eventPublisherMod1;

    @Autowired
    private EventStockHandler eventStockHandlerMod0;

    @Autowired
    private EventStockHandler eventStockHandlerMod1;

    @Autowired
    private EventService eventService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        eventPublisherMod0.subscribe(buildSubscriberConfig(eventStockHandlerMod0));
        eventPublisherMod1.subscribe(buildSubscriberConfig(eventStockHandlerMod1));
    }

    public SubscriberConfig buildSubscriberConfig(EventStockHandler eventStockHandler) {
        return new DefaultSubscriberConfig(eventFilter(eventStockHandler));
    }

    public EventFilter eventFilter(EventStockHandler eventStockHandler) {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        Long lastEventId = eventService.getLastEventId(EventStockHandler.DIVIDER, eventStockHandler.getMod());
        if (lastEventId != null) {
            eventIDRange.setFromExclusive(lastEventId);
        } else {
            eventIDRange.setFromNow();
        }
        return new EventFlowFilter(new EventConstraint(eventIDRange));
    }

}
