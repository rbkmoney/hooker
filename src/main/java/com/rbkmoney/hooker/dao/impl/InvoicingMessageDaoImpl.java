package com.rbkmoney.hooker.dao.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.exception.NotFoundException;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.utils.KeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rbkmoney.hooker.dao.impl.InvoicingMessageRowMapper.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicingMessageDaoImpl implements InvoicingMessageDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final Cache<InvoicingMessageKey, InvoicingMessage> invoicingCache;

    private static RowMapper<InvoicingMessage> messageRowMapper = new InvoicingMessageRowMapper();

    public void saveBatch(List<InvoicingMessage> messages) throws DaoException {
        int[] batchMessagesResult = saveBatchMessages(messages);
        log.info("Batch messages saved info {}",
                IntStream.range(0, messages.size())
                        .mapToObj(i -> "(" + i + " : " + batchMessagesResult[i] + " : " + messages.get(i).getId() + " : " + messages.get(i).getInvoiceId() + ")")
                        .collect(Collectors.toList()));
    }

    private int[] saveBatchMessages(List<InvoicingMessage> messages) {
        try {
            messages.forEach(m -> invoicingCache.put(KeyUtils.key(m), m));

            final String sql = "INSERT INTO hook.message" +
                    "(id, new_event_id, event_time, sequence_id, change_id, type, party_id, event_type, " +
                    "invoice_id, shop_id, invoice_status, payment_id, payment_status, refund_id, refund_status) " +
                    "VALUES " +
                    "(:id, :new_event_id, :event_time, :sequence_id, :change_id, :type, :party_id, CAST(:event_type as hook.eventtype), " +
                    ":invoice_id, :shop_id, :invoice_status, :payment_id, :payment_status, :refund_id, :refund_status) " +
                    "ON CONFLICT (invoice_id, sequence_id, change_id) DO NOTHING ";

            MapSqlParameterSource[] sqlParameterSources = messages.stream()
                    .map(message -> new MapSqlParameterSource()
                            .addValue(ID, message.getId())
                            .addValue(NEW_EVENT_ID, message.getEventId())
                            .addValue(EVENT_TIME, message.getEventTime())
                            .addValue(SEQUENCE_ID, message.getSequenceId())
                            .addValue(CHANGE_ID, message.getChangeId())
                            .addValue(TYPE, message.getType().getValue())
                            .addValue(PARTY_ID, message.getPartyId())
                            .addValue(EVENT_TYPE, message.getEventType().toString())
                            .addValue(INVOICE_ID, message.getInvoiceId())
                            .addValue(SHOP_ID, message.getShopId())
                            .addValue(INVOICE_STATUS, message.getInvoiceStatus().getValue())
                            .addValue(PAYMENT_ID, message.getPaymentId())
                            .addValue(PAYMENT_STATUS, message.getPaymentStatus() != null ? message.getPaymentStatus().getValue() : null)
                            .addValue(REFUND_ID, message.getRefundId())
                            .addValue(REFUND_STATUS, message.getRefundStatus() != null ? message.getRefundStatus().getValue() : null))
                    .toArray(MapSqlParameterSource[]::new);
            return jdbcTemplate.batchUpdate(sql, sqlParameterSources);
        } catch (NestedRuntimeException e) {
            List<String> shortInfo = messages.stream()
                    .map(m -> "(" + m.getId() + " : " + m.getInvoiceId() + ")")
                    .collect(Collectors.toList());
            throw new DaoException("Couldn't save batch messages: " + shortInfo, e);
        }
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
                .addValue(TYPE, key.getType().getValue());
        try {
            return jdbcTemplate.queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("InvoicingMessage not found %s!", key.toString()));
        } catch (NestedRuntimeException e) {
            throw new DaoException(String.format("InvoicingMessage error %s", key.toString()), e);
        }
    }

    @Override
    public List<InvoicingMessage> getBy(Collection<Long> messageIds) throws DaoException {
        final String sql = "SELECT * FROM hook.message WHERE id in (:ids)";
        try {
            return jdbcTemplate.query(sql, new MapSqlParameterSource("ids", messageIds), messageRowMapper);
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't get invoice message by ids: " + messageIds, e);
        }
    }
}
