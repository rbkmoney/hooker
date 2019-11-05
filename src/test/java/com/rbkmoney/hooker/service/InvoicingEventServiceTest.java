package com.rbkmoney.hooker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.swag_webhook_events.model.Event;
import com.rbkmoney.swag_webhook_events.model.RefundSucceeded;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.nio.ByteBuffer;
import java.util.Collections;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

public class InvoicingEventServiceTest extends AbstractIntegrationTest {

    @MockBean
    private InvoicingSrv.Iface invoicingClient;

    @Autowired
    private InvoicingEventService service;

    @Autowired
    private ObjectMapper objectMapper;


    @Before
    public void setUp() throws Exception {
        MockTBaseProcessor tBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        Mockito.when(invoicingClient.get(any(), any(), any()))
                .thenReturn(new Invoice()
                        .setInvoice(tBaseProcessor.process(new com.rbkmoney.damsel.domain.Invoice(),
                                new TBaseHandler<>(com.rbkmoney.damsel.domain.Invoice.class))
                                .setCreatedAt("2016-03-22T06:12:27Z")
                                .setContext(new Content("lel", ByteBuffer.wrap("{\"payment_id\": 271771960}".getBytes())))
                                .setDue("2016-03-22T06:12:27Z"))
                        .setPayments(Collections.singletonList(new InvoicePayment()
                                .setPayment(tBaseProcessor.process(new com.rbkmoney.damsel.domain.InvoicePayment(),
                                        new TBaseHandler<>(com.rbkmoney.damsel.domain.InvoicePayment.class))
                                        .setCreatedAt("2016-03-22T06:12:27Z")
                                        .setId("1"))
                                .setRefunds(Collections.singletonList(tBaseProcessor.process(new com.rbkmoney.damsel.domain.InvoicePaymentRefund(),
                                        new TBaseHandler<>(com.rbkmoney.damsel.domain.InvoicePaymentRefund.class))
                                        .setReason("keksik")
                                        .setCreatedAt("2016-03-22T06:12:27Z")
                                        .setId("1"))))));

    }

    @Test
    public void getByMessage() {
        InvoicingMessage message = random(InvoicingMessage.class);
        message.setPaymentId("1");
        message.setRefundId("1");
        message.setType(InvoicingMessageEnum.REFUND);
        message.setEventTime("2016-03-22T06:12:27Z");
        message.setEventType(EventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED);
        message.setRefundStatus(RefundStatusEnum.succeeded);
        Event event = service.getByMessage(message);
        assertTrue(event instanceof RefundSucceeded);
        assertEquals("keksik", ((RefundSucceeded) event).getRefund().getReason());
    }

    @Test
    public void testJson() throws JsonProcessingException {
        InvoicingMessage message = random(InvoicingMessage.class);
        message.setPaymentId("1");
        message.setType(InvoicingMessageEnum.PAYMENT);
        message.setEventTime("2016-03-22T06:12:27Z");
        message.setEventType(EventType.INVOICE_PAYMENT_STATUS_CHANGED);
        message.setPaymentStatus(PaymentStatusEnum.captured);
        Event event = service.getByMessage(message);
        String json = objectMapper.writeValueAsString(event);
        System.out.println(json);
        assertTrue(json.contains("\"payment_id\":271771960"));
    }
}