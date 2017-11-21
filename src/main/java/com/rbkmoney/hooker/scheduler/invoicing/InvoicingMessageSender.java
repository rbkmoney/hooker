package com.rbkmoney.hooker.scheduler.invoicing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.MessageJson;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.scheduler.MessageScheduler;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;

import java.util.List;

/**
 * Created by jeckep on 18.04.17.
 */
public class InvoicingMessageSender extends MessageSender<InvoicingMessage> {

    public InvoicingMessageSender(Queue queue, List<InvoicingMessage> messages, TaskDao taskDao, MessageScheduler workerTaskScheduler, Signer signer, PostSender postSender) {
        super(queue, messages, taskDao, workerTaskScheduler, signer, postSender);
    }

    @Override
    protected String getMessageJson(InvoicingMessage message) throws JsonProcessingException {
        return MessageJson.buildMessageJson(message);
    }
}
