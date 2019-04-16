package com.rbkmoney.hooker.listener;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMachineEventListener {

    private final MachineEventHandler machineEventHandler;

    @KafkaListener(topics = "${kafka.invoice.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MachineEvent message, Acknowledgment ack) {
        log.debug("Got machineEvent: {}", message);
        machineEventHandler.handle(message, ack);
        log.debug("Handled machineEvent {}", message);
    }

}
