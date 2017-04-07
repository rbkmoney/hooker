package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.Webhook;

/**
 * Created by inalarsanukaev on 06.04.17.
 */
public class WebhookImpl extends Webhook {
    String privKey;

    public String getPrivKey() {
        return privKey;
    }

    public void setPrivKey(String privKey) {
        this.privKey = privKey;
    }
}
