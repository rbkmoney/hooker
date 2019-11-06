package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.payment_processing.Customer;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.*;

public class CustomerConverterTest extends AbstractIntegrationTest {

    @Autowired
    private CustomerConverter converter;

    @Test
    public void testConvert() throws IOException {
        Customer source = new MockTBaseProcessor(MockMode.RANDOM, 15, 1)
                .process(new Customer(), new TBaseHandler<>(Customer.class));
        com.rbkmoney.swag_webhook_events.model.Customer target = converter.convert(source);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getShopId(), target.getShopID());
        assertEquals(source.getStatus().getSetField().getFieldName(), target.getStatus().getValue());
    }
}