package com.rbkmoney.hooker.scheduler.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.impl.CustomerQueueDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerQueue;
import com.rbkmoney.hooker.scheduler.MessageScheduler;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.CustomerEventService;
import com.rbkmoney.hooker.service.EventService;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerMessageScheduler extends MessageScheduler<CustomerMessage, CustomerQueue> {

    public CustomerMessageScheduler(CustomerTaskDao taskDao, CustomerQueueDao queueDao, CustomerDao customerDao,
                                    CustomerEventService eventService, @Value("${message.sender.number}") int numberOfWorkers) {
        super(taskDao, queueDao, customerDao, eventService, numberOfWorkers);
    }

    @Override
    protected MessageSender getMessageSender(MessageSender.QueueStatus queueStatus, List<CustomerMessage> messagesForQueue,
                                             Signer signer, PostSender postSender, EventService eventService, ObjectMapper objectMapper) {
        return new CustomerMessageSender(queueStatus, messagesForQueue, signer, postSender, eventService, objectMapper);
    }
}
