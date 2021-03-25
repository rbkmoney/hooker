package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.CashFlowAccount;
import com.rbkmoney.damsel.domain.FinalCashFlowAccount;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.domain.MerchantCashFlowAccount;
import com.rbkmoney.damsel.domain.SystemCashFlowAccount;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CashFlowUtilsTest {
    @Test
    public void testFeeAmount() {
        List<FinalCashFlowPosting> finalCashFlowPosting = buildFinalCashFlowPostingList();
        Long feeAmount = CashFlowUtils.getFeeAmount(finalCashFlowPosting);
        Assert.assertEquals(feeAmount.longValue(), 20L);
    }

    private List<FinalCashFlowPosting> buildFinalCashFlowPostingList() {
        FinalCashFlowPosting firstFinalCashFlowPosting = new FinalCashFlowPosting();
        Cash cash = new Cash();
        cash.setAmount(10);
        firstFinalCashFlowPosting.setVolume(cash);
        firstFinalCashFlowPosting.setSource(new FinalCashFlowAccount()
                .setAccountType(CashFlowAccount.merchant(MerchantCashFlowAccount.settlement)));
        firstFinalCashFlowPosting.setDestination(
                new FinalCashFlowAccount().setAccountType(CashFlowAccount.system(SystemCashFlowAccount.settlement)));
        FinalCashFlowPosting secondFinalCashFlowPosting = firstFinalCashFlowPosting.deepCopy();
        return List.of(firstFinalCashFlowPosting, secondFinalCashFlowPosting);
    }
}
