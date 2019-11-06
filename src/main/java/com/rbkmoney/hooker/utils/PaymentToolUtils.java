package com.rbkmoney.hooker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.DigitalWalletProvider;
import com.rbkmoney.damsel.domain.MobilePhone;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.hooker.model.PaymentToolDetailsDigitalWalletWrapper;
import com.rbkmoney.swag_webhook_events.model.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Created by inalarsanukaev on 13.10.17.
 */
public class PaymentToolUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static PaymentToolDetails getPaymentToolDetails(PaymentTool paymentTool) {
        if (paymentTool.isSetBankCard()) {
            return new PaymentToolDetailsBankCard()
                    .bin(paymentTool.getBankCard().getBin())
                    .lastDigits(paymentTool.getBankCard().getMaskedPan())
                    .cardNumberMask(paymentTool.getBankCard().getBin() + "******" + paymentTool.getBankCard().getMaskedPan())
                    .tokenProvider(paymentTool.getBankCard().isSetTokenProvider() ?
                            PaymentToolDetailsBankCard.TokenProviderEnum.fromValue(paymentTool.getBankCard().getTokenProvider().name()) : null)
                    .paymentSystem(paymentTool.getBankCard().getPaymentSystem().name());
        } else if (paymentTool.isSetPaymentTerminal()) {
            return new PaymentToolDetailsPaymentTerminal()
                    .provider(PaymentToolDetailsPaymentTerminal.ProviderEnum.fromValue(paymentTool.getPaymentTerminal().getTerminalType().name()));
        } else if (paymentTool.isSetDigitalWallet()) {
            if (paymentTool.getDigitalWallet().getProvider() == DigitalWalletProvider.qiwi) {
                return new PaymentToolDetailsDigitalWalletWrapper(new DigitalWalletDetailsQIWI()
                        .phoneNumberMask(paymentTool.getDigitalWallet().getId()));
            } else {
                throw new UnsupportedOperationException("Unknown digital wallet type");
            }
        } else if (paymentTool.isSetCryptoCurrency()) {
            return new PaymentToolDetailsCryptoWallet()
                    .cryptoCurrency(CryptoCurrency.fromValue(paymentTool.getCryptoCurrency().name()));
        } else if (paymentTool.isSetMobileCommerce()) {
            return new PaymentToolDetailsMobileCommerce()
                    .phoneNumber(paymentTool.getMobileCommerce().getPhone().getCc() + paymentTool.getMobileCommerce().getPhone().getCtn());
        } else {
            throw new UnsupportedOperationException("Unknown payment tool type. Must be bank card, terminal or digital wallet");
        }
    }

    public static String getPaymentToolToken(PaymentTool paymentTool) {
        ObjectNode rootNode = mapper.createObjectNode();
        if (paymentTool.isSetBankCard()) {
            BankCard pCard = paymentTool.getBankCard();
            rootNode.put("type", "bank_card");
            rootNode.put("token", pCard.getToken());
            rootNode.put("payment_system", pCard.getPaymentSystem().toString());
            rootNode.put("bin", pCard.getBin());
            rootNode.put("masked_pan", pCard.getMaskedPan());
            if (pCard.isSetTokenProvider()) {
                rootNode.put("token_provider", pCard.getTokenProvider().name());
            }
        } else if (paymentTool.isSetPaymentTerminal()) {
            rootNode.put("type", "payment_terminal");
            rootNode.put("terminal_type", paymentTool.getPaymentTerminal().getTerminalType().toString());
        } else if (paymentTool.isSetDigitalWallet()) {
            rootNode.put("type", "digital_wallet");
            rootNode.put("provider", paymentTool.getDigitalWallet().getProvider().name());
            rootNode.put("id", paymentTool.getDigitalWallet().getId());
        } else if (paymentTool.isSetCryptoCurrency()) {
            rootNode.put("type", "crypto_currency");
            rootNode.put("crypto_currency", paymentTool.getCryptoCurrency().name());
        } else if (paymentTool.isSetMobileCommerce()) {
            rootNode.put("type", "mobile_commerce");
            MobilePhone mobilePhone = paymentTool.getMobileCommerce().getPhone();
            rootNode.put("mobile_commerce", mobilePhone.getCc() + mobilePhone.getCtn());
        } else {
            throw new UnsupportedOperationException("Unknown payment tool type. Must be bank card, terminal or digital wallet");
        }
        return Base64.getUrlEncoder().encodeToString(rootNode.toString().getBytes(StandardCharsets.UTF_8));
    }
}
