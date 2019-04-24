package com.rbkmoney.hooker.kafka;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.hooker.configuration.RetryConfig;
import com.rbkmoney.hooker.converter.SourceEventParser;
import com.rbkmoney.hooker.listener.KafkaMachineEventListener;
import com.rbkmoney.hooker.listener.MachineEventHandlerImpl;
import com.rbkmoney.hooker.service.HandlerManager;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;

@Slf4j
@TestPropertySource(properties = "kafka.ssl.enable=false")
@ContextConfiguration(classes = {KafkaAutoConfiguration.class, KafkaMachineEventListener.class, MachineEventHandlerImpl.class, RetryConfig.class})
public class KafkaMachineEventListenerKafkaTest extends KafkaAbstractTest {

    @MockBean
    HandlerManager handlerManager;

    @MockBean
    SourceEventParser eventParser;

    @Test
    public void listenEmptyChanges() throws InterruptedException {
        Mockito.when(eventParser.parseEvent(any())).thenReturn(EventPayload.invoice_changes(emptyList()));

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(createMessage());

        writeToTopic(sinkEvent);

        waitForTopicSync();

        Mockito.verify(eventParser, Mockito.times(1)).parseEvent(any());
    }

    private void writeToTopic(SinkEvent sinkEvent) {
        Producer<String, SinkEvent> producer = createProducer();
        ProducerRecord<String, SinkEvent> producerRecord = new ProducerRecord<>(topic, null, sinkEvent);
        try {
            producer.send(producerRecord).get();
        } catch (Exception e) {
            log.error("KafkaAbstractTest initialize e: ", e);
        }
        producer.close();
    }

    private void waitForTopicSync() throws InterruptedException {
        Thread.sleep(1000L);
    }


    private MachineEvent createMessage() {
        MachineEvent message = new MachineEvent();
        Value data = new Value();
        data.setBin(new byte[0]);
        message.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        message.setEventId(1L);
        message.setSourceNs(SOURCE_NS);
        message.setSourceId(SOURCE_ID);
        message.setData(data);
        return message;
    }

}
