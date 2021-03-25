package com.rbkmoney.hooker.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.CryptoCurrency;
import com.rbkmoney.damsel.domain.DigitalWallet;
import com.rbkmoney.damsel.domain.DigitalWalletProvider;
import com.rbkmoney.damsel.domain.PaymentTerminal;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.TerminalPaymentProvider;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetails;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsBankCard;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsCryptoWallet;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsPaymentTerminal;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PaymentToolUtilsTest {

    @Test
    public void testGetPaymentToolDetailsCryptoWallet() {
        PaymentTool paymentTool = PaymentTool.crypto_currency(CryptoCurrency.bitcoin);
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertTrue(paymentToolDetails instanceof PaymentToolDetailsCryptoWallet);
        assertNull(paymentToolDetails.getDetailsType());
        assertEquals(com.rbkmoney.swag_webhook_events.model.CryptoCurrency.BITCOIN.getValue(),
                ((PaymentToolDetailsCryptoWallet) paymentToolDetails).getCryptoCurrency().getValue());
    }

    @Test
    public void testDigitalWalletJson() throws JsonProcessingException {
        PaymentTool paymentTool = PaymentTool.digital_wallet(new DigitalWallet(DigitalWalletProvider.qiwi, "kke"));
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        String json = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .writeValueAsString(paymentToolDetails);
        assertEquals(
                "{\"detailsType\":\"PaymentToolDetailsDigitalWallet\",\"digitalWalletDetails\":" +
                        "{\"digitalWalletDetailsType\":\"DigitalWalletDetailsQIWI\",\"phoneNumberMask\":\"kke\"}}",
                json);
    }

    @Test
    public void testGetPaymentToolDetailsPaymentTerminal() {
        PaymentTool paymentTool = PaymentTool.payment_terminal(new PaymentTerminal()
                .setTerminalType(TerminalPaymentProvider.alipay)
        );
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertTrue(paymentToolDetails instanceof PaymentToolDetailsPaymentTerminal);
        assertNull(paymentToolDetails.getDetailsType());
        assertEquals(PaymentToolDetailsPaymentTerminal.ProviderEnum.ALIPAY.getValue(),
                ((PaymentToolDetailsPaymentTerminal) paymentToolDetails).getProvider().getValue());

        paymentTool = PaymentTool.payment_terminal(new PaymentTerminal()
                .setTerminalType(TerminalPaymentProvider.wechat)
        );
        paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertTrue(paymentToolDetails instanceof PaymentToolDetailsPaymentTerminal);
        assertNull(paymentToolDetails.getDetailsType());
        assertEquals(PaymentToolDetailsPaymentTerminal.ProviderEnum.WECHAT.getValue(),
                ((PaymentToolDetailsPaymentTerminal) paymentToolDetails).getProvider().getValue());
    }

    @Test
    public void testGetPaymentToolDetailsBankCard() throws IOException {
        PaymentTool paymentTool = PaymentTool.bank_card(new MockTBaseProcessor(MockMode.RANDOM, 15, 2)
                .process(new BankCard(), new TBaseHandler<>(BankCard.class)));
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertTrue(paymentToolDetails instanceof PaymentToolDetailsBankCard);
        assertNull(paymentToolDetails.getDetailsType());
        assertEquals(paymentTool.getBankCard().getBin(), ((PaymentToolDetailsBankCard) paymentToolDetails).getBin());
    }

}
