package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.NotFoundException;
import com.rbkmoney.hooker.model.Invoice;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.PaymentContactInfo;
import com.rbkmoney.hooker.model.Refund;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.swag_webhook_events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;

import static com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler.*;
import static com.rbkmoney.hooker.utils.PaymentToolUtils.getPaymentToolDetails;

@Slf4j
public class InvoicingMessageDaoImpl extends NamedParameterJdbcDaoSupport implements InvoicingMessageDao {

    @Autowired
    InvoicingQueueDao queueDao;

    @Autowired
    InvoicingTaskDao taskDao;

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
    public static final String REFUND_ID = "refund_id";
    public static final String REFUND_CREATED_AT = "refund_created_at";
    public static final String REFUND_STATUS = "refund_status";
    public static final String REFUND_FAILURE = "refund_failure";
    public static final String REFUND_FAILURE_REASON = "refund_failure_reason";
    public static final String REFUND_AMOUNT = "refund_amount";
    public static final String REFUND_CURRENCY = "refund_currency";
    public static final String REFUND_REASON = "refund_reason";

    //TODO refactoring
    private static void setNullPaymentParamValues(MapSqlParameterSource params) {
        params.addValue(PAYMENT_ID, null)
                .addValue(PAYMENT_CREATED_AT, null)
                .addValue(PAYMENT_STATUS, null)
                .addValue(PAYMENT_FAILURE, null)
                .addValue(PAYMENT_FAILURE_REASON, null)
                .addValue(PAYMENT_AMOUNT, null)
                .addValue(PAYMENT_CURRENCY, null)
                .addValue(PAYMENT_CONTENT_TYPE, null)
                .addValue(PAYMENT_CONTENT_DATA, null)
                .addValue(PAYMENT_TOOL_TOKEN, null)
                .addValue(PAYMENT_SESSION, null)
                .addValue(PAYMENT_EMAIL, null)
                .addValue(PAYMENT_PHONE, null)
                .addValue(PAYMENT_IP, null)
                .addValue(PAYMENT_FINGERPRINT, null)
                .addValue(PAYMENT_CUSTOMER_ID, null)
                .addValue(PAYMENT_PAYER_TYPE, null)
                .addValue(PAYMENT_RECURRENT_PARENT_INVOICE_ID, null)
                .addValue(PAYMENT_RECURRENT_PARENT_PAYMENT_ID, null)
                .addValue(PAYMENT_TOOL_DETAILS_TYPE, null)
                .addValue(PAYMENT_CARD_BIN, null)
                .addValue(PAYMENT_CARD_LAST_DIGITS, null)
                .addValue(PAYMENT_CARD_NUMBER_MASK, null)
                .addValue(PAYMENT_CARD_TOKEN_PROVIDER, null)
                .addValue(PAYMENT_SYSTEM, null)
                .addValue(PAYMENT_TERMINAL_PROVIDER, null)
                .addValue(PAYMENT_DIGITAL_WALLET_PROVIDER, null)
                .addValue(PAYMENT_DIGITAL_WALLET_ID, null)
                .addValue(PAYMENT_CRYPTO_CURRENCY, null)
                .addValue(REFUND_ID, null)
                .addValue(REFUND_CREATED_AT, null)
                .addValue(REFUND_STATUS, null)
                .addValue(REFUND_FAILURE, null)
                .addValue(REFUND_FAILURE_REASON, null)
                .addValue(REFUND_AMOUNT, null)
                .addValue(REFUND_CURRENCY, null)
                .addValue(REFUND_REASON, null);
    }

    private static RowMapper<InvoiceCartPosition> cartPositionRowMapper = (rs, i) -> {
        InvoiceCartPosition invoiceCartPosition = new InvoiceCartPosition();
        invoiceCartPosition.setProduct(rs.getString("product"));
        invoiceCartPosition.setPrice(rs.getLong("price"));
        invoiceCartPosition.setQuantity(rs.getInt("quantity"));
        invoiceCartPosition.setCost(rs.getLong("cost"));
        String rate = rs.getString("rate");
        if (rate != null) {
            invoiceCartPosition.setTaxMode(new TaxMode(rate));
        }
        return invoiceCartPosition;
    };

    private static RowMapper<InvoicingMessage> messageRowMapper = (rs, i) -> {
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
                    throw new UnsupportedOperationException("Unknown payerType "+payerType+"; must be one of these: "+Arrays.toString(Payer.PayerTypeEnum.values()));
            }
            payment.getPayer().setPayerType(payerType);
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
    };

    public InvoicingMessageDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    private InvoicingMessage getAny(String invoiceId, String paymentId, String refundId, String type) throws NotFoundException, DaoException {
        InvoicingMessage result;
        final String sql = "SELECT * FROM hook.message WHERE invoice_id =:invoice_id" +
                " AND (payment_id IS NULL OR payment_id=:payment_id)" +
                " AND (refund_id IS NULL OR refund_id=:refund_id)" +
                " AND type =:type ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(INVOICE_ID, invoiceId)
                .addValue(PAYMENT_ID, paymentId)
                .addValue(REFUND_ID, refundId)
                .addValue(TYPE, type);
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
            List<InvoiceCartPosition> cart = getInvoiceCartPositions(result.getId());
            if (!cart.isEmpty()) {
                result.getInvoice().setCart(cart);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("InvoicingMessage not found with invoiceId=%s, paymentId=%s, refundId=%s, type=%s!",
                    invoiceId, paymentId, refundId, type));
        } catch (NestedRuntimeException e) {
            throw new DaoException(String.format("InvoicingMessage error with invoiceId=%s, paymentId=%s, refundId=%s, type=%s",
                    invoiceId, paymentId, refundId, type), e);
        }
        return result;
    }

    private List<InvoiceCartPosition> getInvoiceCartPositions(Long messageId) {
        final String sqlCarts = "SELECT * FROM hook.cart_position WHERE message_id =:message_id";
        MapSqlParameterSource paramsCarts = new MapSqlParameterSource("message_id", messageId);
        return getNamedParameterJdbcTemplate().query(sqlCarts, paramsCarts, cartPositionRowMapper);
    }

    private void saveCart(Long messageId, Collection<InvoiceCartPosition> cart) {
        if (cart == null || cart.isEmpty()) return;
        int size = cart.size();
        List<Map<String, Object>> batchValues = new ArrayList<>(size);
        for (InvoiceCartPosition cartPosition : cart) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                    .addValue("message_id", messageId)
                    .addValue("product", cartPosition.getProduct())
                    .addValue("price", cartPosition.getPrice())
                    .addValue("quantity", cartPosition.getQuantity())
                    .addValue("cost", cartPosition.getCost())
                    .addValue("rate", cartPosition.getTaxMode() == null ? null : cartPosition.getTaxMode().getRate());
            batchValues.add(mapSqlParameterSource.getValues());
        }

        final String sql = "INSERT INTO hook.cart_position(message_id, product, price, quantity, cost, rate) VALUES (:message_id, :product, :price, :quantity, :cost, :rate) ";

        try {
            int updateCount[] = getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[size]));
            if (updateCount.length != size) {
                throw new DaoException("Couldn't insert cart for messageId " + messageId);
            }
        } catch (NestedRuntimeException e) {
            throw new DaoException("Fail to save cart for messageId " + messageId, e);
        }
    }


    @Override
    @Transactional
    public void create(InvoicingMessage message) throws DaoException {
        final String sql = "INSERT INTO hook.message" +
                "(event_time, sequence_id, change_id, type, party_id, event_type, " +
                "invoice_id, shop_id, invoice_created_at, invoice_status, invoice_reason, invoice_due_date, invoice_amount, " +
                "invoice_currency, invoice_content_type, invoice_content_data, invoice_product, invoice_description, " +
                "payment_id, payment_created_at, payment_status, payment_failure, payment_failure_reason, payment_amount, " +
                "payment_currency, payment_content_type, payment_content_data, payment_tool_token, payment_session, payment_email, payment_phone, payment_ip, payment_fingerprint, " +
                "payment_customer_id, payment_payer_type, payment_recurrent_parent_invoice_id, payment_recurrent_parent_payment_id, payment_tool_details_type, payment_card_bin, payment_card_last_digits, payment_card_number_mask, payment_card_token_provider, payment_system, payment_terminal_provider, " +
                "payment_digital_wallet_provider, payment_digital_wallet_id, payment_crypto_currency, " +
                "refund_id, refund_created_at, refund_status, refund_failure, refund_failure_reason, refund_amount, refund_currency, refund_reason) " +
                "VALUES " +
                "(:event_time, :sequence_id, :change_id, :type, :party_id, CAST(:event_type as hook.eventtype), " +
                ":invoice_id, :shop_id, :invoice_created_at, :invoice_status, :invoice_reason, :invoice_due_date, :invoice_amount, " +
                ":invoice_currency, :invoice_content_type, :invoice_content_data, :invoice_product, :invoice_description, " +
                ":payment_id, :payment_created_at, :payment_status, :payment_failure, :payment_failure_reason, :payment_amount, " +
                ":payment_currency, :payment_content_type, :payment_content_data, :payment_tool_token, :payment_session, :payment_email, :payment_phone, :payment_ip, :payment_fingerprint, " +
                ":payment_customer_id, CAST(:payment_payer_type as hook.payment_payer_type), :payment_recurrent_parent_invoice_id, :payment_recurrent_parent_payment_id, CAST(:payment_tool_details_type as hook.payment_tool_details_type), " +
                ":payment_card_bin, :payment_card_last_digits, :payment_card_number_mask, :payment_card_token_provider, :payment_system, :payment_terminal_provider, :payment_digital_wallet_provider, :payment_digital_wallet_id, :payment_crypto_currency, " +
                ":refund_id, :refund_created_at, :refund_status, :refund_failure, :refund_failure_reason, :refund_amount, :refund_currency, :refund_reason) " +
                "ON CONFLICT (invoice_id, sequence_id, change_id) DO NOTHING " +
                "RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_TIME, message.getEventTime())
                .addValue(SEQUENCE_ID, message.getSequenceId())
                .addValue(CHANGE_ID, message.getChangeId())
                .addValue(TYPE, message.getType())
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(EVENT_TYPE, message.getEventType().toString())
                .addValue(INVOICE_ID, message.getInvoice().getId())
                .addValue(SHOP_ID, message.getInvoice().getShopID())
                .addValue(INVOICE_CREATED_AT, message.getInvoice().getCreatedAt())
                .addValue(INVOICE_STATUS, message.getInvoice().getStatus())
                .addValue(INVOICE_REASON, message.getInvoice().getReason())
                .addValue(INVOICE_DUE_DATE, message.getInvoice().getDueDate())
                .addValue(INVOICE_AMOUNT, message.getInvoice().getAmount())
                .addValue(INVOICE_CURRENCY, message.getInvoice().getCurrency())
                .addValue(INVOICE_CONTENT_TYPE, message.getInvoice().getMetadata().getType())
                .addValue(INVOICE_CONTENT_DATA, message.getInvoice().getMetadata().getData())
                .addValue(INVOICE_PRODUCT, message.getInvoice().getProduct())
                .addValue(INVOICE_DESCRIPTION, message.getInvoice().getDescription());
        //TODO
        setNullPaymentParamValues(params);
        if (message.isPayment() || message.isRefund()) {
            Payment payment = message.getPayment();
            params.addValue(PAYMENT_ID, payment.getId())
                    .addValue(PAYMENT_CREATED_AT,  payment.getCreatedAt())
                    .addValue(PAYMENT_STATUS, payment.getStatus())
                    .addValue(PAYMENT_FAILURE, payment.getError() != null ? ErrorUtils.toStringFailure(payment.getError()) : null)
                    .addValue(PAYMENT_FAILURE_REASON, payment.getError() != null ? payment.getError().getMessage() : null)
                    .addValue(PAYMENT_AMOUNT, payment.getAmount())
                    .addValue(PAYMENT_CURRENCY, payment.getCurrency())
                    .addValue(PAYMENT_CONTENT_TYPE, payment.getMetadata().getType())
                    .addValue(PAYMENT_CONTENT_DATA, payment.getMetadata().getData())
                    .addValue(PAYMENT_TOOL_TOKEN, payment.getPaymentToolToken())
                    .addValue(PAYMENT_SESSION, payment.getPaymentSession())
                    .addValue(PAYMENT_EMAIL, payment.getContactInfo().getEmail())
                    .addValue(PAYMENT_PHONE, payment.getContactInfo().getPhoneNumber())
                    .addValue(PAYMENT_IP, payment.getIp())
                    .addValue(PAYMENT_FINGERPRINT, payment.getFingerprint());

            Payer.PayerTypeEnum payerType = payment.getPayer().getPayerType();
            params.addValue(PAYMENT_PAYER_TYPE, payerType.getValue());
            switch (payerType) {
                case CUSTOMERPAYER:
                    params.addValue(PAYMENT_CUSTOMER_ID, ((CustomerPayer)payment.getPayer()).getCustomerID());
                    break;
                case PAYMENTRESOURCEPAYER:
                    PaymentResourcePayer payer = (PaymentResourcePayer) payment.getPayer();
                    params.addValue(PAYMENT_TOOL_TOKEN, payer.getPaymentToolToken())
                            .addValue(PAYMENT_SESSION, payer.getPaymentSession())
                            .addValue(PAYMENT_EMAIL, payer.getContactInfo().getEmail())
                            .addValue(PAYMENT_PHONE,payer.getContactInfo().getPhoneNumber())
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
                    throw new UnsupportedOperationException("Unknown payerType "+payerType+"; must be one of these: "+Arrays.toString(Payer.PayerTypeEnum.values()));
            }
        }
        if (message.isRefund()) {
            Refund refund = message.getRefund();
            params.addValue(REFUND_ID, refund.getId())
                    .addValue(REFUND_CREATED_AT, refund.getCreatedAt())
                    .addValue(REFUND_STATUS, refund.getStatus())
                    .addValue(PAYMENT_FAILURE, refund.getError() != null ? refund.getError().getCode() : null)
                    .addValue(PAYMENT_FAILURE_REASON, refund.getError() != null ? refund.getError().getMessage() : null)
                    .addValue(REFUND_AMOUNT, refund.getAmount())
                    .addValue(REFUND_CURRENCY, refund.getCurrency())
                    .addValue(REFUND_REASON, refund.getReason());
        }
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                message.setId(key.longValue());
                saveCart(message.getId(), message.getInvoice().getCart());
                log.info("InvoicingMessage {} saved to db.", message);
                queueDao.createWithPolicy(message.getId());
                taskDao.create(message.getId());
            }
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't create message with invoice_id "+ message.getInvoice().getId(), e);
        }
    }

    @Override
    public Long getMaxEventId() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public List<InvoicingMessage> getBy(Collection<Long> messageIds) throws DaoException {
        final String sql = "SELECT * FROM hook.message WHERE id in (:ids)";
        try {
            List<InvoicingMessage> messagesFromDb = getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", messageIds), messageRowMapper);
            messagesFromDb.forEach(m -> {
                List<InvoiceCartPosition> positions = getInvoiceCartPositions(m.getId());
                if (!positions.isEmpty()) {
                    m.getInvoice().setCart(positions);
                }
            });
            log.debug("messagesFromDb {}", messagesFromDb);
            return messagesFromDb;
        } catch (NestedRuntimeException e) {
            throw new DaoException("InvoicingMessageDaoImpl.getByIds error", e);
        }
    }


    @Override
    public InvoicingMessage getInvoice(String invoiceId) throws NotFoundException, DaoException {
        return getAny(invoiceId, null, null, INVOICE);
    }

    @Override
    public InvoicingMessage getPayment(String invoiceId, String paymentId) throws NotFoundException, DaoException {
        return getAny(invoiceId, paymentId, null, PAYMENT);
    }

    @Override
    public InvoicingMessage getRefund(String invoiceId, String paymentId, String refundId) throws NotFoundException, DaoException {
        return getAny(invoiceId, paymentId, refundId, REFUND);
    }
}
