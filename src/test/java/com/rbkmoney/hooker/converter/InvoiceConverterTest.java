package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.Allocation;
import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InvoiceConverterTest extends AbstractIntegrationTest {

    @Autowired
    private InvoiceConverter converter;

    @Test
    public void testConverter() throws IOException {
        MockTBaseProcessor mockTBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        Invoice source = mockTBaseProcessor
                .process(new Invoice(), new TBaseHandler<>(Invoice.class));
        source.setCreatedAt("2016-03-22T06:12:27Z");
        source.setDue("2016-03-22T06:12:27Z");
        source.setAllocation(mockTBaseProcessor.process(new Allocation(), new TBaseHandler<>(Allocation.class)));
        com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction =
                mockTBaseProcessor.process(new com.rbkmoney.damsel.domain.AllocationTransaction(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransaction.class));
        source.getAllocation().setTransactions(List.of(allocationTransaction));

        com.rbkmoney.swag_webhook_events.model.Invoice target = converter.convert(source);

        assertEquals(source.getId(), target.getId());
        assertEquals(source.getShopId(), target.getShopID());
        assertEquals(source.getCost().getAmount(), target.getAmount().longValue());
        assertEquals(source.getStatus().getSetField().getFieldName(), target.getStatus().getValue());
        if (source.getDetails().isSetCart()) {
            assertEquals(source.getDetails().getCart().getLines().size(), target.getCart().size());
        }
        com.rbkmoney.swag_webhook_events.model.Allocation allocation = target.getAllocation();
        assertNotNull(allocation);
        assertEquals(allocation.size(), 1);
    }
}
