package com.rbkmoney.hooker.model;

import com.rbkmoney.swag_webhook_events.model.DigitalWalletDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentToolDetailsDigitalWallet
        extends com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsDigitalWallet {
    private DigitalWalletDetails digitalWalletDetails;
}
