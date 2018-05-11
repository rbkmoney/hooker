package com.rbkmoney.hooker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.DigitalWalletProvider;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.hooker.model.PaymentToolDetailsDigitalWalletWrapper;
import com.rbkmoney.swag_webhook_events.*;
import org.apache.commons.collections4.MapUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

/**
 * Created by inalarsanukaev on 13.10.17.
 */
public class PaymentToolUtils {

    private static final Map<String, String> digitalWalletMapping = ImmutableMap.of(
            PaymentToolDetailsDigitalWallet.DigitalWalletDetailsTypeEnum.DIGITALWALLETDETAILSQIWI.getValue(), DigitalWalletProvider.qiwi.name()
            );

    public static PaymentToolDetails getPaymentToolDetails(PaymentTool paymentTool) {
        String detailsType;
        String bin = null;
        String lastDigits = null;
        String cardNumberMask = null;
        String paymentSystem = null;
        String terminalProvider = null;
        String digitalWalletType = null;
        String digitalWalletId = null;
        if (paymentTool.isSetBankCard()) {
            detailsType = PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSBANKCARD.getValue();
            bin = paymentTool.getBankCard().getBin();
            lastDigits = paymentTool.getBankCard().getMaskedPan();
            cardNumberMask = bin + "******" + lastDigits;
            paymentSystem = paymentTool.getBankCard().getPaymentSystem().name();
        } else if (paymentTool.isSetPaymentTerminal()) {
            detailsType = PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSPAYMENTTERMINAL.getValue();
            terminalProvider = paymentTool.getPaymentTerminal().getTerminalType().name();
        } else if (paymentTool.isSetDigitalWallet()) {
            detailsType = PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSDIGITALWALLET.getValue();
            digitalWalletType = paymentTool.getDigitalWallet().getProvider().name();
            digitalWalletId = paymentTool.getDigitalWallet().getId();
        } else {
            throw new UnsupportedOperationException("Unknown payment tool type. Must be bank card, terminal or digital wallet");
        }
        return getPaymentToolDetails(detailsType, bin, lastDigits, cardNumberMask, paymentSystem, terminalProvider, digitalWalletType, digitalWalletId);
    }

    public static PaymentToolDetails getPaymentToolDetails(String sDetailsType, String bin, String lastDigits, String cardNumberMask, String paymentSystem, String providerTerminal, String digitalWalletProvider, String digitalWalletId) {
        PaymentToolDetails.DetailsTypeEnum detailsType = PaymentToolDetails.DetailsTypeEnum.fromValue(sDetailsType);
        PaymentToolDetails paymentToolDetails;
        switch (detailsType) {
            case PAYMENTTOOLDETAILSBANKCARD:
                paymentToolDetails = new PaymentToolDetailsBankCard()
                        .bin(bin)
                        .lastDigits(lastDigits)
                        .cardNumberMask(cardNumberMask)
                        .paymentSystem(paymentSystem);
                break;
            case PAYMENTTOOLDETAILSPAYMENTTERMINAL:
                paymentToolDetails = new PaymentToolDetailsPaymentTerminal()
                        .provider(PaymentToolDetailsPaymentTerminal.ProviderEnum.fromValue(providerTerminal));
                break;
            case PAYMENTTOOLDETAILSDIGITALWALLET:
                String digitalWalletDatailsTypeString = MapUtils.invertMap(digitalWalletMapping).get(digitalWalletProvider);
                DigitalWalletDetails.DigitalWalletDetailsTypeEnum digitalWalletDetailsType = DigitalWalletDetails.DigitalWalletDetailsTypeEnum.fromValue(digitalWalletDatailsTypeString);
                PaymentToolDetailsDigitalWalletWrapper paymentToolDetailsDigitalWalletWrapper = new PaymentToolDetailsDigitalWalletWrapper();
                switch (digitalWalletDetailsType) {
                    case DIGITALWALLETDETAILSQIWI:
                        paymentToolDetailsDigitalWalletWrapper.setDigitalWalletDetails(new DigitalWalletDetailsQIWI()
                                .phoneNumberMask(digitalWalletId)
                                .digitalWalletDetailsType(DigitalWalletDetails.DigitalWalletDetailsTypeEnum.DIGITALWALLETDETAILSQIWI));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown digitalWalletDetailsType "+digitalWalletDetailsType+"; must be one of these: "+ Arrays.toString(DigitalWalletDetails.DigitalWalletDetailsTypeEnum.values()));
                }
                paymentToolDetailsDigitalWalletWrapper.getDigitalWalletDetails().setDigitalWalletDetailsType(digitalWalletDetailsType);
                paymentToolDetails = paymentToolDetailsDigitalWalletWrapper;
                break;
            default:
                throw new UnsupportedOperationException("Unknown detailsType "+detailsType+"; must be one of these: "+ Arrays.toString(PaymentToolDetails.DetailsTypeEnum.values()));
        }
        paymentToolDetails.detailsType(detailsType);
        return paymentToolDetails;
    }

    public static void setPaymentToolDetailsParam(MapSqlParameterSource params, PaymentToolDetails paymentToolDetails,
                                            String detailsTypeParamName, String binParamName, String lastDigitsParamName, String cardNumberMaskParamName, String paymentSystemParamName, String terminalProviderParamName,
                                                  String digitalWalletProviderParamName, String digitalWalletIdParamName) {
        PaymentToolDetails.DetailsTypeEnum detailsType = paymentToolDetails.getDetailsType();
        params.addValue(detailsTypeParamName, detailsType.getValue());
        switch (detailsType) {
            case PAYMENTTOOLDETAILSBANKCARD:
                PaymentToolDetailsBankCard pCard = (PaymentToolDetailsBankCard) paymentToolDetails;
                params.addValue(binParamName, pCard.getBin())
                        .addValue(lastDigitsParamName, pCard.getLastDigits())
                        .addValue(cardNumberMaskParamName, pCard.getCardNumberMask())
                        .addValue(paymentSystemParamName, pCard.getPaymentSystem());
                break;
            case PAYMENTTOOLDETAILSPAYMENTTERMINAL:
                PaymentToolDetailsPaymentTerminal pTerminal = (PaymentToolDetailsPaymentTerminal) paymentToolDetails;
                params.addValue(terminalProviderParamName, pTerminal.getProvider().getValue());
                break;
            case PAYMENTTOOLDETAILSDIGITALWALLET:
                PaymentToolDetailsDigitalWalletWrapper paymentToolDetailsDigitalWalletWrapper = (PaymentToolDetailsDigitalWalletWrapper) paymentToolDetails;
                DigitalWalletDetails.DigitalWalletDetailsTypeEnum digitalWalletDetailsType = paymentToolDetailsDigitalWalletWrapper.getDigitalWalletDetails().getDigitalWalletDetailsType();
                String digitalWalletProvider = digitalWalletMapping.get(digitalWalletDetailsType.getValue());
                params.addValue(digitalWalletProviderParamName, digitalWalletProvider);
                switch (digitalWalletDetailsType) {
                    case DIGITALWALLETDETAILSQIWI:
                        DigitalWalletDetailsQIWI digitalWalletDetailsQIWI = (DigitalWalletDetailsQIWI) paymentToolDetailsDigitalWalletWrapper.getDigitalWalletDetails();
                        params.addValue(digitalWalletIdParamName, digitalWalletDetailsQIWI.getPhoneNumberMask());
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown digitalWalletDetailsType " + digitalWalletDetailsType + "; must be one of these: " + Arrays.toString(DigitalWalletDetails.DigitalWalletDetailsTypeEnum.values()));
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown detailsType "+detailsType+"; must be one of these: "+Arrays.toString(PaymentToolDetails.DetailsTypeEnum.values()));
        }
    }

    public static String getPaymentToolToken(PaymentTool paymentTool) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        if (paymentTool.isSetBankCard()) {
            BankCard pCard = paymentTool.getBankCard();
            rootNode.put("type", "bank_card");
            rootNode.put("token", pCard.getToken());
            rootNode.put("payment_system", pCard.getPaymentSystem().toString());
            rootNode.put("bin", pCard.getBin());
            rootNode.put("masked_pan", pCard.getMaskedPan());
        } else if (paymentTool.isSetPaymentTerminal()) {
            rootNode.put("type", "payment_terminal");
            rootNode.put("terminal_type", paymentTool.getPaymentTerminal().getTerminalType().toString());
        } else if (paymentTool.isSetDigitalWallet()) {
            rootNode.put("type", "digital_wallet");
            rootNode.put("provider", paymentTool.getDigitalWallet().getProvider().name());
            rootNode.put("id", paymentTool.getDigitalWallet().getId());
        } else {
            throw new UnsupportedOperationException("Unknown payment tool type. Must be bank card, terminal or digital wallet");
        }
        return Base64.getUrlEncoder().encodeToString(rootNode.toString().getBytes(StandardCharsets.UTF_8));
    }
}
