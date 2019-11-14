package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerMessageEnum;
import com.rbkmoney.hooker.model.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by inalarsanukaev on 13.10.17.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerDaoImpl implements CustomerDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CustomerQueueDao queueDao;
    private final CustomerTaskDao taskDao;

    public static final String ID = "id";
    public static final String EVENT_ID = "event_id";
    public static final String TYPE = "type";
    public static final String OCCURED_AT = "occured_at";
    public static final String SEQUENCE_ID = "sequence_id";
    public static final String CHANGE_ID = "change_id";
    public static final String PARTY_ID = "party_id";
    public static final String EVENT_TYPE = "event_type";
    public static final String CUSTOMER_ID = "customer_id";
    public static final String CUSTOMER_SHOP_ID = "customer_shop_id";
    public static final String BINDING_ID = "binding_id";

    private static RowMapper<CustomerMessage> messageRowMapper = (rs, i) -> {
        CustomerMessage message = new CustomerMessage();
        message.setId(rs.getLong(ID));
        message.setEventId(rs.getLong(EVENT_ID));
        message.setPartyId(rs.getString(PARTY_ID));
        message.setEventTime(rs.getString(OCCURED_AT));
        message.setSequenceId(rs.getLong(SEQUENCE_ID));
        message.setChangeId(rs.getInt(CHANGE_ID));
        message.setType(CustomerMessageEnum.lookup(rs.getString(TYPE)));
        message.setEventType(EventType.valueOf(rs.getString(EVENT_TYPE)));
        message.setShopId(rs.getString(CUSTOMER_SHOP_ID));
        message.setBindingId(rs.getString(BINDING_ID));
        return message;
    };

    @Override
    public CustomerMessage getAny(String customerId, CustomerMessageEnum type) throws DaoException {
        CustomerMessage result = null;
        final String sql = "SELECT * FROM hook.customer_message " +
                "WHERE customer_id =:customer_id AND type=CAST(:type as hook.customer_message_type) " +
                "ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(CUSTOMER_ID, customerId).addValue(TYPE, type.getValue());
        try {
            result = jdbcTemplate.queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            log.warn("CustomerMessage with customerId {}, type {} not exist!", customerId, type);
        } catch (NestedRuntimeException e) {
            throw new DaoException("CustomerMessageDaoImpl.getAny error with customerId " + customerId, e);
        }
        return result;
    }

    @Transactional
    public void create(CustomerMessage message) throws DaoException {
        final String sql = "INSERT INTO hook.customer_message " +
                "(event_id, occured_at, sequence_id, change_id, type, " +
                "party_id, event_type, customer_id, customer_shop_id, binding_id) " +
                "VALUES " +
                "(:event_id, :occured_at, :sequence_id, :change_id, CAST(:type as hook.customer_message_type), " +
                ":party_id, CAST(:event_type as hook.eventtype), :customer_id, :customer_shop_id,  :binding_id) " +
                "ON CONFLICT (customer_id, sequence_id, change_id) DO NOTHING " +
                "RETURNING id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_ID, message.getEventId())
                .addValue(OCCURED_AT, message.getEventTime())
                .addValue(SEQUENCE_ID, message.getSequenceId())
                .addValue(CHANGE_ID, message.getChangeId())
                .addValue(TYPE, message.getType().getValue())
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(EVENT_TYPE, message.getEventType().name())
                .addValue(CUSTOMER_ID, message.getCustomerId())
                .addValue(CUSTOMER_SHOP_ID, message.getShopId())
                .addValue(BINDING_ID, message.getBindingId());
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, params, keyHolder);
            Number holderKey = keyHolder.getKey();
            if (holderKey != null) {
                message.setId(holderKey.longValue());
                log.info("CustomerMessage {} saved to db.", message);
                queueDao.createWithPolicy(message.getId());
                taskDao.create(message.getId());
            }
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't create customerMessage with customerId " + message.getCustomerId(), e);
        }
    }

    public Long getMaxEventId() {
        final String sql = "select max(event_id) from hook.customer_message ";
        try {
            return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<CustomerMessage> getBy(Collection<Long> messageIds) throws DaoException {
        if (messageIds.isEmpty()) {
            return new ArrayList<>();
        }
        final String sql = "SELECT * FROM hook.customer_message WHERE id in (:ids)";
        try {
            List<CustomerMessage> messagesFromDb = jdbcTemplate.query(sql, new MapSqlParameterSource("ids", messageIds), messageRowMapper);
            log.debug("messagesFromDb {}", messagesFromDb);
            return messagesFromDb;
        } catch (NestedRuntimeException e) {
            throw new DaoException("CustomerMessageDaoImpl.getByIds error", e);
        }
    }
}
