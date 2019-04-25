package com.rbkmoney.hooker.kafka;

import com.rbkmoney.hooker.serde.MachineEventSerializer;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;

import java.util.Properties;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = KafkaAbstractTest.Initializer.class)
public abstract class KafkaAbstractTest {

    public static final String SOURCE_ID = "source_id";
    public static final String SOURCE_NS = "source_ns";

    private static final String CONFLUENT_PLATFORM_VERSION = "5.0.1";

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(CONFLUENT_PLATFORM_VERSION).withEmbeddedZookeeper();

    @Value("${kafka.invoice.topic}")
    public String topic;

    public static Producer<String, SinkEvent> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client_id");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MachineEventSerializer.class.getName());
        return new KafkaProducer<>(props);
    }


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues
                    .of("spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
                        "spring.kafka.properties.security.protocol=PLAINTEXT",
                        "spring.kafka.consumer.group-id=TestListener",
                        "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                        "spring.kafka.consumer.value-deserializer=com.rbkmoney.hooker.serde.MachineEventDeserializer",
                        "spring.kafka.consumer.enable-auto-commit=false",
                        "spring.kafka.consumer.auto-offset-reset=earliest",
                        "spring.kafka.consumer.client-id=test",
                        "spring.kafka.listener.type=batch",
                        "spring.kafka.listener.ack-mode=manual",
                        "spring.kafka.listener.concurrency=1",
                        "spring.kafka.listener.poll-timeout=1000",
                        "spring.kafka.listener.no-poll-threshold=5.0",
                        "spring.kafka.listener.log-container-config=true",
                        "spring.kafka.listener.monitor-interval=10s",
                        "spring.kafka.client-id=test",
                        "kafka.invoice.topic=test-topic"
                    );
            values.applyTo(configurableApplicationContext);
        }
    }
}