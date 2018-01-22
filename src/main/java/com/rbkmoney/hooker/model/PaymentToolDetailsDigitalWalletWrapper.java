package com.rbkmoney.hooker.model;

import com.rbkmoney.swag_webhook_events.DigitalWalletDetails;
import com.rbkmoney.swag_webhook_events.PaymentToolDetails;

public class PaymentToolDetailsDigitalWalletWrapper extends PaymentToolDetails {
    private DigitalWalletDetails digitalWalletDetails;

    public DigitalWalletDetails getDigitalWalletDetails() {
        return digitalWalletDetails;
    }

    public void setDigitalWalletDetails(DigitalWalletDetails digitalWalletDetails) {
        this.digitalWalletDetails = digitalWalletDetails;
    }

    public PaymentToolDetailsDigitalWalletWrapper digitalWalletDetails(DigitalWalletDetails digitalWalletDetails) {
        setDigitalWalletDetails(digitalWalletDetails);
        return this;
    }
}
