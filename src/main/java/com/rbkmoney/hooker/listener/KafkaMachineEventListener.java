package com.rbkmoney.hooker.listener;

import com.rbkmoney.machinegun.eventsink.SinkEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class KafkaMachineEventListener {

    private final MachineEventHandler machineEventHandler;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, SinkEvent>> messages, Acknowledgment ack) {
        try {
            log.debug("Got machineEventBatch with size: {}", messages.size());
            machineEventHandler.handle(messages.stream().map(m -> m.value().getEvent()).collect(Collectors.toList()));
            log.debug("Handled machineEvent", messages);
        } catch (Exception e) {
            throw new RuntimeException("Error when handling batch = {}");
        }
    }

}
