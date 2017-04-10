package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.service.crypt.KeyPair;
import com.rbkmoney.hooker.service.crypt.Signer;
import com.rbkmoney.hooker.utils.EventFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

/**
 * Created by inal on 28.11.2016.
 */
@Component
public class WebhookDaoImpl extends NamedParameterJdbcDaoSupport implements WebhookDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public WebhookDaoImpl(DataSource ds) {
        this.setDataSource(ds);
    }

    @Override
    public List<Webhook> getPartyWebhooks(String partyId) {
        log.info("New getPartyWebhooks request. partyId = {}", partyId);
        final String sql = "select w.*, k.pub_key \n" +
                "from hook.webhook w \n" +
                "join hook.key k "+
                "on w.party_id = k.party_id " +
                "where w.party_id =:party_id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("party_id", partyId);

        try {
            List<Webhook> result = getNamedParameterJdbcTemplate().query(sql, params, getWebhookRowMapper());
            log.info("Response getPartyWebhooks.");
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.error("Couldn't find template", e);
            throw new DaoException("Couldn't find webhook with partyId = " + partyId);
        }
    }

    @Override
    public Webhook getWebhookById(String id) {
        log.info("New getWebhook request. id = {}", id);
        final String sql = "select w.*, k.pub_key \n" +
                "from hook.webhook w \n" +
                "join hook.key k " +
                "on w.party_id = k.party_id " +
                "where w.id =:id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        try {
            Webhook result = getNamedParameterJdbcTemplate().queryForObject(sql, params, getWebhookRowMapper());
            log.info("Response getWebhook.");
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.error("Couldn't find webhook", e);
            throw new DaoException("Couldn't find webhook with id = " +id);
        }
    }

    @Override
    public List<Webhook> getWebhooksByCode(EventTypeCode typeCode, String partyId) {
        log.info("New getWebhookByCode request. TypeCode = {}, partyId = {}", typeCode, partyId);
        final String sql = "select w.*, k.pub_key \n" +
                "from hook.webhook w  \n" +
                "join hook.key k \n" +
                "on k.party_id = w.party_id " +
                "where w.code =:code " +
                "and w.party_id =:party_id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", typeCode.getKey());
        params.addValue("party_id", partyId);

        try {
            List<Webhook> result = getNamedParameterJdbcTemplate().query(sql, params, getWebhookRowMapper());
            log.info("Response getPartyWebhooks.");
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.error("Couldn't find template", e);
            throw new DaoException("Couldn't find webhook with typeCode = " + typeCode + "; partyId = " + partyId);
        }
    }

    @Override
    public Webhook addWebhook(WebhookParams webhookParams) {
        //TODO
        KeyPair keyPair = getPairKey(webhookParams.getPartyId());
        if (keyPair == null) {
            keyPair = createPairKey(webhookParams.getPartyId());
        }

        Webhook webhook = new Webhook();
        webhook.setId(UUID.randomUUID().toString());
        webhook.setEventFilter(webhookParams.getFilterStruct());
        webhook.setPartyId(webhookParams.getPartyId());
        webhook.setUrl(webhookParams.getUrl());
        webhook.setPubKey(keyPair.getPublKey());

        final String sql = "INSERT INTO hook.webhook(id, party_id, code, url) " +
                "VALUES (:id, :party_id, :code, :url)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", webhook.getId())
                .addValue("party_id", webhook.getPartyId())
                .addValue("code", EventFilterUtils.getEventTypeCodeByFilter(webhook.getEventFilter()).getKey())
                .addValue("url", webhookParams.getUrl());
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, params);
            if (updateCount != 1) {
                throw new DaoException("Couldn't insert webhook "+webhook.getId()+" into table");
            }
        } catch (DataAccessException e) {
            log.error("WebhookDaoImpl.addWebhook error", e);
            throw new DaoException(e);
        }
        log.info("Webhook with id = {} added to table", webhook.getId());
        return webhook;
    }

    @Override
    public void delete(final String id) {
        log.info("Start deleting webhook info with id = {}", id);
        final String sql = "DELETE FROM hook.webhook where id=:id";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
            if (updateCount != 1) {
                log.error("Couldn't delete webhook from table");
                throw new DaoException();
            }
        } catch (DataAccessException e) {
            log.error("WebhookDaoImpl.delete error", e);
            //throw new DaoException(e);
        }
        log.info("Webhook with id = {} deleted from table", id);
    }

    @Autowired
    Signer signer;

    @Override
    public KeyPair getPairKey(String partyId) {
        log.info("New getPairKey request. partyId = {}", partyId);
        final String sql = "select k.* from hook.key k where k.party_id =:party_id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("party_id", partyId);

        try {
            KeyPair result = getNamedParameterJdbcTemplate().queryForObject(sql, params,
                    (rs, i) -> new KeyPair(rs.getString("priv_key"), rs.getString("pub_key")));
            log.info("Response key.");
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.info("Couldn't find key for party", e);
            return null;
        }
    }

    @Override
    public KeyPair createPairKey(String partyId) {
        final String sql = "INSERT INTO hook.key(party_id, priv_key, pub_key) VALUES (:party_id, :priv_key, :pub_key)";

        KeyPair keyPair = signer.generateKeys();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("party_id", partyId)
                .addValue("priv_key", keyPair.getPrivKey())
                .addValue("pub_key", keyPair.getPublKey());
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, params);
            if (updateCount != 1) {
                throw new DaoException("Couldn't insert key "+partyId+" into table");
            }
        } catch (NestedRuntimeException e) {
            log.error("WebhookKeyDaoImpl.createPairKey error", e);
            throw new DaoException(e);
        }
        log.info("Key with party_id = {} added to table", partyId);
        return keyPair;
    }


    private RowMapper<Webhook> getWebhookRowMapper() {
        return (rs, i) -> new Webhook(rs.getString("id"),
                rs.getString("party_id"),
                EventFilterUtils.getEventFilterByCode(
                        EventTypeCode.valueOfKey(rs.getString("code"))),
                rs.getString("url"),
                rs.getString("pub_key")
                );
    }
}
