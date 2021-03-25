package com.rbkmoney.hooker.kafka;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.hooker.AbstractKafkaIntegrationTest;
import com.rbkmoney.hooker.listener.CustomerEventKafkaListener;
import com.rbkmoney.hooker.listener.CustomerMachineEventHandler;
import com.rbkmoney.hooker.listener.InvoicingEventKafkaListener;
import com.rbkmoney.hooker.listener.InvoicingMachineEventHandler;
import com.rbkmoney.hooker.service.HandlerManager;
import com.rbkmoney.kafka.common.serialization.ThriftSerializer;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;

@Slf4j
@ContextConfiguration(classes = {
        KafkaAutoConfiguration.class,
        InvoicingEventKafkaListener.class,
        InvoicingMachineEventHandler.class,
        CustomerEventKafkaListener.class,
        CustomerMachineEventHandler.class
})
public class KafkaMachineEventListenerKafkaTest extends AbstractKafkaIntegrationTest {

    @org.springframework.beans.factory.annotation.Value("${kafka.topics.invoice.id}")
    private String invoiceTopic;

    @org.springframework.beans.factory.annotation.Value("${kafka.topics.customer.id}")
    private String customerTopic;

    @MockBean
    private HandlerManager handlerManager;

    @MockBean
    private MachineEventParser<EventPayload> eventParser;

    @Test
    public void listenInvoiceEmptyChanges() {
        Mockito.when(eventParser.parse(any())).thenReturn(EventPayload.invoice_changes(emptyList()));

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(createMessage());

        writeToTopic(sinkEvent, invoiceTopic);

        Mockito.verify(eventParser, Mockito.timeout(10000L).times(1)).parse(any());
    }

    @Test
    public void listenCustomerEmptyChanges() {
        Mockito.when(eventParser.parse(any())).thenReturn(EventPayload.customer_changes(emptyList()));

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(createMessage());

        writeToTopic(sinkEvent, customerTopic);

        Mockito.verify(eventParser, Mockito.timeout(10000L).times(1)).parse(any());
    }

    private void writeToTopic(SinkEvent sinkEvent, String topicName) {
        Producer<String, SinkEvent> producer = createProducer();
        ProducerRecord<String, SinkEvent> producerRecord = new ProducerRecord<>(topicName, null, sinkEvent);
        try {
            producer.send(producerRecord).get();
        } catch (Exception e) {
            log.error("KafkaAbstractTest initialize e: ", e);
        }
        producer.close();
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

    public static Producer<String, SinkEvent> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client_id");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new ThriftSerializer<SinkEvent>().getClass());
        return new KafkaProducer<>(props);
    }
}
