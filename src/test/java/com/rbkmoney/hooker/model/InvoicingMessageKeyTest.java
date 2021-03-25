package com.rbkmoney.hooker.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class InvoicingMessageKeyTest {

    @Test
    public void testEqualHashCode() {
        InvoicingMessageKey key = InvoicingMessageKey.builder()
                .invoiceId("inv")
                .type(InvoicingMessageEnum.INVOICE)
                .build();

        Map<InvoicingMessageKey, InvoicingMessage> map = new HashMap<>();
        map.put(key, new InvoicingMessage());

        InvoicingMessageKey keyCopy = InvoicingMessageKey.builder()
                .invoiceId("inv")
                .type(InvoicingMessageEnum.INVOICE)
                .build();

        assertNotNull(map.get(keyCopy));

    }

}
