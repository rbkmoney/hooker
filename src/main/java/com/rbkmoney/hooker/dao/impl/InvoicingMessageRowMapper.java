package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.model.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

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
    public static final String INVOICE_STATUS = "invoice_status";
    public static final String PAYMENT_ID = "payment_id";
    public static final String PAYMENT_STATUS = "payment_status";
    public static final String PAYMENT_FEE = "payment_fee";
    public static final String REFUND_ID = "refund_id";
    public static final String REFUND_STATUS = "refund_status";
    public static final String REFUND_AMOUNT = "refund_amount";
    public static final String REFUND_CURRENCY = "refund_currency";

    @Override
    public InvoicingMessage mapRow(ResultSet rs, int i) throws SQLException {
        InvoicingMessage message = new InvoicingMessage();
        message.setId(rs.getLong(ID));
        message.setEventId(rs.getLong(NEW_EVENT_ID));
        message.setEventTime(rs.getString(EVENT_TIME));
        message.setSequenceId(rs.getLong(SEQUENCE_ID));
        message.setChangeId(rs.getInt(CHANGE_ID));
        message.setType(InvoicingMessageEnum.lookup(rs.getString(TYPE)));
        message.setPartyId(rs.getString(PARTY_ID));
        message.setShopID(rs.getString(SHOP_ID));
        message.setEventType(EventType.valueOf(rs.getString(EVENT_TYPE)));
        message.setInvoiceId(rs.getString(INVOICE_ID));
        message.setInvoiceStatus(InvoiceStatusEnum.valueOf(rs.getString(INVOICE_STATUS)));
        if (message.isPayment() || message.isRefund()) {
            message.setPaymentId(rs.getString(PAYMENT_ID));
            message.setPaymentStatus(PaymentStatusEnum.valueOf(rs.getString(PAYMENT_STATUS)));
            message.setPaymentFee(rs.getLong(PAYMENT_FEE));
        }
        if (message.isRefund()) {
            message.setRefundId(rs.getString(REFUND_ID));
            message.setRefundStatus(RefundStatusEnum.valueOf(rs.getString(REFUND_STATUS)));
            message.setRefundAmount(rs.getLong(REFUND_AMOUNT));
            message.setRefundCurrency(rs.getString(REFUND_CURRENCY));
        }
        return message;
    }


}
