package com.rbkmoney.hooker.scheduler.invoicing;

import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingQueue;
import com.rbkmoney.hooker.scheduler.MessageScheduler;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

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
    protected MessageSender getMessageSender(MessageSender.QueueStatus queueStatus, List<InvoicingMessage> messagesForQueue, TaskDao taskDao, Signer signer, PostSender postSender) {
        return new InvoicingMessageSender(queueStatus, messagesForQueue, taskDao, signer, postSender);
    }
}
