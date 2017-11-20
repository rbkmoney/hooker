package com.rbkmoney.hooker.scheduler.customer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerMessageJson;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.scheduler.MessageScheduler;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;

import java.util.List;

/**
 * Created by jeckep on 18.04.17.
 */
public class CustomerMessageSender extends MessageSender<CustomerMessage> {

    public CustomerMessageSender(Queue queue, List<CustomerMessage> messages, TaskDao taskDao, MessageScheduler workerTaskScheduler, Signer signer, PostSender postSender) {
        super(queue, messages, taskDao, workerTaskScheduler, signer, postSender);
    }

    @Override
    protected String getMessageJson(CustomerMessage message) throws JsonProcessingException {
        return CustomerMessageJson.buildMessageJson(message);
    }
}
