package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.InvoicingQueue;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import com.rbkmoney.swag_webhook_events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class InvoicingQueueDao extends NamedParameterJdbcDaoSupport implements QueueDao<InvoicingQueue> {
    Logger log = LoggerFactory.getLogger(this.getClass());

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
                " join hook.webhook w on m.party_id = w.party_id and w.enabled" +
                " join hook.webhook_to_events wte on wte.hook_id = w.id" +
                " where m.id = :id " +
                " and m.event_type = wte.event_type " +
                " and (m.shop_id = wte.invoice_shop_id or wte.invoice_shop_id is null) " +
                " and (m.invoice_status = wte.invoice_status or wte.invoice_status is null) " +
                " and (m.payment_status = wte.invoice_payment_status or wte.invoice_payment_status is null)" +
                " returning *) " +
                "insert into hook.simple_retry_policy(queue_id) select id from queue";
        try {
            int count = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", messageId));
            log.info("Created {} queues for messageId {}", count, messageId);
        } catch (NestedRuntimeException e) {
            log.error("Fail to createWithPolicy queue {}", messageId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<InvoicingQueue> getWithPolicies(Collection<Long> ids) {
        final String sql =
                " select q.id, q.hook_id, q.invoice_id, wh.party_id, wh.url, k.pub_key, k.priv_key, wh.enabled, wh.retry_policy, srp.fail_count, srp.last_fail_time " +
                        " from hook.invoicing_queue q " +
                        " join hook.webhook wh on wh.id = q.hook_id " +
                        " join hook.party_key k on k.party_id = wh.party_id " +
                        " left join hook.simple_retry_policy srp on q.id = srp.queue_id" +
                        " where q.id in (:ids)";
        final MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);

        try {
            List<InvoicingQueue> queues = getNamedParameterJdbcTemplate().query(sql, params, queueWithPolicyRowMapper);
            return queues;
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void delete(long id) {
        final String sql =
                " DELETE FROM hook.scheduled_task where queue_id=:id AND message_type=CAST(:message_type as hook.message_topic);" +
                        " DELETE FROM hook.simple_retry_policy where queue_id=:id;" +
                        " DELETE FROM hook.invoicing_queue where id=:id; ";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id)
                    .addValue("message_type", Event.TopicEnum.INVOICESTOPIC.getValue()));
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }
}
