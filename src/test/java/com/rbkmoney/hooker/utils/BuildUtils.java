package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeckep on 25.04.17.
 */
public class BuildUtils {
    public static Message message(String type, String invoiceId, String partyId, EventType eventType, String status) {
        return message(type, invoiceId, partyId, eventType, status, null);
    }

    public static Message message(String type, String invoiceId, String partyId, EventType eventType, String status, List<InvoiceCartPosition> cart) {
        Message message = new Message();
        message.setEventId(5555);
        message.setEventTime("time");
        message.setType(type);
        message.setPartyId(partyId);
        message.setEventType(eventType);
        Invoice invoice = new Invoice();
        message.setInvoice(invoice);
        invoice.setId(invoiceId);
        invoice.setShopID("123");
        invoice.setCreatedAt("12.12.2008");
        if (message.isInvoice()) {
            invoice.setStatus(status);
        } else {
            invoice.setStatus("unpaid");
        }
        invoice.setDueDate("12.12.2008");
        invoice.setAmount(12235);
        invoice.setCurrency("RUB");
        InvoiceContent metadata = new InvoiceContent();
        metadata.setType("fff");
        metadata.setData("{\"cms\":\"drupal\",\"cms_version\":\"7.50\",\"module\":\"uc_rbkmoney\",\"order_id\":\"118\"}".getBytes());
        invoice.setMetadata(metadata);
        invoice.setProduct("product");
        invoice.setDescription("description");
        invoice.setCart(cart);
        if (message.isPayment()) {
            Payment payment = new Payment();
            message.setPayment(payment);
            payment.setId("123");
            payment.setCreatedAt("13.12.20017");
            payment.setStatus(status);
            payment.setError(new PaymentStatusError("1", "shit"));
            payment.setAmount(1);
            payment.setCurrency("RUB");
            payment.setPaymentToolToken("payment tool token");
            payment.setPaymentSession("payment session");
            payment.setContactInfo(new PaymentContactInfo("aaaa@mail.ru", "89037279269"));
            payment.setIp("127.0.0.1");
            payment.setFingerprint("fingerbox");
        }
        return message;
    }

    public static ArrayList<InvoiceCartPosition> cart() {
        ArrayList<InvoiceCartPosition> cart = new ArrayList<>();
        cart.add(new InvoiceCartPosition("Зверушка",123L, 5, 5 * 123L, new TaxMode("18%")));
        cart.add(new InvoiceCartPosition("Квакушка", 456L,6, 6 * 456L, null));
        return cart;
    }
}
