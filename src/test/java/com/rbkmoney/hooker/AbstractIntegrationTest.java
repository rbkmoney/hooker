package com.rbkmoney.hooker;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
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
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = HookerApplication.class, initializers = AbstractIntegrationTest.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractIntegrationTest {
    public static final String SOURCE_ID = "source_id";
    public static final String SOURCE_NS = "source_ns";

    private static final String CONFLUENT_PLATFORM_VERSION = "5.0.1";

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(CONFLUENT_PLATFORM_VERSION).withEmbeddedZookeeper();

    @ClassRule
    public static PostgreSQLContainer postgres = (PostgreSQLContainer) new PostgreSQLContainer("postgres:9.6")
                    .withStartupTimeout(Duration.ofMinutes(5));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            log.info("Postgres URL: "+ postgres.getJdbcUrl());
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "flyway.url=" + postgres.getJdbcUrl(),
                    "flyway.user=" + postgres.getUsername(),
                    "flyway.password=" + postgres.getPassword(),
                    "spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
                    "spring.kafka.properties.security.protocol=PLAINTEXT",
                    "spring.kafka.consumer.group-id=TestListener",
                    "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "spring.kafka.consumer.value-deserializer=com.rbkmoney.hooker.serde.SinkEventDeserializer",
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
            ).applyTo(configurableApplicationContext);
            Flyway flyway = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .schemas("hook")
                    .load();
            flyway.migrate();
        }
    }
    @Value("${local.server.port}")
    protected int port;
}
