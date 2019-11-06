package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.CashFlowAccount;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.domain.MerchantCashFlowAccount;

import java.util.List;

public class CashFlowUtils {

    public static Long getFeeAmount(List<FinalCashFlowPosting> finalCashFlowPostings) {
        return finalCashFlowPostings.stream()
                .filter(CashFlowUtils::isSystemFee)
                .map(finalCashFlowPosting -> finalCashFlowPosting.getVolume().getAmount())
                .reduce(Long::sum)
                .orElse(null);
    }

    private static boolean isSystemFee(FinalCashFlowPosting cashFlowPosting) {
        CashFlowAccount source = cashFlowPosting.getSource().getAccountType();
        CashFlowAccount destination = cashFlowPosting.getDestination().getAccountType();

        return source.isSetMerchant()
                && source.getMerchant() == MerchantCashFlowAccount.settlement
                && destination.isSetSystem();
    }
}
