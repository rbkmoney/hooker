package com.rbkmoney.hooker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.swag_webhook_events.PaymentToolDetails;
import com.rbkmoney.swag_webhook_events.PaymentToolDetailsBankCard;
import com.rbkmoney.swag_webhook_events.PaymentToolDetailsPaymentTerminal;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by inalarsanukaev on 13.10.17.
 */
public class PaymentToolUtils {
    public static PaymentToolDetails getPaymentToolDetails(String sDetailsType, String cardNum, String paymentSystem, String providerTerminal) {
        PaymentToolDetails.DetailsTypeEnum detailsType = PaymentToolDetails.DetailsTypeEnum.fromValue(sDetailsType);
        PaymentToolDetails paymentToolDetails;
        switch (detailsType) {
            case PAYMENTTOOLDETAILSBANKCARD:
                paymentToolDetails = new PaymentToolDetailsBankCard()
                        .cardNumberMask(cardNum)
                        .paymentSystem(paymentSystem);
                break;
            case PAYMENTTOOLDETAILSPAYMENTTERMINAL:
                paymentToolDetails = new PaymentToolDetailsPaymentTerminal()
                        .provider(PaymentToolDetailsPaymentTerminal.ProviderEnum.fromValue(providerTerminal));
                break;
            default:
                throw new UnsupportedOperationException("Unknown detailsType "+detailsType+"; must be one of these: "+ Arrays.toString(PaymentToolDetails.DetailsTypeEnum.values()));
        }
        paymentToolDetails.detailsType(detailsType);
        return paymentToolDetails;
    }

    public static void setPaymentToolDetailsParam(MapSqlParameterSource params, PaymentToolDetails paymentToolDetails,
                                            String detailsTypeParamName, String cardNumParamName, String paymentSystemParamName, String terminalProviderParamName) {
        PaymentToolDetails.DetailsTypeEnum detailsType = paymentToolDetails.getDetailsType();
        params.addValue(detailsTypeParamName, detailsType.getValue());
        switch (detailsType) {
            case PAYMENTTOOLDETAILSBANKCARD:
                PaymentToolDetailsBankCard pCard = (PaymentToolDetailsBankCard) paymentToolDetails;
                params.addValue(cardNumParamName, pCard.getCardNumberMask())
                        .addValue(paymentSystemParamName, pCard.getPaymentSystem());
                break;
            case PAYMENTTOOLDETAILSPAYMENTTERMINAL:
                PaymentToolDetailsPaymentTerminal pTerminal = (PaymentToolDetailsPaymentTerminal) paymentToolDetails;
                params.addValue(terminalProviderParamName, pTerminal.getProvider().getValue());
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
        } else {
            throw new UnsupportedOperationException("Unknown payment tool type. Must be bank card or terminal");
        }
        return Base64.getUrlEncoder().encodeToString(rootNode.toString().getBytes(StandardCharsets.UTF_8));
    }
}
