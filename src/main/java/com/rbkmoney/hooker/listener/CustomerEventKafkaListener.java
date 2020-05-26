package com.rbkmoney.hooker.listener;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class CustomerEventKafkaListener {

    private final MachineEventHandler customerMachineEventHandler;

    @KafkaListener(topics = "${kafka.topics.customer.id}",
            containerFactory = "customerListenerContainerFactory")
    public void listen(SinkEvent sinkEvent, Acknowledgment ack) {
        MachineEvent machineEvent = sinkEvent.getEvent();
        log.info("Got machineEvent from customer topic (source id={}, event id={})",
                machineEvent.getSourceId(), machineEvent.getEventId());
        customerMachineEventHandler.handle(Arrays.asList(machineEvent), ack);
        log.info("Machine event from customer topic has been committed (source id={}, event id={})",
                machineEvent.getSourceId(), machineEvent.getEventId());
    }
}
