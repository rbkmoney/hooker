package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.hooker.configuration.CacheConfiguration;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;


public class MessageDaoImpl extends NamedParameterJdbcDaoSupport implements MessageDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TaskDao taskDao;

    @Autowired
    CacheManager cacheManager;

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
        message.setProduct(rs.getString("product"));
        message.setDescription(rs.getString("description"));
        message.setEventType(EventType.valueOf(rs.getString("event_type")));
        message.setEventId(rs.getLong("event_id"));
        message.setEventTime(rs.getString("event_time"));
        message.setType(rs.getString("type"));
        message.setStatus(rs.getString("status"));
        message.setPaymentId(rs.getString("payment_id"));
        return message;
    };

    public MessageDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    @Cacheable(CacheConfiguration.MESSAGES_BY_INVOICE)
    public Message getAny(String invoiceId) throws DaoException {
        Message result = null;
        final String sql = "SELECT * FROM hook.message WHERE invoice_id =:invoice_id LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource("invoice_id", invoiceId);
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Message with invoice id "+invoiceId+" not exist!");
        } catch (NestedRuntimeException e) {
            log.error("MessageDaoImpl.getAny error", e);
            throw new DaoException(e);
        }

        putToCache(result);
        return result;
    }

    @Override
    @Transactional
    public Message create(Message message) throws DaoException {
        String invoiceId = message.getInvoiceId();
        final String sql = "INSERT INTO hook.message(invoice_id, party_id, shop_id, amount, currency, created_at, content_type, content_data, event_id, event_time, event_type, type, payment_id, status, product, description) " +
                " VALUES (:invoice_id, :party_id, :shop_id, :amount, :currency, :created_at, :content_type, :content_data, :event_id, :event_time, " +
                " CAST(:event_type as hook.eventtype), :type, :payment_id, :status, :product, :description) " +
                " RETURNING id";
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
                .addValue("event_id", message.getEventId())
                .addValue("event_time", message.getEventTime())
                .addValue("event_type", message.getEventType().toString())
                .addValue("payment_id", message.getPaymentId())
                .addValue("status", message.getStatus())
                .addValue("product", message.getProduct())
                .addValue("description", message.getDescription());
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            message.setId(keyHolder.getKey().longValue());
            log.debug("Message {} save to db.", message.getId());

            // create tasks
            taskDao.create(message.getId());
            putToCache(message);
            return message;
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't create message with invoce_id "+ message.getInvoiceId(), e);
        }
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
        List<Message> messages = getFromCache(messageIds);

        Set<Long> ids = new HashSet<>();
        if(messages.size() == messageIds.size()){
            return messages;
        }else{
            ids.addAll(messageIds);
            for(Message message: messages){
                ids.remove(message.getId());
            }
        }

        final String sql = "SELECT * FROM hook.message WHERE id in (:ids)";
        try {
            List<Message> messagesFromDb = getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", ids), messageRowMapper);
            for(Message message: messagesFromDb){
                putToCache(message);
            }
            messages.addAll(messagesFromDb);
            return messages;
        }  catch (NestedRuntimeException e) {
            log.error("MessageDaoImpl.getByIds error", e);
            throw new DaoException(e);
        }
    }

    @Override
    public void delete(long id) throws DaoException {
        final String sql = "DELETE FROM hook.message where id = :id";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
        } catch (NestedRuntimeException e) {
            log.warn("MessageDaoImpl.delete error", e);
            throw new DaoException(e);
        }
    }

    private void putToCache(Message message){
        if(message != null) {
            cacheManager.getCache(CacheConfiguration.MESSAGES_BY_IDS).put(message.getId(), message);
            cacheManager.getCache(CacheConfiguration.MESSAGES_BY_INVOICE).put(message.getInvoiceId(), message);
        }
    }

    private List<Message> getFromCache(Collection<Long> ids) {
        Cache cache = cacheManager.getCache(CacheConfiguration.MESSAGES_BY_IDS);
        return ids.stream()
                .map(id -> cache.get(id, Message.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
