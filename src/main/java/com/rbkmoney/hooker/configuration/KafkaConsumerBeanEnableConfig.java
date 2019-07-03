package com.rbkmoney.hooker.configuration;

import com.rbkmoney.hooker.listener.KafkaMachineEventListener;
import com.rbkmoney.hooker.listener.MachineEventHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
//@EnableKafka
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.invoice.enabled", havingValue = "true")
    public KafkaMachineEventListener paymentEventsKafkaListener(MachineEventHandler machineEventHandler) {
        return new KafkaMachineEventListener(machineEventHandler);
    }
}
