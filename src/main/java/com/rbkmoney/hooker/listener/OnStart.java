package com.rbkmoney.hooker.listener;

import com.rbkmoney.eventstock.client.*;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.hooker.handler.poller.CustomerEventStockHandler;
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
    private final List<CustomerEventStockHandler> customerEventStockHandlers;
    private final EventService eventService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (int i = 0; i < eventPublishers.size(); ++i) {
            eventPublishers.get(i).subscribe(buildSubscriberConfig(customerEventStockHandlers.get(i)));
        }
    }

    public SubscriberConfig buildSubscriberConfig(CustomerEventStockHandler customerEventStockHandler) {
        return new DefaultSubscriberConfig(eventFilter(customerEventStockHandler));
    }

    public EventFilter eventFilter(CustomerEventStockHandler customerEventStockHandler) {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        Long lastEventId = eventService.getLastEventId();
        if (lastEventId != null) {
            eventIDRange.setFromExclusive(lastEventId);
        } else {
            eventIDRange.setFromNow();
        }
        return new EventFlowFilter(new EventConstraint(eventIDRange));
    }

}
