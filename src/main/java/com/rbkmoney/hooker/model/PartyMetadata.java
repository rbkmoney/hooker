package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyMetadata {
    private HooksLimit hooksLimit;

    public Integer getShopLimit(String shopId, int defaultLimit) {
        if (hooksLimit != null) {
            if (hooksLimit.getPerShop() != null) {
                return hooksLimit.getPerShop().getOrDefault(shopId, defaultLimit);
            }
        }
        return defaultLimit;
    }

    public Integer getPartyLimit(int defaultLimit) {
        if (hooksLimit != null) {
            if (hooksLimit.getPerParty() != null) {
                return hooksLimit.getPerParty();
            }
        }
        return defaultLimit;
    }
}
