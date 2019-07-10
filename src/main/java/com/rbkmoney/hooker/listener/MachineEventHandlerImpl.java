package com.rbkmoney.hooker.listener;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.service.HandlerManager;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MachineEventHandlerImpl implements MachineEventHandler {

    private final HandlerManager handlerManager;
    private final MachineEventParser<EventPayload> parser;

    @Override
    @Transactional
    public void handle(MachineEvent machineEvent, Acknowledgment ack) {
        EventPayload payload = parser.parse(machineEvent);
        log.info("EventPayload payload: {}", payload);
        if (payload.isSetInvoiceChanges()) {
            for (int i = 0; i < payload.getInvoiceChanges().size(); ++i) {
                InvoiceChange invoiceChange = payload.getInvoiceChanges().get(i);
                try {
                    int j = i;
                    handlerManager.getHandler(invoiceChange)
                            .ifPresentOrElse(handler -> handler.handle(invoiceChange, null, machineEvent.getCreatedAt(), machineEvent.getSourceId(), machineEvent.getEventId(), j),
                                    () -> log.debug("Handler for invoiceChange {} wasn't found (machineEvent {})", invoiceChange, machineEvent));
                } catch (Exception ex) {
                    log.error("Failed to handle invoice change, invoiceChange='{}'", invoiceChange, ex);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("Interrupted while sleeping when handle invoice change, invoiceChange='{}'", invoiceChange);
                        Thread.currentThread().interrupt();
                    }
                    throw ex;
                }
            }
        }
        ack.acknowledge();
        log.debug("Ack for machineEvent {} sent", machineEvent);
    }

}
