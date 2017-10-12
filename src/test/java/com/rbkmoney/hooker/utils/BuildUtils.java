package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.model.Invoice;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.swag_webhook_events.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeckep on 25.04.17.
 */
public class BuildUtils {
    public static Message message(String type, String invoiceId, String partyId, EventType eventType, String status) {
        return message(type, invoiceId, partyId, eventType, status, null, true);
    }

    public static Message message(String type, String invoiceId, String partyId, EventType eventType, String status, List<InvoiceCartPosition> cart, boolean isPayer) {
        Message message = new Message();
        message.setTopic(com.rbkmoney.swag_webhook_events.Event.TopicEnum.INVOICESTOPIC.getValue());
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
            if (isPayer) {
                payment.setPayer(new PaymentResourcePayer()
                        .paymentToolToken("payment tool token")
                        .paymentSession("payment session")
                        .contactInfo(new ContactInfo()
                                .email("aaaa@mail.ru")
                                .phoneNumber("89037279269"))
                        .clientInfo(new ClientInfo()
                                .ip("127.0.0.1")
                                .fingerprint("fingerbox"))
                        .paymentToolDetails(new PaymentToolDetailsBankCard()
                                .cardNumberMask("1234")
                                .paymentSystem("visa")
                                .detailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSBANKCARD))
                        .payerType(Payer.PayerTypeEnum.PAYMENTRESOURCEPAYER));
            } else { //if customer
                payment.setPayer(new CustomerPayer().customerID("12345").payerType(Payer.PayerTypeEnum.CUSTOMERPAYER));
            }
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
