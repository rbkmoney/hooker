package com.rbkmoney.hooker.scheduler.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.scheduler.MessageSender;
import com.rbkmoney.hooker.service.EventService;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;

import java.util.List;

public class CustomerMessageSender extends MessageSender<CustomerMessage> {

    public CustomerMessageSender(MessageSender.QueueStatus queueStatus, List<CustomerMessage> messages, Signer signer,
                                 PostSender postSender, EventService eventService, ObjectMapper objectMapper) {
        super(queueStatus, messages, signer, postSender, eventService, objectMapper);
    }
}
