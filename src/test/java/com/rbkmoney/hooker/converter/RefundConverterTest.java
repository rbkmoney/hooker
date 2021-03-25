package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.swag_webhook_events.model.Refund;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static java.util.List.of;
import static org.junit.Assert.assertEquals;

public class RefundConverterTest extends AbstractIntegrationTest {

    @Autowired
    private RefundConverter converter;

    @Test
    public void testConvert() throws IOException {
        InvoicePaymentRefund source = new MockTBaseProcessor(MockMode.RANDOM, 15, 1)
                .process(new InvoicePaymentRefund(), new TBaseHandler<>(InvoicePaymentRefund.class));
        source.setCreatedAt("2016-03-22T06:12:27Z");
        Refund target =
                converter.convert(new com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund(source, of()));
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getStatus().getSetField().getFieldName(), target.getStatus().getValue());
        assertEquals(source.getReason(), target.getReason());
    }
}
