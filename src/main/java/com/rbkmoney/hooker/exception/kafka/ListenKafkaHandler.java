package com.rbkmoney.hooker.exception.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.List;

@Slf4j
public class ListenKafkaHandler implements ErrorHandler {

    @Override
    public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {
        log.error("ListenKafkaHandler exception topic: {} offset: {} headers: {}  e: ", data.topic(), data.offset(), data.headers(), thrownException);
    }

    @Override
    public void handle(Exception thrownException, ConsumerRecord<?, ?> data, Consumer<?, ?> consumer) {
        log.error("ListenKafkaHandler exception topic: {} offset: {} headers: {}  e: ", data.topic(), data.offset(), data.headers(), thrownException);
    }

    @Override
    public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
        ConsumerRecord<?, ?> data = records.get(0);
        log.error("ListenKafkaHandler exception topic: {} offset: {} headers: {}  e: ", data.topic(), data.offset(), data.headers(), thrownException);
    }

}