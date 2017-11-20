package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.model.CustomerQueue;
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
public class CustomerQueueDao extends NamedParameterJdbcDaoSupport implements QueueDao<CustomerQueue> {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public CustomerQueueDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public static RowMapper<CustomerQueue> queueWithPolicyRowMapper = (rs, i) -> {
        CustomerQueue queue = new CustomerQueue();
        queue.setId(rs.getLong("id"));
        queue.setCustomerId(rs.getString("customer_id"));
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
                " insert into hook.customer_queue(hook_id, customer_id)" +
                " select w.id , m.customer_id" +
                " from hook.customer_message m" +
                " join hook.webhook w on m.party_id = w.party_id and w.enabled" +
                " where m.id = :id " +
                " on conflict(hook_id, customer_id) do nothing returning *) " +
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
    public List<CustomerQueue> getWithPolicies(Collection<Long> ids) {
        final String sql =
                " select q.id, q.hook_id, q.customer_id, wh.party_id, wh.url, k.pub_key, k.priv_key, wh.enabled, wh.retry_policy, srp.fail_count, srp.last_fail_time " +
                        " from hook.customer_queue q " +
                        " join hook.webhook wh on wh.id = q.hook_id " +
                        " join hook.party_key k on k.party_id = wh.party_id " +
                        " left join hook.simple_retry_policy srp on q.id = srp.queue_id" +
                        " where q.id in (:ids)";
        final MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);

        try {
            List<CustomerQueue> queues = getNamedParameterJdbcTemplate().query(sql, params, queueWithPolicyRowMapper);
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
                    .addValue("message_type", Event.TopicEnum.CUSTOMERSTOPIC.getValue()));
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }
}
