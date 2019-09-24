package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.model.Invoice;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.PaymentContactInfo;
import com.rbkmoney.hooker.model.Refund;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.PayerTypeUtils;
import com.rbkmoney.swag_webhook_events.model.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static com.rbkmoney.hooker.utils.PaymentToolUtils.getPaymentToolDetails;

public class InvoicingMessageRowMapper implements RowMapper<InvoicingMessage> {

    public static final String ID = "id";
    public static final String NEW_EVENT_ID = "new_event_id";
    public static final String EVENT_TIME = "event_time";
    public static final String SEQUENCE_ID = "sequence_id";
    public static final String CHANGE_ID = "change_id";
    public static final String TYPE = "type";
    public static final String PARTY_ID = "party_id";
    public static final String EVENT_TYPE = "event_type";
    public static final String INVOICE_ID = "invoice_id";
    public static final String SHOP_ID = "shop_id";
    public static final String INVOICE_CREATED_AT = "invoice_created_at";
    public static final String INVOICE_STATUS = "invoice_status";
    public static final String INVOICE_REASON = "invoice_reason";
    public static final String INVOICE_DUE_DATE = "invoice_due_date";
    public static final String INVOICE_AMOUNT = "invoice_amount";
    public static final String INVOICE_CURRENCY = "invoice_currency";
    public static final String INVOICE_CONTENT_TYPE = "invoice_content_type";
    public static final String INVOICE_CONTENT_DATA = "invoice_content_data";
    public static final String INVOICE_PRODUCT = "invoice_product";
    public static final String INVOICE_DESCRIPTION = "invoice_description";
    public static final String PAYMENT_ID = "payment_id";
    public static final String PAYMENT_CREATED_AT = "payment_created_at";
    public static final String PAYMENT_STATUS = "payment_status";
    public static final String PAYMENT_FAILURE = "payment_failure";
    public static final String PAYMENT_FAILURE_REASON = "payment_failure_reason";
    public static final String PAYMENT_AMOUNT = "payment_amount";
    public static final String PAYMENT_FEE = "payment_fee";
    public static final String PAYMENT_CURRENCY = "payment_currency";
    public static final String PAYMENT_CONTENT_TYPE = "payment_content_type";
    public static final String PAYMENT_CONTENT_DATA = "payment_content_data";
    public static final String PAYMENT_TOOL_TOKEN = "payment_tool_token";
    public static final String PAYMENT_SESSION = "payment_session";
    public static final String PAYMENT_EMAIL = "payment_email";
    public static final String PAYMENT_PHONE = "payment_phone";
    public static final String PAYMENT_IP = "payment_ip";
    public static final String PAYMENT_FINGERPRINT = "payment_fingerprint";
    public static final String PAYMENT_CUSTOMER_ID = "payment_customer_id";
    public static final String PAYMENT_PAYER_TYPE = "payment_payer_type";
    public static final String PAYMENT_RECURRENT_PARENT_INVOICE_ID = "payment_recurrent_parent_invoice_id";
    public static final String PAYMENT_RECURRENT_PARENT_PAYMENT_ID = "payment_recurrent_parent_payment_id";
    public static final String PAYMENT_TOOL_DETAILS_TYPE = "payment_tool_details_type";
    public static final String PAYMENT_CARD_BIN = "payment_card_bin";
    public static final String PAYMENT_CARD_LAST_DIGITS = "payment_card_last_digits";
    public static final String PAYMENT_CARD_NUMBER_MASK = "payment_card_number_mask";
    public static final String PAYMENT_CARD_TOKEN_PROVIDER = "payment_card_token_provider";
    public static final String PAYMENT_SYSTEM = "payment_system";
    public static final String PAYMENT_TERMINAL_PROVIDER = "payment_terminal_provider";
    public static final String PAYMENT_DIGITAL_WALLET_PROVIDER = "payment_digital_wallet_provider";
    public static final String PAYMENT_DIGITAL_WALLET_ID = "payment_digital_wallet_id";
    public static final String PAYMENT_CRYPTO_CURRENCY = "payment_crypto_currency";
    public static final String PAYMENT_MOBILE_COMMERCE_PHONE_NUMBER = "payment_mobile_commerce_phone_number";
    public static final String REFUND_ID = "refund_id";
    public static final String REFUND_CREATED_AT = "refund_created_at";
    public static final String REFUND_STATUS = "refund_status";
    public static final String REFUND_FAILURE = "refund_failure";
    public static final String REFUND_FAILURE_REASON = "refund_failure_reason";
    public static final String REFUND_AMOUNT = "refund_amount";
    public static final String REFUND_CURRENCY = "refund_currency";
    public static final String REFUND_REASON = "refund_reason";

    @Override
    public InvoicingMessage mapRow(ResultSet rs, int i) throws SQLException {
        InvoicingMessage message = new InvoicingMessage();
        message.setId(rs.getLong(ID));
        message.setEventId(rs.getLong(NEW_EVENT_ID));
        message.setEventTime(rs.getString(EVENT_TIME));
        message.setSequenceId(rs.getLong(SEQUENCE_ID));
        message.setChangeId(rs.getInt(CHANGE_ID));
        message.setType(rs.getString(TYPE));
        message.setPartyId(rs.getString(PARTY_ID));
        message.setEventType(EventType.valueOf(rs.getString(EVENT_TYPE)));
        Invoice invoice = new Invoice();
        message.setInvoice(invoice);
        invoice.setId(rs.getString(INVOICE_ID));
        invoice.setShopID(rs.getString(SHOP_ID));
        invoice.setCreatedAt(rs.getString(INVOICE_CREATED_AT));
        invoice.setStatus(rs.getString(INVOICE_STATUS));
        invoice.setReason(rs.getString(INVOICE_REASON));
        invoice.setDueDate(rs.getString(INVOICE_DUE_DATE));
        invoice.setAmount(rs.getLong(INVOICE_AMOUNT));
        invoice.setCurrency(rs.getString(INVOICE_CURRENCY));
        Content invoiceMetadata = new Content();
        invoiceMetadata.setType(rs.getString(INVOICE_CONTENT_TYPE));
        invoiceMetadata.setData(rs.getBytes(INVOICE_CONTENT_DATA));
        invoice.setMetadata(invoiceMetadata);
        invoice.setProduct(rs.getString(INVOICE_PRODUCT));
        invoice.setDescription(rs.getString(INVOICE_DESCRIPTION));
        if (message.isPayment() || message.isRefund()) {
            Payment payment = new Payment();
            message.setPayment(payment);
            payment.setId(rs.getString(PAYMENT_ID));
            payment.setCreatedAt(rs.getString(PAYMENT_CREATED_AT));
            payment.setStatus(rs.getString(PAYMENT_STATUS));
            if (rs.getString(PAYMENT_FAILURE) != null && "failed".equals(rs.getString(PAYMENT_STATUS))) {
                payment.setError(ErrorUtils.toPaymentError(rs.getString(PAYMENT_FAILURE), rs.getString(PAYMENT_FAILURE_REASON)));
            }
            payment.setAmount(rs.getLong(PAYMENT_AMOUNT));
            payment.setFee(rs.getLong(PAYMENT_FEE));
            payment.setCurrency(rs.getString(PAYMENT_CURRENCY));
            Content paymentMetadata = new Content();
            paymentMetadata.setType(rs.getString(PAYMENT_CONTENT_TYPE));
            paymentMetadata.setData(rs.getBytes(PAYMENT_CONTENT_DATA));
            payment.setMetadata(paymentMetadata);
            payment.setPaymentToolToken(rs.getString(PAYMENT_TOOL_TOKEN));
            payment.setPaymentSession(rs.getString(PAYMENT_SESSION));
            payment.setContactInfo(new PaymentContactInfo(rs.getString(PAYMENT_EMAIL), rs.getString(PAYMENT_PHONE)));
            payment.setIp(rs.getString(PAYMENT_IP));
            payment.setFingerprint(rs.getString(PAYMENT_FINGERPRINT));
            PayerTypeUtils.fillPayerType(rs, payment);
        }
        if (message.isRefund()) {
            Refund refund = new Refund();
            message.setRefund(refund);
            refund.setId(rs.getString(REFUND_ID));
            refund.setCreatedAt(rs.getString(REFUND_CREATED_AT));
            refund.setStatus(rs.getString(REFUND_STATUS));
            if (rs.getString(REFUND_FAILURE) != null && "failed".equals(rs.getString(REFUND_STATUS))) {
                refund.setError(ErrorUtils.toPaymentError(rs.getString(REFUND_FAILURE), rs.getString(REFUND_FAILURE_REASON)));
            }
            refund.setAmount(rs.getLong(REFUND_AMOUNT));
            refund.setCurrency(rs.getString(REFUND_CURRENCY));
            refund.setReason(rs.getString(REFUND_REASON));
        }
        return message;
    }


}
