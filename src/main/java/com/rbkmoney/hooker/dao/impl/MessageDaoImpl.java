package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.configuration.CacheConfiguration;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;

public class MessageDaoImpl extends NamedParameterJdbcDaoSupport implements MessageDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TaskDao taskDao;

    @Autowired
    CacheManager cacheManager;

    public static final String ID = "id";
    public static final String EVENT_ID = "event_id";
    public static final String EVENT_TIME = "event_time";
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
    public static final String PAYMENT_ERROR_CODE = "payment_error_code";
    public static final String PAYMENT_ERROR_MESSAGE = "payment_error_message";
    public static final String PAYMENT_AMOUNT = "payment_amount";
    public static final String PAYMENT_CURRENCY = "payment_currency";
    public static final String PAYMENT_TOOL_TOKEN = "payment_tool_token";
    public static final String PAYMENT_SESSION = "payment_session";
    public static final String PAYMENT_EMAIL = "payment_email";
    public static final String PAYMENT_PHONE = "payment_phone";
    public static final String PAYMENT_IP = "payment_ip";
    public static final String PAYMENT_FINGERPRINT = "payment_fingerprint";

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

    private static RowMapper<Message> messageRowMapper = (rs, i) -> {
        Message message = new Message();
        message.setId(rs.getLong(ID));
        message.setEventId(rs.getLong(EVENT_ID));
        message.setEventTime(rs.getString(EVENT_TIME));
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
        InvoiceContent metadata = new InvoiceContent();
        metadata.setType(rs.getString(INVOICE_CONTENT_TYPE));
        metadata.setData(rs.getBytes(INVOICE_CONTENT_DATA));
        invoice.setMetadata(metadata);
        invoice.setProduct(rs.getString(INVOICE_PRODUCT));
        invoice.setDescription(rs.getString(INVOICE_DESCRIPTION));
        if (message.isPayment()) {
            Payment payment = new Payment();
            message.setPayment(payment);
            payment.setId(rs.getString(PAYMENT_ID));
            payment.setCreatedAt(rs.getString(PAYMENT_CREATED_AT));
            payment.setStatus(rs.getString(PAYMENT_STATUS));
            if (rs.getString(PAYMENT_ERROR_CODE) != null && "failed".equals(rs.getString(PAYMENT_STATUS))) {
                payment.setError(new PaymentStatusError(rs.getString(PAYMENT_ERROR_CODE), rs.getString(PAYMENT_ERROR_MESSAGE)));
            }
            payment.setAmount(rs.getLong(PAYMENT_AMOUNT));
            payment.setCurrency(rs.getString(PAYMENT_CURRENCY));
            payment.setPaymentToolToken(rs.getString(PAYMENT_TOOL_TOKEN));
            payment.setPaymentSession(rs.getString(PAYMENT_SESSION));
            payment.setContactInfo(new PaymentContactInfo(rs.getString(PAYMENT_EMAIL), rs.getString(PAYMENT_PHONE)));
            payment.setIp(rs.getString(PAYMENT_IP));
            payment.setFingerprint(rs.getString(PAYMENT_FINGERPRINT));
        }
        return message;
    };

    public MessageDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public Message getAny(String invoiceId, String type) throws DaoException {
        Message message = getFromCache(invoiceId, type);
        if (message != null) {
            return message.copy();
        }
        Message result = null;
        final String sql = "SELECT * FROM hook.message WHERE invoice_id =:invoice_id AND type =:type ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(INVOICE_ID, invoiceId).addValue(TYPE, type);
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
            final String sqlCarts = "SELECT * FROM hook.cart_position WHERE message_id =:message_id";
            MapSqlParameterSource paramsCarts = new MapSqlParameterSource("message_id", result.getId());
            List<InvoiceCartPosition> cart = getNamedParameterJdbcTemplate().query(sqlCarts, paramsCarts, cartPositionRowMapper);
            if (!cart.isEmpty()) {
                result.getInvoice().setCart(cart);
            }
        } catch (EmptyResultDataAccessException e) {
            log.warn("Message with invoiceId {} not exist!", invoiceId);
        } catch (NestedRuntimeException e) {
            throw new DaoException("MessageDaoImpl.getAny error with invoiceId " + invoiceId, e);
        }

        putToCache(result);
        return result;
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
    public Message create(Message message) throws DaoException {
        final String sql = "INSERT INTO hook.message" +
                "(event_id, event_time, type, party_id, event_type, " +
                "invoice_id, shop_id, invoice_created_at, invoice_status, invoice_reason, invoice_due_date, invoice_amount, " +
                "invoice_currency, invoice_content_type, invoice_content_data, invoice_product, invoice_description, " +
                "payment_id, payment_created_at, payment_status, payment_error_code, payment_error_message, payment_amount, " +
                "payment_currency, payment_tool_token, payment_session, payment_email, payment_phone, payment_ip, payment_fingerprint) " +
                "VALUES " +
                "(:event_id, :event_time, :type, :party_id, CAST(:event_type as hook.eventtype), " +
                ":invoice_id, :shop_id, :invoice_created_at, :invoice_status, :invoice_reason, :invoice_due_date, :invoice_amount, " +
                ":invoice_currency, :invoice_content_type, :invoice_content_data, :invoice_product, :invoice_description, " +
                ":payment_id, :payment_created_at, :payment_status, :payment_error_code, :payment_error_message, :payment_amount, " +
                ":payment_currency, :payment_tool_token, :payment_session, :payment_email, :payment_phone, :payment_ip, :payment_fingerprint) " +
                "RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_ID, message.getEventId())
                .addValue(EVENT_TIME, message.getEventTime())
                .addValue(TYPE, message.getType())
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(EVENT_TYPE, message.getEventType().toString())
                .addValue(SHOP_ID, message.getInvoice().getShopID())
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
                .addValue(INVOICE_DESCRIPTION, message.getInvoice().getDescription())
                .addValue(PAYMENT_ID, message.isPayment() ? message.getPayment().getId() : null)
                .addValue(PAYMENT_CREATED_AT, message.isPayment() ? message.getPayment().getCreatedAt() : null)
                .addValue(PAYMENT_STATUS, message.isPayment() ? message.getPayment().getStatus() : null)
                .addValue(PAYMENT_ERROR_CODE, message.isPayment() && message.getPayment().getError() != null ? message.getPayment().getError().getCode() : null)
                .addValue(PAYMENT_ERROR_MESSAGE, message.isPayment() && message.getPayment().getError() != null ? message.getPayment().getError().getMessage() : null)
                .addValue(PAYMENT_AMOUNT, message.isPayment() ? message.getPayment().getAmount() : null)
                .addValue(PAYMENT_CURRENCY, message.isPayment() ? message.getPayment().getCurrency() : null)
                .addValue(PAYMENT_TOOL_TOKEN, message.isPayment() ? message.getPayment().getPaymentToolToken() : null)
                .addValue(PAYMENT_SESSION, message.isPayment() ? message.getPayment().getPaymentSession() : null)
                .addValue(PAYMENT_EMAIL, message.isPayment() ? message.getPayment().getContactInfo().getEmail() : null)
                .addValue(PAYMENT_PHONE, message.isPayment() ? message.getPayment().getContactInfo().getPhoneNumber() : null)
                .addValue(PAYMENT_IP, message.isPayment() ? message.getPayment().getIp() : null)
                .addValue(PAYMENT_FINGERPRINT, message.isPayment() ? message.getPayment().getFingerprint() : null);
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            message.setId(keyHolder.getKey().longValue());
            saveCart(message.getId(), message.getInvoice().getCart());
            log.info("Message {} save to db.", message);

            // create tasks
            taskDao.create(message.getId());
            putToCache(message);
            return message;
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't create message with invoice_id "+ message.getInvoice().getId(), e);
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

        if (messages.size() == messageIds.size()) {
            return messages;
        }

        Set<Long> ids = new HashSet<>(messageIds);
        for (Message message : messages) {
            ids.remove(message.getId());
        }

        final String sql = "SELECT DISTINCT * FROM hook.message WHERE id in (:ids)";
        try {
            List<Message> messagesFromDb = getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", ids), messageRowMapper);
            log.debug("messagesFromDb {}", messagesFromDb);
            for(Message message: messagesFromDb){
                putToCache(message);
            }
            messages.addAll(messagesFromDb);
            return messages;
        }  catch (NestedRuntimeException e) {
            throw new DaoException("MessageDaoImpl.getByIds error", e);
        }
    }

    private void putToCache(Message message){
        if(message != null) {
            cacheManager.getCache(CacheConfiguration.MESSAGES_BY_IDS).put(message.getId(), message);
            cacheManager.getCache(CacheConfiguration.MESSAGES_BY_INVOICE).put(message.getInvoice().getId() + message.getType(), message);
        }
    }

    private Message getFromCache(String invoiceId, String type) {
        Cache cache = cacheManager.getCache(CacheConfiguration.MESSAGES_BY_INVOICE);
        return cache.get(invoiceId + type, Message.class);
    }

    private List<Message> getFromCache(Collection<Long> ids) {
        Cache cache = cacheManager.getCache(CacheConfiguration.MESSAGES_BY_IDS);
        List<Message> messages = new ArrayList<>();
        for (Long id : ids) {
            Message e = cache.get(id, Message.class);
            if (e != null) {
                messages.add(e);
            }
        }
        return messages;
    }
}
