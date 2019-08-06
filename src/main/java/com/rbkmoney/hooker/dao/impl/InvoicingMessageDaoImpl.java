package com.rbkmoney.hooker.dao.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.NotFoundException;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.Refund;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.FilterUtils;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.swag_webhook_events.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rbkmoney.hooker.dao.impl.InvoicingMessageRowMapper.*;
import static com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicingMessageDaoImpl implements InvoicingMessageDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final Cache<String, InvoicingMessage> invoicingCache;

    private final InvoicingCartDao invoicingCartDao;

    private static RowMapper<InvoicingMessage> messageRowMapper = new InvoicingMessageRowMapper();

    //TODO refactoring
    private static void setNullPaymentParamValues(MapSqlParameterSource params) {
        params.addValue(PAYMENT_ID, null)
                .addValue(PAYMENT_CREATED_AT, null)
                .addValue(PAYMENT_STATUS, null)
                .addValue(PAYMENT_FAILURE, null)
                .addValue(PAYMENT_FAILURE_REASON, null)
                .addValue(PAYMENT_AMOUNT, null)
                .addValue(PAYMENT_FEE, null)
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

    public List<InvoicingMessage> saveBatch(List<InvoicingMessage> messages) throws DaoException {
        int[] batchResult = saveBatchMessages(messages);
        List<InvoicingMessage> filteredMessages = FilterUtils.filter(batchResult, messages);
        saveBatchCart(filteredMessages);
        return filteredMessages;
    }

    private int[] saveBatchMessages(List<InvoicingMessage> messages) {
        try {
            messages.forEach(m -> invoicingCache.put(key(m), m));
            final String sql = "INSERT INTO hook.message" +
                    "(id, new_event_id, event_time, sequence_id, change_id, type, party_id, event_type, " +
                    "invoice_id, shop_id, invoice_created_at, invoice_status, invoice_reason, invoice_due_date, invoice_amount, " +
                    "invoice_currency, invoice_content_type, invoice_content_data, invoice_product, invoice_description, " +
                    "payment_id, payment_created_at, payment_status, payment_failure, payment_failure_reason, payment_amount, " +
                    "payment_currency, payment_content_type, payment_content_data, payment_tool_token, payment_session, payment_email, payment_phone, payment_ip, payment_fingerprint, " +
                    "payment_customer_id, payment_payer_type, payment_recurrent_parent_invoice_id, payment_recurrent_parent_payment_id, payment_tool_details_type, payment_card_bin, payment_card_last_digits, payment_card_number_mask, payment_card_token_provider, payment_system, payment_terminal_provider, " +
                    "payment_digital_wallet_provider, payment_digital_wallet_id, payment_crypto_currency, payment_fee, " +
                    "refund_id, refund_created_at, refund_status, refund_failure, refund_failure_reason, refund_amount, refund_currency, refund_reason) " +
                    "VALUES " +
                    "(:id, :new_event_id, :event_time, :sequence_id, :change_id, :type, :party_id, CAST(:event_type as hook.eventtype), " +
                    ":invoice_id, :shop_id, :invoice_created_at, :invoice_status, :invoice_reason, :invoice_due_date, :invoice_amount, " +
                    ":invoice_currency, :invoice_content_type, :invoice_content_data, :invoice_product, :invoice_description, " +
                    ":payment_id, :payment_created_at, :payment_status, :payment_failure, :payment_failure_reason, :payment_amount, " +
                    ":payment_currency, :payment_content_type, :payment_content_data, :payment_tool_token, :payment_session, :payment_email, :payment_phone, :payment_ip, :payment_fingerprint, " +
                    ":payment_customer_id, CAST(:payment_payer_type as hook.payment_payer_type), :payment_recurrent_parent_invoice_id, :payment_recurrent_parent_payment_id, CAST(:payment_tool_details_type as hook.payment_tool_details_type), " +
                    ":payment_card_bin, :payment_card_last_digits, :payment_card_number_mask, :payment_card_token_provider, :payment_system, :payment_terminal_provider, :payment_digital_wallet_provider, :payment_digital_wallet_id, :payment_crypto_currency, :payment_fee, " +
                    ":refund_id, :refund_created_at, :refund_status, :refund_failure, :refund_failure_reason, :refund_amount, :refund_currency, :refund_reason) " +
                    "ON CONFLICT (invoice_id, sequence_id, change_id) DO NOTHING ";

            MapSqlParameterSource[] sqlParameterSources = messages.stream().map(message -> {
                MapSqlParameterSource params = new MapSqlParameterSource()
                        .addValue(ID, message.getId())
                        .addValue(NEW_EVENT_ID, message.getEventId())
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
                            .addValue(PAYMENT_CREATED_AT, payment.getCreatedAt())
                            .addValue(PAYMENT_STATUS, payment.getStatus())
                            .addValue(PAYMENT_FAILURE, payment.getError() != null ? ErrorUtils.toStringFailure(payment.getError()) : null)
                            .addValue(PAYMENT_FAILURE_REASON, payment.getError() != null ? payment.getError().getMessage() : null)
                            .addValue(PAYMENT_AMOUNT, payment.getAmount())
                            .addValue(PAYMENT_AMOUNT, payment.getFee())
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
                return params;
            }).toArray(MapSqlParameterSource[]::new);

            return jdbcTemplate.batchUpdate(sql, sqlParameterSources);
        } catch (NestedRuntimeException e) {
            List<String> shortInfo = messages.stream().map(m -> m.getId() + " : " + m.getInvoice().getId()).collect(Collectors.toList());
            throw new DaoException("Couldn't save batch messages: " + shortInfo, e);
        }
    }

    private void saveBatchCart(List<InvoicingMessage> messages) {
        List<InvoiceCartPosition> carts = new ArrayList<>();
        messages.forEach(m -> {
            List<InvoiceCartPosition> cart = m.getInvoice().getCart();
            if (cart != null && !cart.isEmpty()) {
                cart.forEach(c -> c.setMessageId(m.getId()));
                carts.addAll(cart);
            }
        });
        invoicingCartDao.saveBatch(carts);
    }

    private InvoicingMessage getAny(String invoiceId, String paymentId, String refundId, String type) throws NotFoundException, DaoException {
        String key = key(invoiceId, paymentId, refundId);
        InvoicingMessage result = invoicingCache.getIfPresent(key);
        if (result != null) {
            log.info("From cache {}", result.getId());
            return result.copy();
        }
        final String sql = "SELECT * FROM hook.message WHERE invoice_id =:invoice_id" +
                " AND (payment_id IS NULL OR payment_id=:payment_id)" +
                " AND (refund_id IS NULL OR refund_id=:refund_id)" +
                " AND type =:type ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(INVOICE_ID, invoiceId)
                .addValue(PAYMENT_ID, paymentId)
                .addValue(REFUND_ID, refundId)
                .addValue(TYPE, type);
        try {
            result = jdbcTemplate.queryForObject(sql, params, messageRowMapper);
            List<InvoiceCartPosition> cart = invoicingCartDao.getByMessageId(result.getId());
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
        log.info("From database {}", result.getId());
        return result;
    }

    private String key(String... keys) {
        return Stream.of(keys).filter(Objects::nonNull).collect(Collectors.joining("_"));
    }

    private String key(InvoicingMessage message) {
        return key(message.getInvoice().getId(),
                message.getPayment() != null ? message.getPayment().getId() : null,
                message.getRefund() != null ? message.getRefund().getId() : null);
    }

    @Override
    public List<InvoicingMessage> getBy(Collection<Long> messageIds) throws DaoException {
        final String sql = "SELECT * FROM hook.message WHERE id in (:ids)";
        try {
            List<InvoicingMessage> messagesFromDb = jdbcTemplate.query(sql,
                    new MapSqlParameterSource("ids", messageIds), messageRowMapper);
            messagesFromDb.forEach(m -> {
                List<InvoiceCartPosition> positions = invoicingCartDao.getByMessageId(m.getId());
                if (!positions.isEmpty()) {
                    m.getInvoice().setCart(positions);
                }
            });
            return messagesFromDb;
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't get invoice message by ids: " + messageIds, e);
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
