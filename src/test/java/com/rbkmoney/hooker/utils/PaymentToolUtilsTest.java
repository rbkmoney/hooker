package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.CryptoCurrency;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.swag_webhook_events.PaymentToolDetails;
import com.rbkmoney.swag_webhook_events.PaymentToolDetailsBankCard;
import com.rbkmoney.swag_webhook_events.PaymentToolDetailsCryptoWallet;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import static org.junit.Assert.*;

public class PaymentToolUtilsTest {

    @Test
    public void testGetPaymentToolDetails() {
        PaymentTool paymentTool = PaymentTool.crypto_currency(CryptoCurrency.bitcoin);
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertEquals(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSCRYPTOWALLET, paymentToolDetails.getDetailsType());
        assertEquals(com.rbkmoney.swag_webhook_events.CryptoCurrency.BITCOIN.getValue(), ((PaymentToolDetailsCryptoWallet)paymentToolDetails).getCryptoCurrency().getValue());
    }

    @Test
    public void testGetPaymentToolDetails1() {
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails("PaymentToolDetailsBankCard", "4242424242424242", "4242", "424242*******4242", "applepay", "visa", "qiwi", "digitalWalletProvider", "digitalWalletId", "bitcoin");
        assertEquals(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSBANKCARD, paymentToolDetails.getDetailsType());
        assertEquals("visa", ((PaymentToolDetailsBankCard) paymentToolDetails).getPaymentSystem());

        paymentToolDetails = PaymentToolUtils.getPaymentToolDetails("PaymentToolDetailsCryptoWallet", "4242424242424242", "4242", "424242*******4242", "applepay", "visa", "qiwi", "digitalWalletProvider", "digitalWalletId", "bitcoin");
        assertEquals(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSCRYPTOWALLET, paymentToolDetails.getDetailsType());
        assertEquals("bitcoin", ((PaymentToolDetailsCryptoWallet) paymentToolDetails).getCryptoCurrency().getValue());
    }

    @Test
    public void testSetPaymentToolDetailsParam() {
        MapSqlParameterSource params = new MapSqlParameterSource();
        PaymentToolDetails paymentToolDetails = new PaymentToolDetailsCryptoWallet().cryptoCurrency(com.rbkmoney.swag_webhook_events.CryptoCurrency.BITCOIN);
        paymentToolDetails.setDetailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSCRYPTOWALLET);
        PaymentToolUtils.setPaymentToolDetailsParam(params, paymentToolDetails,
                "detailsTypeParamName", "binParamName", "lastDigitsParamName", "cardNumberMaskParamName", "tokenProviderParamName",
                "paymentSystemParamName", "terminalProviderParamName", "digitalWalletProviderParamName", "digitalWalletIdParamName", "cryptoWalletCurrencyParamName");
        assertEquals("PaymentToolDetailsCryptoWallet", params.getValue("detailsTypeParamName"));
        assertEquals("bitcoin", params.getValue("cryptoWalletCurrencyParamName"));
    }
}
