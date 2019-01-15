package com.rbkmoney.hooker.configuration;

import com.rbkmoney.eventstock.client.*;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.hooker.handler.poller.EventStockHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class EventStockPollerConfig {

    @Value("${bm.pooling.url}")
    private Resource bmUri;

    @Value("${bm.pooling.delay}")
    private int pollDelay;

    @Value("${bm.pooling.maxPoolSize}")
    private int maxPoolSize;

    @Value("${bm.pooling.maxQuerySize}")
    private int maxQuerySize;

    @Value("${bm.pooling.workersCount}")
    private int workersCount;

    @Autowired
    private List<Handler> pollingEventHandlers;

    @Bean
    public List<EventStockHandler> eventStockHandlers() {
        List<EventStockHandler> eventStockHandlers = new ArrayList<>();
        for (int i = 0; i < workersCount; ++i) {
            eventStockHandlers.add(new EventStockHandler(pollingEventHandlers, workersCount, i));
        }
        return eventStockHandlers;
    }

    @Bean
    public List<EventPublisher> eventPublishers(List<EventStockHandler> eventStockHandlers) throws IOException {
        List<EventPublisher> eventPublishers = new ArrayList<>();
        for (int i = 0; i < workersCount; ++i) {
            eventPublishers.add(new PollingEventPublisherBuilder()
                .withURI(bmUri.getURI())
                .withEventHandler(eventStockHandlers.get(i))
                .withMaxPoolSize(maxPoolSize)
                .withPollDelay(pollDelay)
                .withMaxQuerySize(maxQuerySize)
                .build());
        }
        return eventPublishers;
    }
}
