package com.rbkmoney.hooker.listener;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.converter.SourceEventParser;
import com.rbkmoney.hooker.exception.ParseException;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.hooker.service.HandlerManager;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;

public class MachineEventHandlerImplTest {

    @Mock
    private HandlerManager handlerManager;
    @Mock
    private Handler handler;
    @Mock
    private SourceEventParser eventParser;
    @Mock
    private Acknowledgment ack;

    private MachineEventHandlerImpl machineEventHandler;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        machineEventHandler = new MachineEventHandlerImpl(handlerManager, eventParser);
    }

    @Test
    public void listenEmptyChanges() {
        Mockito.when(handlerManager.getHandler(any())).thenReturn(java.util.Optional.of(handler));

        MachineEvent message = new MachineEvent();
        Event event = new Event();
        EventPayload payload = new EventPayload();
        payload.setInvoiceChanges(new ArrayList<>());
        event.setPayload(payload);
        Mockito.when(eventParser.parseEvent(message)).thenReturn(payload);

        machineEventHandler.handle(message, ack);

        Mockito.verify(handlerManager, Mockito.times(0)).getHandler(any());
        Mockito.verify(handler, Mockito.times(0)).handle(any(), any(), any(), any(), any(), any());
        Mockito.verify(ack, Mockito.times(1)).acknowledge();
    }

    @Test(expected = ParseException.class)
    public void listenEmptyException() {
        MachineEvent message = new MachineEvent();
        Mockito.when(eventParser.parseEvent(message)).thenThrow(new ParseException());
        machineEventHandler.handle(message, ack);

        Mockito.verify(ack, Mockito.times(0)).acknowledge();
    }

    @Test
    public void listenChanges() {
        MachineEvent message = new MachineEvent();
        Event event = new Event();
        EventPayload payload = new EventPayload();
        ArrayList<InvoiceChange> invoiceChanges = new ArrayList<>();
        invoiceChanges.add(new InvoiceChange());
        payload.setInvoiceChanges(invoiceChanges);
        event.setPayload(payload);
        Mockito.when(eventParser.parseEvent(message)).thenReturn(payload);
        Mockito.when(handlerManager.getHandler(any())).thenReturn(java.util.Optional.of(handler));

        machineEventHandler.handle(message, ack);

        Mockito.verify(handlerManager, Mockito.times(1)).getHandler(any());
        Mockito.verify(handler, Mockito.times(1)).handle(any(), any(), any(), any(), any(), any());
        Mockito.verify(ack, Mockito.times(1)).acknowledge();
    }

}
