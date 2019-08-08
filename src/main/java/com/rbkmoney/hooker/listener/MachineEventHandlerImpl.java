package com.rbkmoney.hooker.listener;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.service.BatchService;
import com.rbkmoney.hooker.service.HandlerManager;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MachineEventHandlerImpl implements MachineEventHandler {

    private final HandlerManager handlerManager;
    private final MachineEventParser<EventPayload> parser;
    private final BatchService batchService;

    @Override
    @Transactional
    public void handle(List<MachineEvent> machineEvents, Acknowledgment ack) {
        Map<InvoicingMessageKey, InvoicingMessage> temporalStorage = new HashMap<>();
        List<InvoicingMessage> messages = new ArrayList<>();
        machineEvents.forEach(me -> {
            EventPayload payload = parser.parse(me);
            if (payload.isSetInvoiceChanges()) {
                for (int i = 0; i < payload.getInvoiceChanges().size(); ++i) {
                    InvoiceChange invoiceChange = payload.getInvoiceChanges().get(i);
                    int j = i;
                    handlerManager.getHandler(invoiceChange).ifPresent(handler -> {
                        log.info("Start to handle event {}", invoiceChange);
                        InvoicingMessage message = handler.handle(invoiceChange,
                                new EventInfo(null, me.getCreatedAt(), me.getSourceId(), me.getEventId(), j),
                                temporalStorage);
                        if (message != null) {
                            messages.add(message);
                        }
                    });
                }
            }
        });
        if (!messages.isEmpty()) {
            batchService.process(messages);
        }
        ack.acknowledge();
    }
}
