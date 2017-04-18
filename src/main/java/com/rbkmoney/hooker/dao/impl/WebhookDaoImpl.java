package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.WebhookDao;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import com.rbkmoney.hooker.service.crypt.KeyPair;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by inal on 28.11.2016.
 */

public class WebhookDaoImpl extends NamedParameterJdbcDaoSupport implements WebhookDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public WebhookDaoImpl(DataSource ds) {
        this.setDataSource(ds);
    }

    public static RowMapper<Hook> hookWithPolicyRowMapper = (rs, i) -> {
        Hook hook = new Hook();
        hook.setId(rs.getLong("id"));
        hook.setPartyId(rs.getString("party_id"));
        hook.setUrl(rs.getString("url"));
        hook.setPubKey(rs.getString("pub_key"));
        hook.setPrivKey(rs.getString("priv_key"));
        hook.setEnabled(rs.getBoolean("enabled"));
        RetryPolicyType retryPolicyType = RetryPolicyType.valueOf(rs.getString("retry_policy"));
        hook.setRetryPolicyType(retryPolicyType);
        hook.setRetryPolicyRecord(retryPolicyType.build(rs));
        return hook;
    };

    @Override
    public List<Hook> getPartyWebhooks(String partyId) {
        log.info("New getPartyWebhooks request. partyId = {}", partyId);
        final String sql = "select w.*, k.pub_key, wte.event_type \n" +
                "from hook.webhook w \n" +
                "join hook.party_key k "+
                "on w.party_id = k.party_id " +
                "join hook.webhook_to_events wte " +
                "on wte.hook_id = w.id "+
                "where w.party_id =:party_id " +
                "order by id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("party_id", partyId);

        try {
            List<AllHookTablesRow> allHookTablesRows = getNamedParameterJdbcTemplate().query(sql, params, allHookTablesRowRowMapper);
            List<Hook> result = squashToWebhooks(allHookTablesRows);
            log.info("Response getPartyWebhooks.");
            return result;
        } catch (DataAccessException e) {
            String message = "Couldn't getPartyWebhooks for partyId " + partyId;
            log.warn(message, e);
            throw new DaoException(message);
        }
    }

    private List<Hook> squashToWebhooks(List<AllHookTablesRow> allHookTablesRows) {
        List<Hook> result = new ArrayList<>();
        if (allHookTablesRows == null || allHookTablesRows.isEmpty()) {
            return result;
        }
        final Map<Long, List<AllHookTablesRow>> hookIdToRows = new HashMap<>();

        //grouping by hookId
        for(AllHookTablesRow row: allHookTablesRows){
            final long hookId = row.id;
            List<AllHookTablesRow> list = hookIdToRows.get(hookId);
            if(list == null){
                list = new ArrayList<>();
                hookIdToRows.put(hookId, list);
            }
            list.add(row);
        }

        for(long hookId: hookIdToRows.keySet()){
            List<AllHookTablesRow> rows = hookIdToRows.get(hookId);
            Hook hook = new Hook();
            hook.setId(hookId);
            hook.setPartyId(rows.get(0).partyId);
            hook.setUrl(rows.get(0).url);
            hook.setPubKey(rows.get(0).pubKey);
            hook.setEnabled(rows.get(0).enabled);
            hook.setEventTypes(rows.stream().map(r -> r.eventType).collect(Collectors.toSet()));

            result.add(hook);
        }

        return result;
    }

    @Override
    public Hook getWebhookById(long id) {
        log.info("New getWebhook request. id = {}", id);
        final String sql = "select w.*, k.pub_key, wte.event_type \n" +
                "from hook.webhook w \n" +
                "join hook.party_key k " +
                "on w.party_id = k.party_id " +
                "join hook.webhook_to_events wte " +
                "on wte.hook_id = w.id "+
                "where w.id =:id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        try {
            List<AllHookTablesRow> allHookTablesRows = getNamedParameterJdbcTemplate().query(sql, params, allHookTablesRowRowMapper);
            List<Hook> result = squashToWebhooks(allHookTablesRows);
            if (result == null || result.isEmpty()) {
                return null;
            }
            return result.get(0);
        } catch (DataAccessException e) {
            String message = "Couldn't getWebhookById for id " + id;
            log.warn(message, e);
            throw new DaoException(message);
        }
    }

    public List<Hook> getWithPolicies(Collection<Long> ids){
        final String sql =
                " select w.*, k.*, srp.*" +
                " from hook.webhook w " +
                " join hook.party_key k on k.party_id = w.party_id " +
                " left join hook.simple_retry_policy srp on srp.hook_id = w.id" +
                " where w.id in (:ids)";
        final MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);

        try {
            List<Hook> hooks = getNamedParameterJdbcTemplate().query(sql, params, hookWithPolicyRowMapper);
            return hooks;
        } catch (DataAccessException e) {
            throw new DaoException(e);
        }

    }

    @Override
    //TODO keys, weebhook and relation between hook and events should be saved in transaction
    public Hook create(Hook hook) {
        KeyPair keyPair = createPairKey(hook.getPartyId());
        hook.setPubKey(keyPair.getPublKey());
        hook.setEnabled(true);

        final String sql = "INSERT INTO hook.webhook(party_id, url) " +
                "VALUES (:party_id, :url) RETURNING ID";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("party_id", hook.getPartyId())
                .addValue("url", hook.getUrl());
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            int updateCount = getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            if (updateCount != 1) {
                throw new DaoException("Couldn't insert webhook "+hook.getId()+" into table");
            }
            hook.setId(keyHolder.getKey().longValue());
            saveEventCodeList(hook.getId(), hook.getEventTypes());
            addRecordToRetryPolicy(hook.getId());
        } catch (DataAccessException e) {
            log.warn("WebhookDaoImpl.addWebhook error", e);
            throw new DaoException(e);
        }
        log.info("Webhook with id = {} added to table", hook.getId());
        return hook;
    }

    private void addRecordToRetryPolicy(long hookId){
        final String sql = "insert into hook.simple_retry_policy(hook_id) VALUES (:hook_id)";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("hook_id", hookId));
        } catch (DataAccessException e) {
            log.warn("Fail to create simple_retry_policy for hook: " + hookId, e);
            throw new DaoException(e);
        }

    }

    private void saveEventCodeList(long hookId, Collection<EventType> eventTypes){
        int size = eventTypes.size();
        List<Map<String, Object>> batchValues = new ArrayList<>(size);
        for (EventType eventType : eventTypes){
            batchValues.add(new MapSqlParameterSource("hook_id", hookId).addValue("event_type", eventType.toString()).getValues());
        }

        final String sql = "INSERT INTO hook.webhook_to_events(hook_id, event_type) VALUES (:hook_id, CAST(:event_type AS hook.eventtype))";

        try {
            int updateCount[] = getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[size]));
            if (updateCount.length != size) {
                throw new DaoException("Couldn't insert relation between hook and events.");
            }
        } catch (DataAccessException e) {
            log.warn("WebhookDaoImpl.addWebhookAndEventCodesRow error", e);
            throw new DaoException(e);
        }
    }

    @Override
    public boolean delete(long id) {
        log.info("Start deleting webhook info with id = {}", id);
        final String sql =
                " DELETE FROM hook.simple_retry_policy where hook_id=:id;" +
                " DELETE FROM hook.webhook_to_events where hook_id=:id;" +
                " DELETE FROM hook.webhook where id=:id; ";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
        } catch (DataAccessException e) {
            log.error("WebhookDaoImpl.delete error", e);
            throw new DaoException(e);
        }
        log.info("Webhook with id = {} deleted from table", id);
        return true;
    }

    @Override
    public void disable(long id) {
        final String sql = " UPDATE hook.webhook SET enabled = FALSE where id=:id; ";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
        } catch (DataAccessException e) {
            log.error("Fail to disable webhook: " + id, e);
            throw new DaoException(e);
        }
        log.info("Webhook with id = {} disabled", id);
    }

    @Autowired
    Signer signer;

    private KeyPair createPairKey(String partyId) {
        final String sql = "INSERT INTO hook.party_key(party_id, priv_key, pub_key) " +
                "VALUES (:party_id, :priv_key, :pub_key) " +
                "ON CONFLICT(party_id) DO NOTHING";

        KeyPair keyPair = signer.generateKeys();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("party_id", partyId)
                .addValue("priv_key", keyPair.getPrivKey())
                .addValue("pub_key", keyPair.getPublKey());
        try {
            getNamedParameterJdbcTemplate().update(sql, params);
        } catch (DataAccessException e) {
            log.warn("WebhookKeyDaoImpl.createPairKey error", e);
            throw new DaoException(e);
        }
        log.info("Key with party_id = {} added to table", partyId);
        return keyPair;
    }


    private static RowMapper<AllHookTablesRow> allHookTablesRowRowMapper =
            (rs, i) -> new AllHookTablesRow(rs.getLong("id"),
                rs.getString("party_id"),
                EventType.valueOf(rs.getString("event_type")),
                rs.getString("url"),
                rs.getString("pub_key"),
                rs.getBoolean("enabled"));


    static class AllHookTablesRow {
        long id;
        String partyId;
        EventType eventType;
        String url;
        String pubKey;
        boolean enabled;

        AllHookTablesRow(long id, String partyId, EventType eventType, String url, String pubKey, boolean enabled) {
            this.id = id;
            this.partyId = partyId;
            this.eventType = eventType;
            this.url = url;
            this.pubKey = pubKey;
            this.enabled = enabled;
        }
    }
}
