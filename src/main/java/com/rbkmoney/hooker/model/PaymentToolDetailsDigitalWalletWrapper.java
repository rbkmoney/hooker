package com.rbkmoney.hooker.model;

import com.rbkmoney.swag_webhook_events.model.DigitalWalletDetails;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetails;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentToolDetailsDigitalWalletWrapper extends PaymentToolDetails {
    private DigitalWalletDetails digitalWalletDetails;

    public PaymentToolDetailsDigitalWalletWrapper digitalWalletDetails(DigitalWalletDetails digitalWalletDetails) {
        setDigitalWalletDetails(digitalWalletDetails);
        return this;
    }
}
