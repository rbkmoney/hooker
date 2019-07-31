package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetails;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsBankCard;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsCryptoWallet;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PaymentToolUtilsTest {

    @Test
    public void testGetPaymentToolDetails() {
        PaymentTool paymentTool = PaymentTool.crypto_currency(CryptoCurrency.bitcoin);
        PaymentToolDetails paymentToolDetails = PaymentToolUtils.getPaymentToolDetails(paymentTool);
        assertEquals(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSCRYPTOWALLET, paymentToolDetails.getDetailsType());
        assertEquals(com.rbkmoney.swag_webhook_events.model.CryptoCurrency.BITCOIN.getValue(), ((PaymentToolDetailsCryptoWallet)paymentToolDetails).getCryptoCurrency().getValue());
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
        PaymentToolDetails paymentToolDetails = new PaymentToolDetailsCryptoWallet().cryptoCurrency(com.rbkmoney.swag_webhook_events.model.CryptoCurrency.BITCOIN);
        paymentToolDetails.setDetailsType(PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSCRYPTOWALLET);
        PaymentToolUtils.setPaymentToolDetailsParam(params, paymentToolDetails,
                "detailsTypeParamName", "binParamName", "lastDigitsParamName", "cardNumberMaskParamName", "tokenProviderParamName",
                "paymentSystemParamName", "terminalProviderParamName", "digitalWalletProviderParamName", "digitalWalletIdParamName", "cryptoWalletCurrencyParamName");
        assertEquals("PaymentToolDetailsCryptoWallet", params.getValue("detailsTypeParamName"));
        assertEquals("bitcoin", params.getValue("cryptoWalletCurrencyParamName"));
    }

    @Test
    public void testFeeAmount() throws IOException {
        List<FinalCashFlowPosting> finalCashFlowPosting = buildFinalCashFlowPostingList();
        Long feeAmount = PaymentToolUtils.getFeeAmount(finalCashFlowPosting);
        Assert.assertEquals(feeAmount.longValue(), 20L);
    }

    private List<FinalCashFlowPosting> buildFinalCashFlowPostingList() throws IOException {
        FinalCashFlowPosting firstFinalCashFlowPosting = new FinalCashFlowPosting();
        Cash cash = new Cash();
        cash.setAmount(10);
        firstFinalCashFlowPosting.setVolume(cash);
        firstFinalCashFlowPosting.setSource(new FinalCashFlowAccount().setAccountType(CashFlowAccount.merchant(MerchantCashFlowAccount.settlement)));
        firstFinalCashFlowPosting.setDestination(new FinalCashFlowAccount().setAccountType(CashFlowAccount.system(SystemCashFlowAccount.settlement)));

        FinalCashFlowPosting secondFinalCashFlowPosting = firstFinalCashFlowPosting.deepCopy();

        return List.of(firstFinalCashFlowPosting, secondFinalCashFlowPosting);
    }

}
