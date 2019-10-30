package com.rbkmoney.hooker.scheduler.invoicing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.EventService;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;

import java.util.List;

public class InvoicingMessageSender extends MessageSender<InvoicingMessage> {

    public InvoicingMessageSender(MessageSender.QueueStatus queueStatus, List<InvoicingMessage> messages, Signer signer,
                                  PostSender postSender, EventService eventService, ObjectMapper objectMapper) {
        super(queueStatus, messages, signer, postSender, eventService, objectMapper);
    }
}
