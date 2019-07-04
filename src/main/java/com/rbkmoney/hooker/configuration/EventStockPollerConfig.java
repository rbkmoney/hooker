package com.rbkmoney.hooker.configuration;

import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.hooker.handler.poller.CustomerEventStockHandler;
import com.rbkmoney.hooker.handler.poller.impl.customer.AbstractCustomerEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class EventStockPollerConfig {

    @Value("${bm.pooling.url}")
    private Resource bmUri;

    @Value("${bm.pooling.delay}")
    private int pollDelay;

    @Value("${bm.pooling.maxPoolSize}")
    private int maxPoolSize;

    @Value("${bm.pooling.maxQuerySize}")
    private int maxQuerySize;

    private final List<AbstractCustomerEventHandler> pollingEventHandlers;

    @Bean
    public CustomerEventStockHandler eventStockHandler() {
        return new CustomerEventStockHandler(pollingEventHandlers);
    }

    @Bean
    public EventPublisher eventPublisher(CustomerEventStockHandler customerEventStockHandler) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(bmUri.getURI())
                .withEventHandler(customerEventStockHandler)
                .withMaxPoolSize(maxPoolSize)
                .withPollDelay(pollDelay)
                .withMaxQuerySize(maxQuerySize)
                .build();
    }
}
