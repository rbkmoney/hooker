package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.CustomerQueue;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.retry.RetryPolicyType;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerQueueDao implements QueueDao<CustomerQueue> {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public static RowMapper<CustomerQueue> queueWithPolicyRowMapper = (rs, i) -> {
        CustomerQueue queue = new CustomerQueue();
        queue.setId(rs.getLong("id"));
        queue.setCustomerId(rs.getString("customer_id"));
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

    public void createWithPolicy(long messageId) throws DaoException {
        final String sql = "with queue as ( " +
                " insert into hook.customer_queue(hook_id, customer_id)" +
                " select w.id, m.customer_id" +
                " from hook.customer_message m" +
                " join hook.webhook w on m.party_id = w.party_id and w.enabled and w.topic=CAST(:message_type as hook.message_topic)" +
                " where m.id = :id " +
                " on conflict(hook_id, customer_id) do nothing returning *) " +
                "insert into hook.simple_retry_policy(queue_id, message_type) select id, CAST(:message_type as hook.message_topic) from queue";
        try {
            int count = jdbcTemplate.update(sql, new MapSqlParameterSource("id", messageId)
                    .addValue("message_type", getMessagesTopic()));
            log.info("Created {} queues for messageId {}", count, messageId);
        } catch (NestedRuntimeException e) {
            log.error("Fail to createWithPolicy queue {}", messageId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<CustomerQueue> getWithPolicies(Collection<Long> ids) throws DaoException {
        final String sql =
                " select q.id, q.hook_id, q.customer_id, wh.party_id, wh.url, k.pub_key, k.priv_key, wh.enabled, wh.retry_policy, srp.fail_count, srp.last_fail_time, srp.next_fire_time_ms, srp.message_type " +
                        " from hook.customer_queue q " +
                        " join hook.webhook wh on wh.id = q.hook_id and wh.enabled and wh.topic=CAST(:message_type as hook.message_topic)" +
                        " join hook.party_data k on k.party_id = wh.party_id " +
                        " left join hook.simple_retry_policy srp on q.id = srp.queue_id and srp.message_type=CAST(:message_type as hook.message_topic)" +
                        " where q.id in (:ids) and q.enabled";
        final MapSqlParameterSource params = new MapSqlParameterSource("ids", ids)
                .addValue("message_type", getMessagesTopic());

        try {
            return jdbcTemplate.query(sql, params, queueWithPolicyRowMapper);
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void disable(long id) throws DaoException {
        final String sql = " UPDATE hook.customer_queue SET enabled = FALSE where id=:id;";
        try {
            jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
        } catch (NestedRuntimeException e) {
            log.error("Fail to disable queue: {}", id, e);
            throw new DaoException(e);
        }
    }

    public String getMessagesTopic() {
        return Event.TopicEnum.CUSTOMERSTOPIC.getValue();
    }
}
