package com.rbkmoney.hooker.scheduler.customer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerMessageJson;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;

import java.util.List;

/**
 * Created by jeckep on 18.04.17.
 */
public class CustomerMessageSender extends MessageSender<CustomerMessage> {

    public CustomerMessageSender(MessageSender.QueueStatus queueStatus, List<CustomerMessage> messages, TaskDao taskDao, Signer signer, PostSender postSender) {
        super(queueStatus, messages, taskDao, signer, postSender);
    }

    @Override
    protected String getMessageJson(CustomerMessage message) throws JsonProcessingException {
        return CustomerMessageJson.buildMessageJson(message);
    }
}
