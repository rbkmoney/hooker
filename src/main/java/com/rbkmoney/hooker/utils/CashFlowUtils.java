package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.domain.CashFlowAccount;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.domain.MerchantCashFlowAccount;
import com.rbkmoney.damsel.domain.ProviderCashFlowAccount;
import com.rbkmoney.hooker.model.FeeType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static Map<FeeType, Long> getFees(List<FinalCashFlowPosting> cashFlowPostings) {
        if (cashFlowPostings != null && !cashFlowPostings.isEmpty()) {
            return cashFlowPostings.stream()
                    .collect(
                            Collectors.groupingBy(
                                    CashFlowUtils::getFeeType,
                                    Collectors.summingLong(posting -> posting.getVolume().getAmount())
                            )
                    );
        } else {
            return Map.of();
        }
    }

    public static Map<FeeType, String> getCurrency(List<FinalCashFlowPosting> cashFlowPostings) {
        if (cashFlowPostings != null && !cashFlowPostings.isEmpty()) {
            return cashFlowPostings.stream()
                    .collect(
                            Collectors.groupingBy(
                                    CashFlowUtils::getFeeType,
                                    Collectors.mapping(
                                            o -> o.getVolume().getCurrency().getSymbolicCode(),
                                            Collectors.collectingAndThen(
                                                    Collectors.toList(),
                                                    values -> values.isEmpty() ? null : values.get(0)
                                            )
                                    )
                            )
                    );
        } else {
            return Map.of();
        }
    }

    public static FeeType getFeeType(FinalCashFlowPosting cashFlowPosting) {
        CashFlowAccount source = cashFlowPosting.getSource().getAccountType();
        CashFlowAccount destination = cashFlowPosting.getDestination().getAccountType();

        if (source.isSetProvider() && source.getProvider() == ProviderCashFlowAccount.settlement
                && destination.isSetMerchant() && destination.getMerchant() == MerchantCashFlowAccount.settlement) {
            return FeeType.AMOUNT;
        }

        if (source.isSetMerchant()
                && source.getMerchant() == MerchantCashFlowAccount.settlement
                && destination.isSetSystem()) {
            return FeeType.FEE;
        }

        if (source.isSetSystem()
                && destination.isSetExternal()) {
            return FeeType.EXTERNAL_FEE;
        }

        if (source.isSetSystem()
                && destination.isSetProvider()) {
            return FeeType.PROVIDER_FEE;
        }

        return FeeType.UNKNOWN;
    }
}
