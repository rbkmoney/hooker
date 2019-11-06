package com.rbkmoney.hooker.model;

import com.rbkmoney.swag_webhook_events.model.DigitalWalletDetails;
import com.rbkmoney.swag_webhook_events.model.PaymentToolDetailsDigitalWallet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentToolDetailsDigitalWalletWrapper extends PaymentToolDetailsDigitalWallet {
    private DigitalWalletDetails digitalWalletDetails;
}
