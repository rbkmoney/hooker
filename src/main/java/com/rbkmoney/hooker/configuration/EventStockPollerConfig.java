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

    @Autowired
    private List<Handler> pollingEventHandlers;

    @Bean(destroyMethod = "destroy")
    public EventPublisher eventPublisherMod0(EventStockHandler eventStockHandlerMod0) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(bmUri.getURI())
                .withEventHandler(eventStockHandlerMod0)
                .withMaxPoolSize(maxPoolSize)
                .withPollDelay(pollDelay)
                .withMaxQuerySize(maxQuerySize)
                .build();
    }

    @Bean(destroyMethod = "destroy")
    public EventPublisher eventPublisherMod1(EventStockHandler eventStockHandlerMod1) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(bmUri.getURI())
                .withEventHandler(eventStockHandlerMod1)
                .withMaxPoolSize(maxPoolSize)
                .withPollDelay(pollDelay)
                .withMaxQuerySize(maxQuerySize)
                .build();
    }

    @Bean
    public EventStockHandler eventStockHandlerMod0(){
        return new EventStockHandler(pollingEventHandlers, 0);
    }

    @Bean
    public EventStockHandler eventStockHandlerMod1(){
        return new EventStockHandler(pollingEventHandlers, 1);
    }

}
