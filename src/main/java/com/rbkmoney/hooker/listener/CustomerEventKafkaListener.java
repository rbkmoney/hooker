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
public class CustomerEventKafkaListener {

    private final MachineEventHandler customerMachineEventHandler;

    @KafkaListener(topics = "${kafka.topics.customer.id}",
            containerFactory = "customerListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, SinkEvent>> messages, Acknowledgment ack) {
        log.info("Got machineEvent from customer topic. Batch size: {}", messages.size());
        customerMachineEventHandler.handle(messages.stream()
                .map(m -> m.value().getEvent())
                .collect(Collectors.toList()), ack);
        log.info("Batch from customer topic has been committed (size={}, values={})", messages.size(),
                LogUtil.toSummaryStringWithSinkEventValues(messages));
    }
}
