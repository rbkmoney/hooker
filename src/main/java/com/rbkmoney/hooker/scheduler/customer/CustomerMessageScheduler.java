package com.rbkmoney.hooker.scheduler.customer;

import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.impl.CustomerQueueDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerQueue;
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
public class CustomerMessageScheduler extends MessageScheduler<CustomerMessage, CustomerQueue> {

    public CustomerMessageScheduler(
            @Autowired CustomerTaskDao taskDao,
            @Autowired CustomerQueueDao queueDao,
            @Autowired CustomerDao customerDao,
            @Value("${message.sender.number}") int numberOfWorkers) {
        super(taskDao, queueDao, customerDao, numberOfWorkers);
    }

    @Override
    protected MessageSender getMessageSender(MessageSender.QueueStatus queueStatus, List<CustomerMessage> messagesForQueue, Signer signer, PostSender postSender) {
        return new CustomerMessageSender(queueStatus, messagesForQueue, signer, postSender);
    }
}
