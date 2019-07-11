package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.hooker.handler.poller.impl.customer.AbstractCustomerEventHandler;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import com.rbkmoney.hooker.utils.BuildUtils;
import com.rbkmoney.swag_webhook_events.Customer;
import com.rbkmoney.swag_webhook_events.DigitalWalletDetails;
import com.rbkmoney.swag_webhook_events.DigitalWalletDetailsQIWI;
import com.rbkmoney.swag_webhook_events.PaymentToolDetails;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by inalarsanukaev on 24.04.17.
 */
public class MessageJsonTest {
    @Test
    public void test() throws JsonProcessingException {
        InvoicingMessage message = BuildUtils.buildMessage(AbstractInvoiceEventHandler.PAYMENT, "444", "987", EventType.INVOICE_PAYMENT_STARTED, "cancelled");
        String json = InvoicingMessageJson.buildMessageJson(message);
        System.out.println(json);
        Assert.assertTrue(json.contains("\"kek\":\"lol\""));
    }

    @Test
    public void testCart() throws JsonProcessingException {
        InvoicingMessage message = BuildUtils.buildMessage(AbstractInvoiceEventHandler.PAYMENT, "444", "987", EventType.INVOICE_PAYMENT_STARTED, "cancelled", BuildUtils.cart(), true);
        String messageJson = InvoicingMessageJson.buildMessageJson(message);
        System.out.println(messageJson);
        Assert.assertTrue(messageJson.contains("taxMode"));
    }

    @Test
    public void testInvoiceCustomer() throws JsonProcessingException {
        InvoicingMessage message = BuildUtils.buildMessage(AbstractInvoiceEventHandler.PAYMENT, "444", "987", EventType.INVOICE_PAYMENT_STARTED, "cancelled", BuildUtils.cart(), false);
        String messageJson = InvoicingMessageJson.buildMessageJson(message);
        System.out.println(messageJson);
        Assert.assertTrue(messageJson.contains("CustomerPayer"));
    }

    @Test
    public void testCustomer() throws JsonProcessingException {
        CustomerMessage message = BuildUtils.buildCustomerMessage(1L, "444",  EventType.CUSTOMER_CREATED, AbstractCustomerEventHandler.CUSTOMER, "1234", "2342", Customer.StatusEnum.READY);
        String messageJson = CustomerMessageJson.buildMessageJson(message);
        System.out.println(messageJson);
        Assert.assertTrue(messageJson.contains("CustomerCreated"));
    }

    @Test
    public void testCustomerBinding() throws JsonProcessingException {
        CustomerMessage message = BuildUtils.buildCustomerMessage(1L, "444",  EventType.CUSTOMER_BINDING_SUCCEEDED, AbstractCustomerEventHandler.BINDING, "1234", "2342", Customer.StatusEnum.READY);
        String messageJson = CustomerMessageJson.buildMessageJson(message);
        System.out.println(messageJson);
        Assert.assertTrue(messageJson.contains("CustomerBindingSucceeded"));
    }

    @Test
    public void testDigitalWallet() throws JsonProcessingException {
        CustomerMessage message = BuildUtils.buildCustomerMessage(1L, "444",  EventType.CUSTOMER_BINDING_SUCCEEDED, AbstractCustomerEventHandler.BINDING, "1234", "2342", Customer.StatusEnum.READY);
        message.getCustomerBinding().getPaymentResource().setPaymentToolDetails(new PaymentToolDetailsDigitalWalletWrapper()
                .digitalWalletDetails(new DigitalWalletDetailsQIWI()
                        .phoneNumberMask("134242")
                        .digitalWalletDetailsType(DigitalWalletDetails.DigitalWalletDetailsTypeEnum.DIGITALWALLETDETAILSQIWI))
        .detailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSDIGITALWALLET));

        String messageJson = CustomerMessageJson.buildMessageJson(message);
        System.out.println(messageJson);
        Assert.assertTrue(messageJson.contains("DigitalWalletDetailsQIWI"));
    }

}
