package com.rbkmoney.hooker.listener;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.hooker.handler.poller.customer.AbstractCustomerEventHandler;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerMachineEventHandler implements MachineEventHandler {

    private final MachineEventParser<EventPayload> parser;
    private final List<AbstractCustomerEventHandler> pollingEventHandlers;

    @Override
    @Transactional
    public void handle(List<MachineEvent> machineEvents, Acknowledgment ack) {
        for (MachineEvent machineEvent : machineEvents) {
            EventPayload payload = parser.parse(machineEvent);
            if (!payload.isSetCustomerChanges()) {
                return;
            }

            List<CustomerChange> changes = payload.getCustomerChanges();
            for (int i = 0; i < changes.size(); ++i) {
                preparePollingHandlers(changes.get(i), machineEvent, i);
            }
        }
        ack.acknowledge();
    }

    private void preparePollingHandlers(CustomerChange cc, MachineEvent machineEvent, int i) {
        pollingEventHandlers.stream()
                .filter(handler -> handler.accept(cc))
                .findFirst()
                .ifPresent(handler -> processEvent(handler, cc, machineEvent, i));
    }

    private void processEvent(Handler pollingEventHandler, Object cc, MachineEvent machineEvent, int i) {
        long id = machineEvent.getEventId();
        try {
            log.info("We got an event {}", new TBaseProcessor()
                    .process(machineEvent, JsonHandler.newPrettyJsonInstance()));
            EventInfo eventInfo = new EventInfo(
                    machineEvent.getEventId(),
                    machineEvent.getCreatedAt(),
                    machineEvent.getSourceId(),
                    machineEvent.getEventId(),
                    i
            );
            pollingEventHandler.handle(cc, eventInfo);
        } catch (Exception e) {
            log.error("Error when poller handling with id {}", id, e);
        }
    }

}
