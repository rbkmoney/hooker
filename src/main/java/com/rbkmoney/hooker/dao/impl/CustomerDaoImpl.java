package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.utils.CustomerUtils;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.swag_webhook_events.model.*;
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

import static com.rbkmoney.hooker.utils.PaymentToolUtils.getPaymentToolDetails;

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
    public static final String CUSTOMER_STATUS = "customer_status";
    public static final String CUSTOMER_EMAIL = "customer_email";
    public static final String CUSTOMER_PHONE = "customer_phone";
    public static final String CUSTOMER_METADATA = "customer_metadata";
    public static final String BINDING_ID = "binding_id";
    public static final String BINDING_PAYMENT_TOOL_TOKEN = "binding_payment_tool_token";
    public static final String BINDING_PAYMENT_SESSION = "binding_payment_session";
    public static final String BINDING_PAYMENT_TOOL_DETAILS_TYPE = "binding_payment_tool_details_type";
    public static final String BINDING_PAYMENT_CARD_BIN = "binding_payment_card_bin";
    public static final String BINDING_PAYMENT_CARD_LAST_DIGITS = "binding_payment_card_last_digits";
    public static final String BINDING_PAYMENT_CARD_NUMBER_MASK = "binding_payment_card_number_mask";
    public static final String BINDING_PAYMENT_CARD_SYSTEM = "binding_payment_card_system";
    public static final String BINDING_PAYMENT_CARD_TOKEN_PROVIDER = "binding_payment_card_token_provider";
    public static final String BINDING_PAYMENT_TERMINAL_PROVIDER = "binding_payment_terminal_provider";
    public static final String BINDING_PAYMENT_DIGITAL_WALLET_PROVIDER = "binding_payment_digital_wallet_provider";
    public static final String BINDING_PAYMENT_DIGITAL_WALLET_ID = "binding_payment_digital_wallet_id";
    public static final String BINDING_PAYMENT_CRYPTO_CURRENCY = "binding_payment_crypto_currency";
    public static final String BINDING_PAYMENT_MOBILE_COMMERCE_PHONE_NUMBER = "binding_payment_mobile_commerce_phone_number";
    public static final String BINDING_CLIENT_IP = "binding_client_ip";
    public static final String BINDING_CLIENT_FINGERPRINT = "binding_client_fingerprint";
    public static final String BINDING_STATUS = "binding_status";
    public static final String BINDING_ERROR_CODE = "binding_error_code";
    public static final String BINDING_ERROR_MESSAGE = "binding_error_message";

    //TODO refactoring
    private static void setNullPaymentParamValues(MapSqlParameterSource params) {
        params.addValue(BINDING_ID, null)
                .addValue(BINDING_PAYMENT_TOOL_TOKEN, null)
                .addValue(BINDING_PAYMENT_SESSION, null)
                .addValue(BINDING_PAYMENT_TOOL_DETAILS_TYPE, null)
                .addValue(BINDING_PAYMENT_CARD_BIN, null)
                .addValue(BINDING_PAYMENT_CARD_LAST_DIGITS, null)
                .addValue(BINDING_PAYMENT_CARD_NUMBER_MASK, null)
                .addValue(BINDING_PAYMENT_CARD_TOKEN_PROVIDER, null)
                .addValue(BINDING_PAYMENT_CARD_SYSTEM, null)
                .addValue(BINDING_PAYMENT_TERMINAL_PROVIDER, null)
                .addValue(BINDING_PAYMENT_DIGITAL_WALLET_PROVIDER, null)
                .addValue(BINDING_PAYMENT_DIGITAL_WALLET_ID, null)
                .addValue(BINDING_PAYMENT_CRYPTO_CURRENCY, null)
                .addValue(BINDING_PAYMENT_MOBILE_COMMERCE_PHONE_NUMBER, null)
                .addValue(BINDING_CLIENT_IP, null)
                .addValue(BINDING_CLIENT_FINGERPRINT, null)
                .addValue(BINDING_STATUS, null)
                .addValue(BINDING_ERROR_CODE, null)
                .addValue(BINDING_ERROR_MESSAGE, null);
    }

    private static RowMapper<CustomerMessage> messageRowMapper = (rs, i) -> {
        CustomerMessage message = new CustomerMessage();
        message.setId(rs.getLong(ID));
        message.setEventId(rs.getLong(EVENT_ID));
        message.setPartyId(rs.getString(PARTY_ID));
        message.setOccuredAt(rs.getString(OCCURED_AT));
        message.setSequenceId(rs.getLong(SEQUENCE_ID));
        message.setChangeId(rs.getInt(CHANGE_ID));
        message.setType(rs.getString(TYPE));
        message.setEventType(EventType.valueOf(rs.getString(EVENT_TYPE)));
        message.setCustomer(new Customer()
                .id(rs.getString(CUSTOMER_ID))
                .shopID(rs.getString(CUSTOMER_SHOP_ID))
                .status(Customer.StatusEnum.fromValue(rs.getString(CUSTOMER_STATUS)))
                .contactInfo(new ContactInfo()
                        .email(rs.getString(CUSTOMER_EMAIL))
                        .phoneNumber(rs.getString(CUSTOMER_PHONE)))
                .metadata(CustomerUtils.getJsonObject(rs.getString(CUSTOMER_METADATA))));
        if (message.isBinding()) {
            PaymentResource paymentResource = new PaymentResource()
                    .paymentSession(rs.getString(BINDING_PAYMENT_SESSION))
                    .paymentToolToken(rs.getString(BINDING_PAYMENT_TOOL_TOKEN))
                    .clientInfo(new ClientInfo()
                            .ip(rs.getString(BINDING_CLIENT_IP))
                            .fingerprint(rs.getString(BINDING_CLIENT_FINGERPRINT)));
            CustomerBinding customerBinding = new CustomerBinding()
                    .id(rs.getString(BINDING_ID))
                    .paymentResource(paymentResource);
            customerBinding.status(CustomerBinding.StatusEnum.fromValue(rs.getString(BINDING_STATUS)))
                    .error(new CustomerBindingError()
                            .code(rs.getString(BINDING_ERROR_CODE))
                            .message(rs.getString(BINDING_ERROR_MESSAGE)));
            message.setCustomerBinding(customerBinding);

            paymentResource.setPaymentToolDetails(getPaymentToolDetails(rs.getString(BINDING_PAYMENT_TOOL_DETAILS_TYPE), rs.getString(BINDING_PAYMENT_CARD_BIN),
                    rs.getString(BINDING_PAYMENT_CARD_LAST_DIGITS), rs.getString(BINDING_PAYMENT_CARD_NUMBER_MASK), rs.getString(BINDING_PAYMENT_CARD_TOKEN_PROVIDER), rs.getString(BINDING_PAYMENT_CARD_SYSTEM), rs.getString(BINDING_PAYMENT_TERMINAL_PROVIDER),
                    rs.getString(BINDING_PAYMENT_DIGITAL_WALLET_PROVIDER), rs.getString(BINDING_PAYMENT_DIGITAL_WALLET_ID), rs.getString(BINDING_PAYMENT_CRYPTO_CURRENCY), rs.getString(BINDING_PAYMENT_MOBILE_COMMERCE_PHONE_NUMBER)));
        }
        return message;
    };

    @Override
    public CustomerMessage getAny(String customerId, String type) throws DaoException {
        CustomerMessage result = null;
        final String sql = "SELECT * FROM hook.customer_message WHERE customer_id =:customer_id AND type=CAST(:type as hook.customer_message_type) ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(CUSTOMER_ID, customerId).addValue(TYPE, type);
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
                "(event_id, occured_at, sequence_id, change_id, type, party_id, event_type, " +
                "customer_id, customer_shop_id, customer_status, customer_email , customer_phone, customer_metadata, " +
                "binding_id, binding_payment_tool_token, binding_payment_session, binding_payment_tool_details_type, " +
                "binding_payment_card_bin, binding_payment_card_last_digits, binding_payment_card_number_mask, binding_payment_card_token_provider, binding_payment_card_system, binding_payment_terminal_provider, " +
                "binding_payment_digital_wallet_provider, binding_payment_digital_wallet_id, binding_payment_crypto_currency, " +
                "binding_payment_mobile_commerce_phone_number, " +
                "binding_client_ip, binding_client_fingerprint, binding_status, binding_error_code, binding_error_message) " +
                "VALUES " +
                "(:event_id, :occured_at, :sequence_id, :change_id, CAST(:type as hook.customer_message_type), :party_id, CAST(:event_type as hook.eventtype), " +
                ":customer_id, :customer_shop_id, CAST(:customer_status as hook.customer_status), :customer_email , :customer_phone, :customer_metadata, " +
                ":binding_id, :binding_payment_tool_token, :binding_payment_session, CAST(:binding_payment_tool_details_type as hook.payment_tool_details_type), " +
                ":binding_payment_card_bin, :binding_payment_card_last_digits, :binding_payment_card_number_mask, :binding_payment_card_token_provider, :binding_payment_card_system, :binding_payment_terminal_provider, " +
                ":binding_payment_digital_wallet_provider, :binding_payment_digital_wallet_id, :binding_payment_crypto_currency, " +
                ":binding_payment_mobile_commerce_phone_number, " +
                ":binding_client_ip, :binding_client_fingerprint, CAST(:binding_status as hook.customer_binding_status), :binding_error_code, :binding_error_message) " +
                "ON CONFLICT (customer_id, sequence_id, change_id) DO NOTHING " +
                "RETURNING id";
        Customer customer = message.getCustomer();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_ID, message.getEventId())
                .addValue(OCCURED_AT, message.getOccuredAt())
                .addValue(SEQUENCE_ID, message.getSequenceId())
                .addValue(CHANGE_ID, message.getChangeId())
                .addValue(TYPE, message.getType())
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(EVENT_TYPE, message.getEventType().name())
                .addValue(CUSTOMER_ID, customer.getId())
                .addValue(CUSTOMER_SHOP_ID, customer.getShopID())
                .addValue(CUSTOMER_STATUS, customer.getStatus().getValue())
                .addValue(CUSTOMER_EMAIL, customer.getContactInfo().getEmail())
                .addValue(CUSTOMER_PHONE, customer.getContactInfo().getPhoneNumber())
                .addValue(CUSTOMER_METADATA, customer.getMetadata() != null ? customer.getMetadata().toString() : null);

        //TODO
        setNullPaymentParamValues(params);
        if (message.isBinding()) {
            CustomerBinding customerBinding = message.getCustomerBinding();
            PaymentResource paymentResource = customerBinding.getPaymentResource();
            params.addValue(BINDING_ID, customerBinding.getId())
                    .addValue(BINDING_PAYMENT_TOOL_TOKEN,  paymentResource.getPaymentToolToken())
                    .addValue(BINDING_PAYMENT_SESSION, paymentResource.getPaymentSession())
                    .addValue(BINDING_CLIENT_IP, paymentResource.getClientInfo().getIp())
                    .addValue(BINDING_CLIENT_FINGERPRINT, paymentResource.getClientInfo().getFingerprint())
                    .addValue(BINDING_STATUS, customerBinding.getStatus().getValue())
                    .addValue(BINDING_ERROR_CODE, customerBinding.getError() != null ? customerBinding.getError().getCode() : null)
                    .addValue(BINDING_ERROR_MESSAGE, customerBinding.getError() != null ? customerBinding.getError().getMessage() : null);

            PaymentToolUtils.setPaymentToolDetailsParam(params, paymentResource.getPaymentToolDetails(),
                    BINDING_PAYMENT_TOOL_DETAILS_TYPE, BINDING_PAYMENT_CARD_BIN, BINDING_PAYMENT_CARD_LAST_DIGITS, BINDING_PAYMENT_CARD_NUMBER_MASK, BINDING_PAYMENT_CARD_TOKEN_PROVIDER, BINDING_PAYMENT_CARD_SYSTEM, BINDING_PAYMENT_TERMINAL_PROVIDER,
                    BINDING_PAYMENT_DIGITAL_WALLET_PROVIDER, BINDING_PAYMENT_DIGITAL_WALLET_ID, BINDING_PAYMENT_CRYPTO_CURRENCY, BINDING_PAYMENT_MOBILE_COMMERCE_PHONE_NUMBER);
        }
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
            throw new DaoException("Couldn't create customerMessage with customerId " + customer.getId(), e);
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
        }  catch (NestedRuntimeException e) {
            throw new DaoException("CustomerMessageDaoImpl.getByIds error", e);
        }
    }
}
