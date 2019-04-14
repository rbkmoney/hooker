package com.rbkmoney.hooker.listener;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.converter.SourceEventParser;
import com.rbkmoney.hooker.service.HandlerManager;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MachineEventListener implements MessageListener {

    private final HandlerManager handlerManager;
    private final SourceEventParser eventParser;

    @KafkaListener(topics = "${kafka.invoice.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MachineEvent message, Acknowledgment ack) {
        handle(message, ack);
        ack.acknowledge();
    }

    @Override
    public void handle(MachineEvent machineEvent, Acknowledgment ack) {
        EventPayload payload = eventParser.parseEvent(machineEvent);
        log.info("EventPayload payload: {}", payload);
        if (payload.isSetInvoiceChanges()) {
            for (InvoiceChange invoiceChange : payload.getInvoiceChanges()) {
                try {
                    handlerManager.getHandler(invoiceChange)
                            .ifPresent(handler -> handler.handle(invoiceChange, machineEvent));
                } catch (Exception ex) {
                    log.error("Failed to handle invoice change, invoiceChange='{}'", invoiceChange, ex);
                    throw ex;
                }
            }
        } else if (payload.isSetCustomerChanges()) {
            for (CustomerChange customerChange : payload.getCustomerChanges()) {
                try {
                    handlerManager.getHandler(customerChange)
                            .ifPresent(handler -> handler.handle(customerChange, machineEvent));
                } catch (Exception ex) {
                    log.error("Failed to handle customer change, customerChange='{}'", customerChange, ex);
                    throw ex;
                }
            }
        }
    }
}
