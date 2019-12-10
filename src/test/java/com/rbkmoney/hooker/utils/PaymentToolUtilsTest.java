package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetails;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsBankCard;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsCryptoWallet;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsPaymentTerminal;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class PaymentToolUtilsTest {

    @Test
    public void testGetPaymentToolDetailsCryptoWallet() {
        PaymentTool paymentTool = PaymentTool.crypto_currency(CryptoCurrency.bitcoin);
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertTrue(paymentToolDetails instanceof PaymentToolDetailsCryptoWallet);
        assertNull(paymentToolDetails.getDetailsType());
        assertEquals(com.rbkmoney.swag_webhook_events.model.CryptoCurrency.BITCOIN.getValue(), ((PaymentToolDetailsCryptoWallet)paymentToolDetails).getCryptoCurrency().getValue());
    }

    @Test
    public void testGetPaymentToolDetailsPaymentTerminal() {
        PaymentTool paymentTool = PaymentTool.payment_terminal(new PaymentTerminal()
                .setTerminalType(TerminalPaymentProvider.alipay)
        );
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertTrue(paymentToolDetails instanceof PaymentToolDetailsPaymentTerminal);
        assertNull(paymentToolDetails.getDetailsType());
        assertEquals(PaymentToolDetailsPaymentTerminal.ProviderEnum.ALIPAY.getValue(), ((PaymentToolDetailsPaymentTerminal) paymentToolDetails).getProvider().getValue());

        paymentTool = PaymentTool.payment_terminal(new PaymentTerminal()
                .setTerminalType(TerminalPaymentProvider.wechat)
        );
        paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertTrue(paymentToolDetails instanceof PaymentToolDetailsPaymentTerminal);
        assertNull(paymentToolDetails.getDetailsType());
        assertEquals(PaymentToolDetailsPaymentTerminal.ProviderEnum.WECHAT.getValue(), ((PaymentToolDetailsPaymentTerminal) paymentToolDetails).getProvider().getValue());
    }

    @Test
    public void testGetPaymentToolDetailsBankCard() throws IOException {
        PaymentTool paymentTool = PaymentTool.bank_card(new MockTBaseProcessor(MockMode.RANDOM, 15, 2).process(new BankCard(), new TBaseHandler<>(BankCard.class)));
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertTrue(paymentToolDetails instanceof PaymentToolDetailsBankCard);
        assertNull(paymentToolDetails.getDetailsType());
        assertEquals(paymentTool.getBankCard().getBin(), ((PaymentToolDetailsBankCard)paymentToolDetails).getBin());
    }

}
