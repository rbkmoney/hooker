package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.model.EventStatus;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class MessageDaoImpl extends NamedParameterJdbcDaoSupport implements MessageDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    private static RowMapper<Message> messageRowMapper = (rs, i) -> {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setInvoiceId(rs.getString("invoice_id"));
        message.setPartyId(rs.getString("party_id"));
        message.setShopId(rs.getInt("shop_id"));
        message.setAmount(rs.getLong("amount"));
        message.setCurrency(rs.getString("currency"));
        message.setCreatedAt(rs.getString("created_at"));
        Content metadata = new Content();
        metadata.setType(rs.getString("content_type"));
        metadata.setData(rs.getBytes("content_data"));
        message.setMetadata(metadata);

        message.setEventStatus(EventStatus.valueOf(rs.getString("event_status")));
        message.setEventType(EventType.valueOf(rs.getString("event_type")));
        message.setEventId(rs.getLong("event_id"));
        message.setType(rs.getString("type"));
        message.setStatus(rs.getString("status"));
        message.setPaymentId(rs.getString("payment_id"));
        return message;
    };

    public MessageDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public Message getAny(String invoiceId) throws DaoException {
        Message result = null;
        final String sql = "SELECT * FROM hook.message WHERE invoice_id =:invoice_id LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource("invoice_id", invoiceId);
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Message with invoice id "+invoiceId+" not exist!");
        } catch (NestedRuntimeException e) {
            log.warn("MessageDaoImpl.getAny error", e);
            throw new DaoException(e);
        }
        return result;
    }

    @Override
    public boolean save(Message message) throws DaoException {
        String invoiceId = message.getInvoiceId();
        final String sql = "INSERT INTO hook.message(invoice_id, party_id, shop_id, amount, currency, created_at, content_type, content_data, event_id, event_type, event_status, type, payment_id, status) " +
                "VALUES (:invoice_id, :party_id, :shop_id, :amount, :currency, :created_at, :content_type, :content_data, :event_id," +
                " CAST(:event_type as hook.eventtype), CAST(:event_status as hook.eventstatus), :type, :payment_id, :status) ";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("invoice_id", invoiceId)
                .addValue("party_id", message.getPartyId())
                .addValue("shop_id", message.getShopId())
                .addValue("amount", message.getAmount())
                .addValue("currency", message.getCurrency())
                .addValue("created_at", message.getCreatedAt())
                .addValue("content_type", message.getMetadata().getType())
                .addValue("content_data", message.getMetadata().getData())
                .addValue("type", message.getType())
                .addValue("type", message.getType())
                .addValue("event_id", message.getEventId())
                .addValue("event_type", message.getEventType().toString())
                .addValue("event_status", message.getEventStatus().toString())
                .addValue("payment_id", message.getPaymentId())
                .addValue("status", message.getStatus());
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, params);
            if (updateCount != 1) {
                return false;
            }
        } catch (NestedRuntimeException e) {
            log.warn("MessageDaoImpl.create error", e);
            throw new DaoException(e);
        }
        log.info("Party info with invoiceId = {} added to table", invoiceId);
        return true;
    }

    @Override
    public Long getMaxEventId() {
        final String sql = "SELECT max(event_id) FROM hook.message";
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<>(), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Message> getBy(Collection<Long> messageIds) {
        if(messageIds == null || messageIds.size() == 0){
            return new ArrayList<>();
        }
        final String sql = "SELECT * FROM hook.message WHERE id in (:ids)";
        try {
            List<Message> messages = getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", messageIds), messageRowMapper);
            return messages;
        }  catch (DataAccessException e) {
            log.error("MessageDaoImpl.getByIds error", e);
            throw new DaoException(e);
        }
    }

    @Override
    public boolean delete(String invoiceId) throws DaoException {
        log.info("Start deleting payment info with invoiceId = {}", invoiceId);
        final String sql = "DELETE FROM hook.message where invoice_id=:invoice_id";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("invoice_id", invoiceId));
        } catch (DataAccessException e) {
            log.warn("MessageDaoImpl.delete error", e);
            throw new DaoException(e);
        }
        log.info("Payment info with invoiceId = {} deleted from table", invoiceId);
        return true;
    }

    @Override
    public boolean delete(long id) throws DaoException {
        final String sql = "DELETE FROM hook.message where id = :id";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
            if (updateCount != 1) {
                return false;
            }
        } catch (DataAccessException e) {
            log.warn("MessageDaoImpl.delete error", e);
            throw new DaoException(e);
        }
        return true;
    }

    @Override
    public List<Message> getBy(EventStatus eventStatus) {
        final String sql = "SELECT * FROM hook.message WHERE event_status = '" + eventStatus.toString() + "'";
        try {
            List<Message> messages = getNamedParameterJdbcTemplate().query(sql, messageRowMapper);
            return messages;
        }  catch (DataAccessException e) {
            log.error("MessageDaoImpl.getByStatus error", e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<Long> getIdsBy(EventStatus eventStatus) {
        final String sql = "SELECT id FROM hook.message WHERE event_status = '" + eventStatus.toString() + "'";
        try {
            List<Long> ids = getNamedParameterJdbcTemplate().queryForList(sql,new HashMap<>(), Long.class);
            return ids;
        }  catch (DataAccessException e) {
            log.error("MessageDaoImpl.getIdsByStatus error", e);
            throw new DaoException(e);
        }
    }

    @Override
    public void updateStatus(List<Long> ids, EventStatus eventStatus) {
        if(ids == null || ids.size() == 0){
            return;
        }
        final String sql = "UPDATE hook.message SET event_status = cast(:event_status as hook.eventstatus) WHERE id in (:ids)";
        try {
            getNamedParameterJdbcTemplate().update(sql,new MapSqlParameterSource("ids", ids).addValue("event_status", eventStatus.toString()));
        }  catch (DataAccessException e) {
            log.error("Fail to update status of messages.", e);
            throw new DaoException(e);
        }
    }
}
