package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.model.Invoice;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.PaymentContactInfo;
import com.rbkmoney.hooker.model.Refund;
import com.rbkmoney.swag_webhook_events.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeckep on 25.04.17.
 */
public class BuildUtils {
    public static InvoicingMessage buildMessage(String type, String invoiceId, String partyId, EventType eventType, String status) {
        return buildMessage(type, invoiceId, partyId, eventType, status, null, true);
    }

    public static InvoicingMessage buildMessage(String type, String invoiceId, String partyId, EventType eventType, String status, List<InvoiceCartPosition> cart, boolean isPayer) {
        InvoicingMessage message = new InvoicingMessage();
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
        if (message.isPayment() || message.isRefund()) {
            Payment payment = new Payment();
            message.setPayment(payment);
            payment.setId("123");
            payment.setCreatedAt("13.12.20017");
            payment.setStatus(status);
            PaymentError paymentError = new PaymentError();
            paymentError.setCode("code");
            paymentError.setMessage("mess");
            PaymentErrorSubError subError = new PaymentErrorSubError();
            subError.setCode("sub_code");
            paymentError.setSubError(subError);
            payment.setError(paymentError);
            payment.setAmount(1);
            payment.setCurrency("RUB");
            payment.setPaymentToolToken("payment tool token");
            payment.setPaymentSession("payment session");
            payment.setContactInfo(new PaymentContactInfo("aaaa@mail.ru", "89037279209"));
            payment.setIp("127.0.0.1");
            payment.setFingerprint("fingerbox");
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
                                .bin("520034")
                                .lastDigits("1234")
                                .cardNumberMask("520034******1234")
                                .paymentSystem("visa")
                                .detailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSBANKCARD))
                        .payerType(Payer.PayerTypeEnum.PAYMENTRESOURCEPAYER));
            } else { //if customer
                payment.setPayer(new CustomerPayer().customerID("12345").payerType(Payer.PayerTypeEnum.CUSTOMERPAYER));
            }
        }

        if (message.isRefund()) {
            Refund refund = new Refund();
            message.setRefund(refund);
            refund.setId("123");
            refund.setAmount(115L);
            refund.setCurrency("RUB");
            refund.setStatus("status");
            refund.setReason("kek");
        }
        return message;
    }

    public static ArrayList<InvoiceCartPosition> cart() {
        ArrayList<InvoiceCartPosition> cart = new ArrayList<>();
        cart.add(new InvoiceCartPosition("Зверушка",123L, 5, 5 * 123L, new TaxMode("18%")));
        cart.add(new InvoiceCartPosition("Квакушка", 456L,6, 6 * 456L, null));
        return cart;
    }

    public static CustomerMessage buildCustomerMessage(Long eventId, String partyId, EventType eventType, String type, String custId, String shopId, Customer.StatusEnum custStatus){
        CustomerMessage customerMessage = new CustomerMessage();
        customerMessage.setEventId(eventId);
        customerMessage.setPartyId(partyId);
        customerMessage.setOccuredAt("time");
        customerMessage.setEventType(eventType);
        customerMessage.setType(type);
        customerMessage.setCustomer(new Customer()
                .id(custId)
                .shopID(shopId)
                .status(custStatus)
                .contactInfo(new ContactInfo().phoneNumber("1234").email("aaa@mail.ru"))
                .metadata(CustomerUtils.getJsonObject("{\"field1\":\"value1\",\"field2\":[123,123,123]}")));

        if (customerMessage.isBinding()) {
            customerMessage.setCustomerBinding(new CustomerBinding()
                    .id("12456")
                    .status(CustomerBinding.StatusEnum.PENDING)
            .paymentResource(new PaymentResource()
            .paymentToolToken("shjfbergiwengriweno")
            .paymentSession("wrgnjwierngweirngi")
            .clientInfo(new ClientInfo().ip("127.0.0.1").fingerprint("finger"))
            .paymentToolDetails(new PaymentToolDetailsBankCard()
                    .bin("440088")
                    .lastDigits("1234")
                    .cardNumberMask("440088******1234")
                    .paymentSystem("visa")
                    .tokenProvider(PaymentToolDetailsBankCard.TokenProviderEnum.APPLEPAY)
                    .detailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSBANKCARD)
            )));
        }
        return customerMessage;
    }
}
