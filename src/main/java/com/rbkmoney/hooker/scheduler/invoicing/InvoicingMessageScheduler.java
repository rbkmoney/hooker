package com.rbkmoney.hooker.scheduler.invoicing;

import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingQueue;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.scheduler.MessageScheduler;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by jeckep on 17.04.17.
 */

@Service
public class InvoicingMessageScheduler extends MessageScheduler<InvoicingMessage, InvoicingQueue> {

    public InvoicingMessageScheduler(
            @Autowired InvoicingTaskDao taskDao,
            @Autowired InvoicingQueueDao queueDao,
            @Autowired InvoicingMessageDao customerDao,
            @Value("${message.sender.number}") int numberOfWorkers) {
        super(taskDao, queueDao, customerDao, numberOfWorkers);
    }

    @Override
    protected MessageSender getMessageSender(Queue queue, List<InvoicingMessage> messagesForQueue, TaskDao taskDao, MessageScheduler messageScheduler, Signer signer, PostSender postSender) {
        return new InvoicingMessageSender(queue, messagesForQueue, taskDao, messageScheduler, signer, postSender);
    }
}
