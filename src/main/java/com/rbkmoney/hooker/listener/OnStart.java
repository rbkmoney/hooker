package com.rbkmoney.hooker.listener;

import com.rbkmoney.eventstock.client.*;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.hooker.handler.poller.EventStockHandler;
import com.rbkmoney.hooker.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OnStart implements ApplicationListener<ApplicationReadyEvent> {

    private final List<EventPublisher> eventPublishers;
    private final List<EventStockHandler> eventStockHandlers;
    private final EventService eventService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (int i = 0; i < eventPublishers.size(); ++i) {
            eventPublishers.get(i).subscribe(buildSubscriberConfig(eventStockHandlers.get(i)));
        }
    }

    public SubscriberConfig buildSubscriberConfig(EventStockHandler eventStockHandler) {
        return new DefaultSubscriberConfig(eventFilter(eventStockHandler));
    }

    public EventFilter eventFilter(EventStockHandler eventStockHandler) {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        Long lastEventId = eventService.getLastEventId(eventStockHandler.getDivider(), eventStockHandler.getMod());
        if (lastEventId != null) {
            eventIDRange.setFromExclusive(lastEventId);
        } else {
            eventIDRange.setFromNow();
        }
        return new EventFlowFilter(new EventConstraint(eventIDRange));
    }

}
