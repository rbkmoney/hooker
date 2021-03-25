package com.rbkmoney.hooker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.domain.InvoicePaid;
import com.rbkmoney.damsel.domain.InvoicePaymentPending;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.InvoiceStatus;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageEnum;
import com.rbkmoney.hooker.model.PaymentStatusEnum;
import com.rbkmoney.hooker.model.RefundStatusEnum;
import com.rbkmoney.hooker.utils.BuildUtils;
import com.rbkmoney.swag_webhook_events.model.Event;
import com.rbkmoney.swag_webhook_events.model.RefundSucceeded;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        Mockito.when(invoicingClient.get(any(), any(), any()))
                .thenReturn(BuildUtils.buildInvoice("partyId", "invoiceId", "1", "1",
                        InvoiceStatus.paid(new InvoicePaid()),
                        InvoicePaymentStatus.pending(new InvoicePaymentPending())));
    }

    @Test
    public void testRefundSucceeded() {
        InvoicingMessage message = random(InvoicingMessage.class);
        message.setPaymentId("1");
        message.setRefundId("1");
        message.setType(InvoicingMessageEnum.REFUND);
        message.setEventTime("2016-03-22T06:12:27Z");
        message.setEventType(EventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED);
        message.setRefundStatus(RefundStatusEnum.SUCCEEDED);
        Event event = service.getEventByMessage(message);
        assertTrue(event instanceof RefundSucceeded);
        RefundSucceeded refundSucceded = (RefundSucceeded) event;
        assertEquals(message.getEventId().intValue(), event.getEventID().intValue());
        assertEquals("invoiceId", refundSucceded.getInvoice().getId());
        assertEquals("1", refundSucceded.getPayment().getId());
        assertEquals("1", refundSucceded.getRefund().getId());
        assertEquals("keksik", refundSucceded.getRefund().getReason());
        assertEquals("chicken-teriyaki", refundSucceded.getRefund().getRrn());
    }

    @Test
    public void testJson() throws JsonProcessingException {
        InvoicingMessage message = random(InvoicingMessage.class);
        message.setPaymentId("1");
        message.setType(InvoicingMessageEnum.PAYMENT);
        message.setEventTime("2016-03-22T06:12:27Z");
        message.setEventType(EventType.INVOICE_PAYMENT_STATUS_CHANGED);
        message.setPaymentStatus(PaymentStatusEnum.CAPTURED);
        Event event = service.getEventByMessage(message);
        String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("\"payment_id\":271771960"));
    }
}
