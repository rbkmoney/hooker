package com.rbkmoney.hooker.model;

import com.rbkmoney.swag_webhook_events.model.DigitalWalletDetails;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetails;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsDigitalWallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentToolDetailsDigitalWalletWrapper extends PaymentToolDetailsDigitalWallet {
    private DigitalWalletDetails digitalWalletDetails;

    public PaymentToolDetailsDigitalWalletWrapper digitalWalletDetails(DigitalWalletDetails digitalWalletDetails) {
        setDigitalWalletDetails(digitalWalletDetails);
        return this;
    }
}
