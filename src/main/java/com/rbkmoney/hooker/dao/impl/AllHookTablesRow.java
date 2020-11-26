package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by inalarsanukaev on 18.04.17.
 */
@AllArgsConstructor
@Getter
@Setter
public class AllHookTablesRow {
    private long id;
    private String partyId;
    private String topic;
    private String url;
    private String pubKey;
    private boolean enabled;
    private double availability;
    private WebhookAdditionalFilter webhookAdditionalFilter;

}
