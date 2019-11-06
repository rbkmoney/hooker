package com.rbkmoney.hooker.scheduler.invoicing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingQueue;
import com.rbkmoney.hooker.scheduler.MessageScheduler;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.EventService;
import com.rbkmoney.hooker.service.InvoicingEventService;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoicingMessageScheduler extends MessageScheduler<InvoicingMessage, InvoicingQueue> {

    public InvoicingMessageScheduler(InvoicingTaskDao taskDao, InvoicingQueueDao queueDao, InvoicingMessageDao messageDao,
                                     InvoicingEventService eventService, @Value("${message.sender.number}") int numberOfWorkers) {
        super(taskDao, queueDao, messageDao, eventService, numberOfWorkers);
    }

    @Override
    protected MessageSender getMessageSender(MessageSender.QueueStatus queueStatus, List<InvoicingMessage> messagesForQueue,
                                             Signer signer, PostSender postSender, EventService eventService, ObjectMapper objectMapper) {
        return new InvoicingMessageSender(queueStatus, messagesForQueue, signer, postSender, eventService, objectMapper);
    }
}
