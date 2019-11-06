package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.swag_webhook_events.model.CustomerPayer;
import com.rbkmoney.swag_webhook_events.model.Payment;
import com.rbkmoney.swag_webhook_events.model.PaymentResourcePayer;
import com.rbkmoney.swag_webhook_events.model.RecurrentPayer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.*;

public class PaymentConverterTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentConverter converter;

    @Test
    public void testConvert() throws IOException {
        InvoicePayment source = new MockTBaseProcessor(MockMode.RANDOM, 15, 1)
                .process(new InvoicePayment(), new TBaseHandler<>(InvoicePayment.class));
        source.setCreatedAt("2016-03-22T06:12:27Z");
        Payment target = converter.convert(source);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getStatus().getSetField().getFieldName(), target.getStatus().getValue());
        assertEquals(source.getCost().getAmount(), target.getAmount().longValue());
        assertEquals(source.getCost().getCurrency().getSymbolicCode(), target.getCurrency());
        if (source.getPayer().isSetCustomer()) {
            assertTrue(target.getPayer() instanceof CustomerPayer);
            assertEquals(source.getPayer().getCustomer().getCustomerId(), ((CustomerPayer)target.getPayer()).getCustomerID());
        } if (source.getPayer().isSetPaymentResource()) {
            assertTrue(target.getPayer() instanceof PaymentResourcePayer);
            assertEquals(source.getPayer().getPaymentResource().getContactInfo().getEmail(), ((PaymentResourcePayer) target.getPayer()).getContactInfo().getEmail());
            assertEquals(source.getPayer().getPaymentResource().getContactInfo().getPhoneNumber(), ((PaymentResourcePayer) target.getPayer()).getContactInfo().getPhoneNumber());
            assertEquals(source.getPayer().getPaymentResource().getResource().getPaymentSessionId(), ((PaymentResourcePayer) target.getPayer()).getPaymentSession());
        } else if (source.getPayer().isSetRecurrent()) {
            assertTrue(target.getPayer() instanceof RecurrentPayer);
            assertEquals(source.getPayer().getRecurrent().getRecurrentParent().getInvoiceId(),
                    ((RecurrentPayer)target.getPayer()).getRecurrentParentPayment().getInvoiceID());
        }
    }
}