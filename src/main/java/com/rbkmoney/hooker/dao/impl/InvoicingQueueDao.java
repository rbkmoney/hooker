package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.CacheMng;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.InvoicingQueue;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import com.rbkmoney.swag_webhook_events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class InvoicingQueueDao extends NamedParameterJdbcDaoSupport implements QueueDao<InvoicingQueue> {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    CacheMng cacheMng;

    public InvoicingQueueDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public static RowMapper<InvoicingQueue> queueWithPolicyRowMapper = (rs, i) -> {
        InvoicingQueue queue = new InvoicingQueue();
        queue.setId(rs.getLong("id"));
        queue.setInvoiceId(rs.getString("invoice_id"));
        Hook hook = new Hook();
        hook.setId(rs.getLong("hook_id"));
        hook.setPartyId(rs.getString("party_id"));
        hook.setTopic(rs.getString("message_type"));
        hook.setUrl(rs.getString("url"));
        hook.setPubKey(rs.getString("pub_key"));
        hook.setPrivKey(rs.getString("priv_key"));
        hook.setEnabled(rs.getBoolean("enabled"));
        RetryPolicyType retryPolicyType = RetryPolicyType.valueOf(rs.getString("retry_policy"));
        hook.setRetryPolicyType(retryPolicyType);
        queue.setHook(hook);
        queue.setRetryPolicyRecord(retryPolicyType.build(rs));
        return queue;
    };

    @Override
    public void createWithPolicy(long messageId) throws DaoException {
        final String sql = "with queue as ( " +
                " insert into hook.invoicing_queue(hook_id, invoice_id)" +
                " select w.id , m.invoice_id" +
                " from hook.message m" +
                " join hook.webhook w on m.party_id = w.party_id and w.enabled and w.topic=CAST(:message_type as hook.message_topic)" +
                " where m.id = :id " +
                " on conflict(hook_id, invoice_id) do nothing returning *) " +
                "insert into hook.simple_retry_policy(queue_id, message_type) select id, CAST(:message_type as hook.message_topic) from queue";
        try {
            int count = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", messageId)
                    .addValue("message_type", getMessagesTopic()));
            log.info("Created {} queues for messageId {}", count, messageId);
        } catch (NestedRuntimeException e) {
            log.error("Fail to createWithPolicy queue {}", messageId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<InvoicingQueue> getWithPolicies(Collection<Long> ids) {
        List<InvoicingQueue> queues = cacheMng.getQueues(ids, InvoicingQueue.class);
        if (queues.size() == ids.size()) {
            return queues;
        }
        Set<Long> queueIds = new HashSet<>(ids);
        queues.forEach(h -> queueIds.remove(h.getId()));
        final String sql =
                " select q.id, q.hook_id, q.invoice_id, wh.party_id, wh.url, k.pub_key, k.priv_key, wh.enabled, wh.retry_policy, srp.fail_count, srp.last_fail_time, srp.message_type " +
                        " from hook.invoicing_queue q " +
                        " join hook.webhook wh on wh.id = q.hook_id and wh.enabled and wh.topic=CAST(:message_type as hook.message_topic)" +
                        " join hook.party_key k on k.party_id = wh.party_id " +
                        " left join hook.simple_retry_policy srp on q.id = srp.queue_id and srp.message_type=CAST(:message_type as hook.message_topic)" +
                        " where q.id in (:ids) and q.enabled";
        final MapSqlParameterSource params = new MapSqlParameterSource("ids", queueIds)
                .addValue("message_type", getMessagesTopic());

        try {
            List<InvoicingQueue> queuesFromDb = getNamedParameterJdbcTemplate().query(sql, params, queueWithPolicyRowMapper);
            cacheMng.putQueues(queuesFromDb);
            queues.addAll(queuesFromDb);
            return queues;
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void disable(long id) throws DaoException {
        final String sql = " UPDATE hook.invoicing_queue SET enabled = FALSE where id=:id;";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
        } catch (NestedRuntimeException e) {
            log.error("Fail to disable queue: {}", id, e);
            throw new DaoException(e);
        }
    }

    public String getMessagesTopic() {
        return Event.TopicEnum.INVOICESTOPIC.getValue();
    }
}
