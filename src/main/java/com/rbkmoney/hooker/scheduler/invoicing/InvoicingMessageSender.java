package com.rbkmoney.hooker.scheduler.invoicing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageJson;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;

import java.util.List;

public class InvoicingMessageSender extends MessageSender<InvoicingMessage> {

    public InvoicingMessageSender(MessageSender.QueueStatus queueStatus, List<InvoicingMessage> messages, Signer signer, PostSender postSender) {
        super(queueStatus, messages, signer, postSender);
    }

    @Override
    protected String getMessageJson(InvoicingMessage message) throws JsonProcessingException {
        return InvoicingMessageJson.buildMessageJson(message);
    }
}
