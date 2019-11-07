package com.rbkmoney.hooker.model;

import org.junit.Test;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.*;

public class InvoicingMessageTest {

    @Test
    public void testCopy() {
        InvoicingMessage invoicingMessage = random(InvoicingMessage.class);
        InvoicingMessage copy = invoicingMessage.copy();
        invoicingMessage.setPaymentFee(123L);
        assertNotEquals(123, copy.getPaymentFee().longValue());
    }
}
