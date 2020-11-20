package com.rbkmoney.hooker.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.impl.CustomerQueueDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.retry.RetryPoliciesService;
import com.rbkmoney.hooker.scheduler.MessageScheduler;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.*;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;


@Configuration
public class AppConfig {

    @Value("${message.scheduler.invoicing.threadPoolSize}")
    private int invoicingThreadPoolSize;

    @Value("${message.scheduler.customer.threadPoolSize}")
    private int customerThreadPoolSize;

    @Value("${message.scheduler.delay}")
    private int delayMillis;

    @Value("${merchant.callback.timeout}")
    private int timeout;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public MessageSender<InvoicingMessage, InvoicingQueue> invoicngMessageSender(Signer signer,
                                                                 InvoicingEventService eventService,
                                                                 ObjectMapper objectMapper) {
        return new MessageSender<>(invoicingThreadPoolSize, timeout, signer, eventService, objectMapper);
    }

    @Bean
    public MessageSender<CustomerMessage, CustomerQueue> customerMessageSender(Signer signer,
                                                                               CustomerEventService eventService,
                                                                               ObjectMapper objectMapper) {
        return new MessageSender<>(customerThreadPoolSize, timeout, signer, eventService, objectMapper);
    }

    @Bean
    public MessageProcessor<InvoicingMessage, InvoicingQueue> invoicingMessageProcessor(InvoicingTaskDao taskDao,
                                                                                        InvoicingQueueDao queueDao,
                                                                                        InvoicingMessageDao messageDao,
                                                                                        RetryPoliciesService retryPoliciesService,
                                                                                        TransactionTemplate transactionTemplate,
                                                                                        MessageSender<InvoicingMessage, InvoicingQueue> invoicngMessageSender) {
        return new MessageProcessor<>(taskDao, queueDao, messageDao, retryPoliciesService, transactionTemplate, invoicngMessageSender);
    }

    @Bean
    public MessageProcessor<CustomerMessage, CustomerQueue> customerMessageProcessor(CustomerTaskDao taskDao,
                                                                                     CustomerQueueDao queueDao,
                                                                                     CustomerDao messageDao,
                                                                                     RetryPoliciesService retryPoliciesService,
                                                                                     TransactionTemplate transactionTemplate,
                                                                                     MessageSender<CustomerMessage, CustomerQueue> customerMessageSender) {
        return new MessageProcessor<>(taskDao, queueDao, messageDao, retryPoliciesService, transactionTemplate, customerMessageSender);
    }

    @Bean
    public MessageScheduler<InvoicingMessage, InvoicingQueue> invoicingMessageScheduler(MessageProcessor<InvoicingMessage, InvoicingQueue> invoicingMessageProcessor) {
        return new MessageScheduler<>(invoicingThreadPoolSize, delayMillis, invoicingMessageProcessor);
    }

    @Bean
    public MessageScheduler<CustomerMessage, CustomerQueue> cuustomerMessageScheduler(MessageProcessor<CustomerMessage, CustomerQueue> customerMessageProcessor) {
        return new MessageScheduler<>(customerThreadPoolSize, delayMillis, customerMessageProcessor);
    }
}
