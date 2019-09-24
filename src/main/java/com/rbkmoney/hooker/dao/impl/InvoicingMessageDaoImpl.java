package com.rbkmoney.hooker.dao.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.NotFoundException;
import com.rbkmoney.hooker.model.Payment;
import com.rbkmoney.hooker.model.Refund;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.KeyUtils;
import com.rbkmoney.hooker.utils.PayerTypeUtils;
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
import java.util.stream.IntStream;

import static com.rbkmoney.hooker.dao.impl.InvoicingMessageRowMapper.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicingMessageDaoImpl implements InvoicingMessageDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final Cache<InvoicingMessageKey, InvoicingMessage> invoicingCache;

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
                .addValue(PAYMENT_MOBILE_COMMERCE_PHONE_NUMBER, null)
                .addValue(REFUND_ID, null)
                .addValue(REFUND_CREATED_AT, null)
                .addValue(REFUND_STATUS, null)
                .addValue(REFUND_FAILURE, null)
                .addValue(REFUND_FAILURE_REASON, null)
                .addValue(REFUND_AMOUNT, null)
                .addValue(REFUND_CURRENCY, null)
                .addValue(REFUND_REASON, null);
    }

    public void saveBatch(List<InvoicingMessage> messages) throws DaoException {
        int[] batchMessagesResult = saveBatchMessages(messages);
        log.info("Batch messages saved info {}",
                IntStream.range(0, messages.size())
                        .mapToObj(i -> "(" + i + " : " + batchMessagesResult[i] + " : " + messages.get(i).getId() + " : " + messages.get(i).getInvoice().getId() + ")")
                        .collect(Collectors.toList()));
        saveBatchCart(messages);
    }

    private int[] saveBatchMessages(List<InvoicingMessage> messages) {
        try {
            messages.forEach(m -> invoicingCache.put(KeyUtils.key(m), m));

            final String sql = "INSERT INTO hook.message" +
                    "(id, new_event_id, event_time, sequence_id, change_id, type, party_id, event_type, " +
                    "invoice_id, shop_id, invoice_created_at, invoice_status, invoice_reason, invoice_due_date, invoice_amount, " +
                    "invoice_currency, invoice_content_type, invoice_content_data, invoice_product, invoice_description, " +
                    "payment_id, payment_created_at, payment_status, payment_failure, payment_failure_reason, payment_amount, " +
                    "payment_currency, payment_content_type, payment_content_data, payment_tool_token, payment_session, payment_email, payment_phone, payment_ip, payment_fingerprint, " +
                    "payment_customer_id, payment_payer_type, payment_recurrent_parent_invoice_id, payment_recurrent_parent_payment_id, payment_tool_details_type, payment_card_bin, payment_card_last_digits, payment_card_number_mask, payment_card_token_provider, payment_system, payment_terminal_provider, " +
                    "payment_digital_wallet_provider, payment_digital_wallet_id, payment_crypto_currency, payment_mobile_commerce_phone_number, payment_fee, " +
                    "refund_id, refund_created_at, refund_status, refund_failure, refund_failure_reason, refund_amount, refund_currency, refund_reason) " +
                    "VALUES " +
                    "(:id, :new_event_id, :event_time, :sequence_id, :change_id, :type, :party_id, CAST(:event_type as hook.eventtype), " +
                    ":invoice_id, :shop_id, :invoice_created_at, :invoice_status, :invoice_reason, :invoice_due_date, :invoice_amount, " +
                    ":invoice_currency, :invoice_content_type, :invoice_content_data, :invoice_product, :invoice_description, " +
                    ":payment_id, :payment_created_at, :payment_status, :payment_failure, :payment_failure_reason, :payment_amount, " +
                    ":payment_currency, :payment_content_type, :payment_content_data, :payment_tool_token, :payment_session, :payment_email, :payment_phone, :payment_ip, :payment_fingerprint, " +
                    ":payment_customer_id, CAST(:payment_payer_type as hook.payment_payer_type), :payment_recurrent_parent_invoice_id, :payment_recurrent_parent_payment_id, CAST(:payment_tool_details_type as hook.payment_tool_details_type), " +
                    ":payment_card_bin, :payment_card_last_digits, :payment_card_number_mask, :payment_card_token_provider, :payment_system, :payment_terminal_provider, :payment_digital_wallet_provider, :payment_digital_wallet_id, :payment_crypto_currency, :payment_mobile_commerce_phone_number, :payment_fee, " +
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
                            .addValue(PAYMENT_FEE, payment.getFee())
                            .addValue(PAYMENT_CURRENCY, payment.getCurrency())
                            .addValue(PAYMENT_CONTENT_TYPE, payment.getMetadata().getType())
                            .addValue(PAYMENT_CONTENT_DATA, payment.getMetadata().getData())
                            .addValue(PAYMENT_TOOL_TOKEN, payment.getPaymentToolToken())
                            .addValue(PAYMENT_SESSION, payment.getPaymentSession())
                            .addValue(PAYMENT_EMAIL, payment.getContactInfo().getEmail())
                            .addValue(PAYMENT_PHONE, payment.getContactInfo().getPhoneNumber())
                            .addValue(PAYMENT_IP, payment.getIp())
                            .addValue(PAYMENT_FINGERPRINT, payment.getFingerprint());

                    PayerTypeUtils.fillPayerTypeParam(params, payment);
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
            List<String> shortInfo = messages.stream()
                    .map(m -> "(" + m.getId() + " : " + m.getInvoice().getId() + ")")
                    .collect(Collectors.toList());
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
        int[] batchResult = invoicingCartDao.saveBatch(carts);
        log.info("Batch carts saved info {}",
                IntStream.range(0, carts.size())
                        .mapToObj(i -> "(" + i + " : " + batchResult[i] + " : " + carts.get(i).getMessageId() + ")")
                        .collect(Collectors.toList()));
    }

    @Override
    public InvoicingMessage getInvoicingMessage(InvoicingMessageKey key) throws NotFoundException, DaoException {
        InvoicingMessage result = invoicingCache.getIfPresent(key);
        if (result != null) {
            return result;
        }
        final String sql = "SELECT * FROM hook.message WHERE invoice_id =:invoice_id" +
                " AND (payment_id IS NULL OR payment_id=:payment_id)" +
                " AND (refund_id IS NULL OR refund_id=:refund_id)" +
                " AND type =:type ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(INVOICE_ID, key.getInvoiceId())
                .addValue(PAYMENT_ID, key.getPaymentId())
                .addValue(REFUND_ID, key.getRefundId())
                .addValue(TYPE, key.getType().value());
        try {
            result = jdbcTemplate.queryForObject(sql, params, messageRowMapper);
            List<InvoiceCartPosition> cart = invoicingCartDao.getByMessageId(result.getId());
            if (!cart.isEmpty()) {
                result.getInvoice().setCart(cart);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("InvoicingMessage not found %s!", key.toString()));
        } catch (NestedRuntimeException e) {
            throw new DaoException(String.format("InvoicingMessage error %s", key.toString()), e);
        }
        return result;
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
}
