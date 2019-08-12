package com.rbkmoney.hooker.listener;


import com.rbkmoney.kafka.common.util.LogUtil;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class KafkaMachineEventListener {

    private final MachineEventHandler machineEventHandler;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, SinkEvent>> messages, Acknowledgment ack) {
        log.info("Got machineEvent batch with size: {}", messages.size());
        machineEventHandler.handle(messages.stream().map(m -> m.value().getEvent()).collect(Collectors.toList()), ack);
        log.info("Batch has been committed, size={}, {}", messages.size(), LogUtil.toSummaryStringWithSinkEventValues(messages));
    }
}
