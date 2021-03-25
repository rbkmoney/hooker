package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.PaymentTool;
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

import static java.util.List.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PaymentConverterTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentConverter converter;

    @Test
    public void testConvert() throws IOException {
        MockTBaseProcessor mockTBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        InvoicePayment source = mockTBaseProcessor
                .process(new InvoicePayment(), new TBaseHandler<>(InvoicePayment.class));
        source.setCreatedAt("2016-03-22T06:12:27Z");
        if (source.getPayer().isSetPaymentResource()) {
            source.getPayer().getPaymentResource().getResource()
                    .setPaymentTool(PaymentTool
                            .bank_card(mockTBaseProcessor.process(new BankCard(), new TBaseHandler<>(BankCard.class))));
        }
        Payment target = converter
                .convert(new com.rbkmoney.damsel.payment_processing.InvoicePayment(source, of(), of(), of(), of()));
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getStatus().getSetField().getFieldName(), target.getStatus().getValue());
        if (source.getStatus().isSetCaptured() && source.getStatus().getCaptured().isSetCost()) {
            assertEquals(source.getStatus().getCaptured().getCost().getAmount(), target.getAmount().longValue());
            assertEquals(source.getStatus().getCaptured().getCost().getCurrency().getSymbolicCode(),
                    target.getCurrency());
        } else {
            assertEquals(source.getCost().getAmount(), target.getAmount().longValue());
            assertEquals(source.getCost().getCurrency().getSymbolicCode(), target.getCurrency());
        }
        if (source.getPayer().isSetCustomer()) {
            assertTrue(target.getPayer() instanceof CustomerPayer);
            assertEquals(source.getPayer().getCustomer().getCustomerId(),
                    ((CustomerPayer) target.getPayer()).getCustomerID());
        }
        if (source.getPayer().isSetPaymentResource()) {
            assertTrue(target.getPayer() instanceof PaymentResourcePayer);
            assertEquals(source.getPayer().getPaymentResource().getContactInfo().getEmail(),
                    ((PaymentResourcePayer) target.getPayer()).getContactInfo().getEmail());
            assertEquals(source.getPayer().getPaymentResource().getContactInfo().getPhoneNumber(),
                    ((PaymentResourcePayer) target.getPayer()).getContactInfo().getPhoneNumber());
            assertEquals(source.getPayer().getPaymentResource().getResource().getPaymentSessionId(),
                    ((PaymentResourcePayer) target.getPayer()).getPaymentSession());
        } else if (source.getPayer().isSetRecurrent()) {
            assertTrue(target.getPayer() instanceof RecurrentPayer);
            assertEquals(source.getPayer().getRecurrent().getRecurrentParent().getInvoiceId(),
                    ((RecurrentPayer) target.getPayer()).getRecurrentParentPayment().getInvoiceID());
        }
    }
}
