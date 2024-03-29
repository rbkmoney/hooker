package com.rbkmoney.hooker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.LegacyDigitalWalletProvider;
import com.rbkmoney.damsel.domain.MobilePhone;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.hooker.model.PaymentToolDetailsDigitalWallet;
import com.rbkmoney.mamsel.*;
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
                    .lastDigits(paymentTool.getBankCard().getLastDigits())
                    .cardNumberMask(
                            paymentTool.getBankCard().getBin() + "******" + paymentTool.getBankCard().getLastDigits())
                    .tokenProvider(PaymentToolDetailsBankCard.TokenProviderEnum.fromValue(
                            TokenProviderUtil.getTokenProviderName(paymentTool.getBankCard())))
                    .paymentSystem(PaymentSystemUtil.getPaymentSystemName(paymentTool.getBankCard()))
                    .issuerCountry(paymentTool.getBankCard().getIssuerCountry() != null
                            ? paymentTool.getBankCard().getIssuerCountry().name() : null)
                    .bankName(paymentTool.getBankCard().getBankName());
        } else if (paymentTool.isSetPaymentTerminal()) {
            return new PaymentToolDetailsPaymentTerminal()
                    .provider(PaymentToolDetailsPaymentTerminal.ProviderEnum.fromValue(
                            TerminalPaymentUtil.getTerminalPaymentProviderName(paymentTool.getPaymentTerminal())));
        } else if (paymentTool.isSetDigitalWallet()) {
            LegacyDigitalWalletProvider walletProvider = LegacyDigitalWalletProvider.valueOf(
                    DigitalWalletUtil.getDigitalWalletName(paymentTool.getDigitalWallet()));
            if (walletProvider == LegacyDigitalWalletProvider.qiwi) {
                return new PaymentToolDetailsDigitalWallet(new DigitalWalletDetailsQIWI()
                        .phoneNumberMask(paymentTool.getDigitalWallet().getId()));
            } else {
                throw new UnsupportedOperationException("Unknown digital wallet type");
            }
        } else if (CryptoCurrencyUtil.isSetCryptoCurrency(paymentTool)) {
            return new PaymentToolDetailsCryptoWallet()
                    .cryptoCurrency(CryptoCurrency.fromValue(CryptoCurrencyUtil.getCryptoCurrencyName(paymentTool)));
        } else if (paymentTool.isSetMobileCommerce()) {
            return new PaymentToolDetailsMobileCommerce()
                    .phoneNumber(paymentTool.getMobileCommerce().getPhone().getCc() +
                            paymentTool.getMobileCommerce().getPhone().getCtn());
        } else {
            throw new UnsupportedOperationException(
                    "Unknown payment tool type. Must be bank card, terminal or digital wallet");
        }
    }

    public static String getPaymentToolToken(PaymentTool paymentTool) {
        ObjectNode rootNode = mapper.createObjectNode();
        if (paymentTool.isSetBankCard()) {
            BankCard paymentCard = paymentTool.getBankCard();
            rootNode.put("type", "bank_card");
            rootNode.put("token", paymentCard.getToken());
            rootNode.put("payment_system", PaymentSystemUtil.getPaymentSystemName(paymentCard));
            rootNode.put("bin", paymentCard.getBin());
            rootNode.put("masked_pan", paymentCard.getLastDigits());
            rootNode.put("token_provider", TokenProviderUtil.getTokenProviderName(paymentCard));
        } else if (paymentTool.isSetPaymentTerminal()) {
            rootNode.put("type", "payment_terminal");
            rootNode.put(
                    "terminal_type",
                    TerminalPaymentUtil.getTerminalPaymentProviderName(paymentTool.getPaymentTerminal())
            );
        } else if (paymentTool.isSetDigitalWallet()) {
            rootNode.put("type", "digital_wallet");
            rootNode.put("provider", DigitalWalletUtil.getDigitalWalletName(paymentTool.getDigitalWallet()));
            rootNode.put("id", paymentTool.getDigitalWallet().getId());
        } else if (CryptoCurrencyUtil.isSetCryptoCurrency(paymentTool)) {
            rootNode.put("type", "crypto_currency");
            rootNode.put("crypto_currency", CryptoCurrencyUtil.getCryptoCurrencyName(paymentTool));
        } else if (paymentTool.isSetMobileCommerce()) {
            rootNode.put("type", "mobile_commerce");
            MobilePhone mobilePhone = paymentTool.getMobileCommerce().getPhone();
            rootNode.put("mobile_commerce", mobilePhone.getCc() + mobilePhone.getCtn());
        } else {
            throw new UnsupportedOperationException(
                    "Unknown payment tool type. Must be bank card, terminal or digital wallet");
        }
        return Base64.getUrlEncoder().encodeToString(rootNode.toString().getBytes(StandardCharsets.UTF_8));
    }
}
