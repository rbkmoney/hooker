package com.rbkmoney.hooker.configuration;

import com.rbkmoney.hooker.listener.CustomerEventKafkaListener;
import com.rbkmoney.hooker.listener.InvoicingEventKafkaListener;
import com.rbkmoney.hooker.listener.MachineEventHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.invoice.enabled", havingValue = "true")
    public InvoicingEventKafkaListener paymentEventsKafkaListener(MachineEventHandler invoicingMachineEventHandler) {
        return new InvoicingEventKafkaListener(invoicingMachineEventHandler);
    }

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.customer.enabled", havingValue = "true")
    public CustomerEventKafkaListener customerEventsKafkaListener(MachineEventHandler customerMachineEventHandler) {
        return new CustomerEventKafkaListener(customerMachineEventHandler);
    }
}
