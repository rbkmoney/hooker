package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.payment_processing.CustomerBinding;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.*;

public class CustomerBindingConverterTest extends AbstractIntegrationTest {

    @Autowired
    private CustomerBindingConverter converter;

    @Test
    public void testConvert() throws IOException {
        CustomerBinding source = new MockTBaseProcessor(MockMode.RANDOM, 15, 1)
                .process(new CustomerBinding(), new TBaseHandler<>(CustomerBinding.class));
        com.rbkmoney.swag_webhook_events.model.CustomerBinding target = converter.convert(source);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getPaymentResource().getPaymentSessionId(), target.getPaymentResource().getPaymentSession());
        assertEquals(source.getStatus().getSetField().getFieldName(), target.getStatus().getValue());
    }
}