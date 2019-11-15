package com.rbkmoney.hooker.model;

import org.junit.Test;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertNotEquals;

public class InvoicingMessageTest {

    @Test
    public void testCopy() {
        InvoicingMessage invoicingMessage = random(InvoicingMessage.class);
        InvoicingMessage copy = invoicingMessage.copy();
        invoicingMessage.setRefundId("asd");
        assertNotEquals("asd", copy.getRefundId());
    }
}
