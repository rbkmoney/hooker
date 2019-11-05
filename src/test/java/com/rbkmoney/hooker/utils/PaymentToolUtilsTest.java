package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetails;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsCryptoWallet;
import org.junit.Assert;
import org.junit.Test;

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
    public void testFeeAmount() {
        List<FinalCashFlowPosting> finalCashFlowPosting = buildFinalCashFlowPostingList();
        Long feeAmount = PaymentToolUtils.getFeeAmount(finalCashFlowPosting);
        Assert.assertEquals(feeAmount.longValue(), 20L);
    }

    private List<FinalCashFlowPosting> buildFinalCashFlowPostingList() {
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
