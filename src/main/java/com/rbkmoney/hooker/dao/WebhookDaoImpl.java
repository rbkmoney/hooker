package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.service.crypt.KeyPair;
import com.rbkmoney.hooker.service.crypt.Signer;
import com.rbkmoney.hooker.utils.EventFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

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
        final String sql = "select w.*, k.pub_key, wte.event_code \n" +
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
            List<Webhook> result = squashToWebhooks(allHookTablesRows);
            log.info("Response getPartyWebhooks.");
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.info("Couldn't find webhooks for partyId "+partyId);
            return null;
        }
    }

    private List<Webhook> squashToWebhooks(List<AllHookTablesRow> allHookTablesRows) {
        final Map<String, List<AllHookTablesRow>> hookIdToRows = new HashMap<>();

        //grouping by hookId
        for(AllHookTablesRow row: allHookTablesRows){
            final String hookId = row.id;
            List<AllHookTablesRow> list = hookIdToRows.get(hookId);
            if(list == null){
                list = new ArrayList<>();
                hookIdToRows.put(hookId, list);
            }
            list.add(row);
        }

        List<Webhook> result = new ArrayList<>();
        for(String hookId: hookIdToRows.keySet()){
            List<AllHookTablesRow> rows = hookIdToRows.get(hookId);
            Webhook webhook = new Webhook();
            webhook.setId(hookId);
            webhook.setPartyId(rows.get(0).partyId);
            webhook.setUrl(rows.get(0).url);
            webhook.setPubKey(rows.get(0).pubKey);
            webhook.setEnabled(rows.get(0).enabled);

            webhook.setEventFilter(EventFilterUtils.getEventFilterByCode(rows.stream().map(r -> r.code).collect(Collectors.toList())));

            result.add(webhook);
        }

        return result;
    }

    @Override
    public Webhook getWebhookById(String id) {
        log.info("New getWebhook request. id = {}", id);
        final String sql = "select w.*, k.pub_key, wte.event_code \n" +
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
            List<Webhook> result = squashToWebhooks(allHookTablesRows);
            log.info("Response getWebhook.");
            return result.get(0);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Couldn't find webhook", e);
            throw new DaoException("Couldn't find webhook with id = " +id);
        }
    }

    @Override
    @Deprecated
    public List<Webhook> getWebhooksByCode(EventTypeCode typeCode, String partyId) {
        log.info("New getWebhookByCode request. TypeCode = {}, partyId = {}", typeCode, partyId);
        final String sql = "select w.*, k.pub_key, wte.event_code \n" +
                "from hook.webhook w  \n" +
                "join hook.party_key k \n" +
                "on k.party_id = w.party_id " +
                "join hook.webhook_to_events wte " +
                "on wte.hook_id = w.id "+
                "where wte.event_code =:code " +
                "and w.party_id =:party_id " +
                "order by w.id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", typeCode.getKey());
        params.addValue("party_id", partyId);

        try {
            List<AllHookTablesRow> allHookTablesRows = getNamedParameterJdbcTemplate().query(sql, params, allHookTablesRowRowMapper);
            List<Webhook> result = squashToWebhooks(allHookTablesRows);
            log.info("Response getWebhooksByCode.");
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.info("Couldn't find webhook with TypeCode = "+typeCode+", partyId = "+partyId);
            return null;
        }
    }

    @Override
    //TODO keys, weebhook and relation between hook and events should be saved in transaction
    public Webhook addWebhook(WebhookParams webhookParams) {
        Set<EventTypeCode> eventTypeCodeSetByFilter = EventFilterUtils.getEventTypeCodeSetByFilter(webhookParams.getEventFilter());
        //TODO: does damsel reflect this ???
        if (eventTypeCodeSetByFilter == null || eventTypeCodeSetByFilter.isEmpty()) {
            return null;
        }
        KeyPair keyPair = createPairKey(webhookParams.getPartyId());
        Webhook webhook = new Webhook();
        //TODO: use autogenerated id (bigint)
        webhook.setId(UUID.randomUUID().toString());
        webhook.setEventFilter(webhookParams.getEventFilter());
        webhook.setPartyId(webhookParams.getPartyId());
        webhook.setUrl(webhookParams.getUrl());
        webhook.setPubKey(keyPair.getPublKey());
        webhook.setEnabled(true);

        final String sql = "INSERT INTO hook.webhook(id, party_id, url) " +
                "VALUES (:id, :party_id, :url)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", webhook.getId())
                .addValue("party_id", webhook.getPartyId())
                .addValue("url", webhookParams.getUrl());
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, params);
            if (updateCount != 1) {
                throw new DaoException("Couldn't insert webhook "+webhook.getId()+" into table");
            }
            saveEventCodeList(webhook.getId(), EventFilterUtils.getCodes(eventTypeCodeSetByFilter));
        } catch (DataAccessException e) {
            log.warn("WebhookDaoImpl.addWebhook error", e);
            throw new DaoException(e);
        }
        log.info("Webhook with id = {} added to table", webhook.getId());
        return webhook;
    }

    private void saveEventCodeList(String hookId, List<String> eventCodes){
        final String sqlTemplateBegin = "INSERT INTO hook.webhook_to_events(hook_id, event_code) VALUES ";

        final MapSqlParameterSource params = new MapSqlParameterSource().addValue("hook_id", hookId);
        final List<String> insertValues = new ArrayList<>();
        for(int i=0; i < eventCodes.size(); i++){
            insertValues.add("(:hook_id, :code" + i + ")");
            params.addValue("code" + i, eventCodes.get(i));
        }

        final String sql =  sqlTemplateBegin + String.join(", ", insertValues) + ";";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, params);
            if (updateCount != eventCodes.size()) {
                throw new DaoException("Couldn't insert relation between hook and events.");
            }
        } catch (DataAccessException e) {
            log.warn("WebhookDaoImpl.addWebhookAndEventCodesRow error", e);
            throw new DaoException(e);
        }
    }

    @Override
    public boolean delete(final String id) {
        log.info("Start deleting webhook info with id = {}", id);
        final String sql =
                "DELETE FROM hook.webhook_to_events where hook_id=:id;" +
                "DELETE FROM hook.webhook where id=:id; ";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
        } catch (DataAccessException e) {
            log.warn("WebhookDaoImpl.delete error", e);
            throw new DaoException(e);
        }
        log.info("Webhook with id = {} deleted from table", id);
        return true;
    }

    @Autowired
    Signer signer;

    @Override
    public KeyPair getPairKey(String partyId) {
        log.info("New getPairKey request. partyId = {}", partyId);
        final String sql = "select k.* from hook.party_key k where k.party_id =:party_id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("party_id", partyId);

        try {
            KeyPair result = getNamedParameterJdbcTemplate().queryForObject(sql, params,
                    (rs, i) -> new KeyPair(rs.getString("priv_key"), rs.getString("pub_key")));
            log.info("Response key.");
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.info("Couldn't find key for party " + partyId);
            return null;
        }
    }

    @Override
    public KeyPair createPairKey(String partyId) {
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
            (rs, i) -> new AllHookTablesRow(rs.getString("id"),
                rs.getString("party_id"),
                EventTypeCode.valueOfKey(rs.getString("event_code")),
                rs.getString("url"),
                rs.getString("pub_key"),
                rs.getBoolean("enabled"));


    static class AllHookTablesRow {
        String id;
        String partyId;
        EventTypeCode code;
        String url;
        String pubKey;
        boolean enabled;

        AllHookTablesRow(String id, String partyId, EventTypeCode code, String url, String pubKey, boolean enabled) {
            this.id = id;
            this.partyId = partyId;
            this.code = code;
            this.url = url;
            this.pubKey = pubKey;
            this.enabled = enabled;
        }
    }
}
