package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.webhooker.EventFilter;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.model.PartyMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HooksLimitService {
    private final HookDao hookDao;

    @Value("${limit.perShop}")
    private int defaultShopLimit;

    @Value("${limit.perParty}")
    private int defaultPartyLimit;

    public boolean isLimitExceeded(WebhookParams webhookParams) {
        EventFilter eventFilter = webhookParams.getEventFilter();
        if (!eventFilter.isSetInvoice()) {
            return false;
        }
        String partyId = webhookParams.getPartyId();
        PartyMetadata partyMetadata = hookDao.getPartyMetadata(partyId);
        String shopId = eventFilter.getInvoice().getShopId();
        if (shopId != null) {
            return isShopLimitExceeded(partyId, shopId, partyMetadata);
        } else {
            return isPartyLimitExceeded(partyId, partyMetadata);
        }
    }

    private boolean isPartyLimitExceeded(String partyId, PartyMetadata partyMetadata) {
        int limit = partyMetadata == null ? defaultPartyLimit : partyMetadata.getPartyLimit(defaultPartyLimit);
        int hooksCount = hookDao.getPartyHooksCount(partyId);
        return hooksCount >= limit;
    }

    private boolean isShopLimitExceeded(String partyId, String shopId, PartyMetadata partyMetadata) {
        int limit = partyMetadata == null ? defaultShopLimit : partyMetadata.getShopLimit(shopId, defaultShopLimit);
        int hooksCount = hookDao.getShopHooksCount(partyId, shopId);
        return hooksCount >= limit;
    }
}
