package com.rbkmoney.hooker.configuration;

import com.rbkmoney.hooker.serde.MachineEventDeserializer;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.LoggingErrorHandler;
import org.springframework.retry.support.RetryTemplate;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private static final String GROUP_ID = "HookerListener";
    private static final String EARLIEST = "earliest";
    public static final String PKCS_12 = "PKCS12";

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;
    @Value("${kafka.concurrency}")
    private int concurrency;
    @Value("${kafka.ssl.server-password}")
    private String serverStorePassword;
    @Value("${kafka.ssl.server-keystore-location}")
    private String serverStoreCertPath;
    @Value("${kafka.ssl.keystore-password}")
    private String keyStorePassword;
    @Value("${kafka.ssl.key-password}")
    private String keyPassword;
    @Value("${kafka.ssl.keystore-location}")
    private String clientStoreCertPath;
    @Value("${kafka.ssl.enable}")
    private boolean kafkaSslEnable;

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MachineEventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);
        if (kafkaSslEnable) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name());
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, new File(serverStoreCertPath).getAbsolutePath());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, serverStorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, PKCS_12);
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, PKCS_12);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, new File(clientStoreCertPath).getAbsolutePath());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keyStorePassword);
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
        }
        return props;
    }

    @Bean
    public ConsumerFactory<String, MachineEvent> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> kafkaListenerContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory,
            RetryTemplate retryTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, MachineEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckOnError(false);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setErrorHandler(new LoggingErrorHandler());
        factory.setConcurrency(concurrency);
        factory.setRetryTemplate(retryTemplate);
        return factory;
    }

    @PreDestroy
    public void stopKafka() {
        kafkaListenerEndpointRegistry.stop();
    }
}