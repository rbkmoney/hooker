package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.swag_webhook_events.model.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static com.rbkmoney.hooker.dao.impl.InvoicingMessageRowMapper.*;
import static com.rbkmoney.hooker.dao.impl.InvoicingMessageRowMapper.PAYMENT_RECURRENT_PARENT_PAYMENT_ID;
import static com.rbkmoney.hooker.utils.PaymentToolUtils.getPaymentToolDetails;

public class PayerTypeUtils {

    public static void fillPayerTypeParam(MapSqlParameterSource params, Payment payment) {
        Payer.PayerTypeEnum payerType = payment.getPayer().getPayerType();
        params.addValue(PAYMENT_PAYER_TYPE, payerType.getValue());
        switch (payerType) {
            case CUSTOMERPAYER:
                params.addValue(PAYMENT_CUSTOMER_ID, ((CustomerPayer) payment.getPayer()).getCustomerID());
                break;
            case PAYMENTRESOURCEPAYER:
                PaymentResourcePayer payer = (PaymentResourcePayer) payment.getPayer();
                params.addValue(PAYMENT_TOOL_TOKEN, payer.getPaymentToolToken())
                        .addValue(PAYMENT_SESSION, payer.getPaymentSession())
                        .addValue(PAYMENT_EMAIL, payer.getContactInfo().getEmail())
                        .addValue(PAYMENT_PHONE, payer.getContactInfo().getPhoneNumber())
                        .addValue(PAYMENT_IP, payer.getClientInfo().getIp())
                        .addValue(PAYMENT_FINGERPRINT, payer.getClientInfo().getFingerprint());

                PaymentToolUtils.setPaymentToolDetailsParam(params, payer.getPaymentToolDetails(),
                        PAYMENT_TOOL_DETAILS_TYPE, PAYMENT_CARD_BIN, PAYMENT_CARD_LAST_DIGITS, PAYMENT_CARD_NUMBER_MASK, PAYMENT_CARD_TOKEN_PROVIDER, PAYMENT_SYSTEM, PAYMENT_TERMINAL_PROVIDER,
                        PAYMENT_DIGITAL_WALLET_PROVIDER, PAYMENT_DIGITAL_WALLET_ID, PAYMENT_CRYPTO_CURRENCY);
                break;
            case RECURRENTPAYER:
                RecurrentPayer recurrentPayer = (RecurrentPayer) payment.getPayer();
                params.addValue(PAYMENT_RECURRENT_PARENT_INVOICE_ID, recurrentPayer.getRecurrentParentPayment().getInvoiceID())
                        .addValue(PAYMENT_RECURRENT_PARENT_PAYMENT_ID, recurrentPayer.getRecurrentParentPayment().getPaymentID());
                break;
            default:
                throw new UnsupportedOperationException("Unknown payerType " + payerType + "; must be one of these: " + Arrays.toString(Payer.PayerTypeEnum.values()));
        }
    }

    public static void fillPayerType(ResultSet rs, Payment payment) throws SQLException {
        Payer.PayerTypeEnum payerType = Payer.PayerTypeEnum.fromValue(rs.getString(PAYMENT_PAYER_TYPE));
        switch (payerType) {
            case CUSTOMERPAYER:
                payment.setPayer(new CustomerPayer().customerID(rs.getString(PAYMENT_CUSTOMER_ID)));
                break;
            case PAYMENTRESOURCEPAYER:
                PaymentResourcePayer payer = new PaymentResourcePayer()
                        .paymentToolToken(rs.getString(PAYMENT_TOOL_TOKEN))
                        .paymentSession(rs.getString(PAYMENT_SESSION))
                        .contactInfo(new ContactInfo()
                                .email(rs.getString(PAYMENT_EMAIL))
                                .phoneNumber(rs.getString(PAYMENT_PHONE)))
                        .clientInfo(new ClientInfo()
                                .fingerprint(rs.getString(PAYMENT_FINGERPRINT))
                                .ip(rs.getString(PAYMENT_IP)));

                payer.setPaymentToolDetails(getPaymentToolDetails(rs.getString(PAYMENT_TOOL_DETAILS_TYPE), rs.getString(PAYMENT_CARD_BIN),
                        rs.getString(PAYMENT_CARD_LAST_DIGITS), rs.getString(PAYMENT_CARD_NUMBER_MASK), rs.getString(PAYMENT_CARD_TOKEN_PROVIDER), rs.getString(PAYMENT_SYSTEM), rs.getString(PAYMENT_TERMINAL_PROVIDER),
                        rs.getString(PAYMENT_DIGITAL_WALLET_PROVIDER), rs.getString(PAYMENT_DIGITAL_WALLET_ID), rs.getString(PAYMENT_CRYPTO_CURRENCY)));
                payment.setPayer(payer);
                break;
            case RECURRENTPAYER:
                payment.setPayer(new RecurrentPayer()
                        .recurrentParentPayment(new PaymentRecurrentParent()
                                .invoiceID(rs.getString(PAYMENT_RECURRENT_PARENT_INVOICE_ID))
                                .paymentID(rs.getString(PAYMENT_RECURRENT_PARENT_PAYMENT_ID)))
                        .contactInfo(new ContactInfo()
                                .email(rs.getString(PAYMENT_EMAIL))
                                .phoneNumber(rs.getString(PAYMENT_PHONE))));
                break;
            default:
                throw new UnsupportedOperationException("Unknown payerType "+payerType+"; must be one of these: "+ Arrays.toString(Payer.PayerTypeEnum.values()));
        }
        payment.getPayer().setPayerType(payerType);
    }

}
