package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.AbstractTaskDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CustomerTaskDao extends AbstractTaskDao {

    public CustomerTaskDao(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getMessageTopic() {
        return Event.TopicEnum.CUSTOMERSTOPIC.getValue();
    }

    public void create(long messageId) throws DaoException {
        final String sql =
                " insert into hook.scheduled_task(message_id, queue_id, message_type)" +
                        " select m.id, q.id, w.topic" +
                        " from hook.customer_message m" +
                        " join hook.webhook w on m.party_id = w.party_id and w.enabled and w.topic=CAST(:message_type as hook.message_topic)" +
                        " join hook.webhook_to_events wte on wte.hook_id = w.id" +
                        " join hook.customer_queue q on q.hook_id=w.id and q.enabled and q.customer_id=m.customer_id" +
                        " where m.id = :message_id " +
                        " and m.event_type = wte.event_type " +
                        " and (m.customer_shop_id = wte.invoice_shop_id or wte.invoice_shop_id is null) " +
                        " ON CONFLICT (message_id, queue_id, message_type) DO NOTHING";
        try {
            jdbcTemplate.update(sql, new MapSqlParameterSource("message_id", messageId)
                    .addValue("message_type", getMessageTopic()));
        } catch (NestedRuntimeException e) {
            log.error("Fail to create tasks for messages.", e);
            throw new DaoException(e);
        }
    }

    public int create(long hookId, String customerId) throws DaoException {
        final String sql =
                " insert into hook.scheduled_task(message_id, queue_id, message_type)" +
                        " select m.id, q.id, w.topic" +
                        " from hook.customer_message m" +
                        " join hook.webhook w on m.party_id = w.party_id " +
                        "                    and w.id = :hook_id "+
                        "                    and w.enabled " +
                        "                    and w.topic=CAST(:message_type as hook.message_topic)" +
                        " join hook.webhook_to_events wte on wte.hook_id = w.id" +
                        " join hook.customer_queue q on q.hook_id=w.id " +
                        "                           and q.enabled " +
                        "                           and q.customer_id=m.customer_id" +
                        " where m.customer_id = :customer_id " +
                        " and m.event_type = wte.event_type " +
                        " and (m.customer_shop_id = wte.invoice_shop_id or wte.invoice_shop_id is null) " +
                        " ON CONFLICT (message_id, queue_id, message_type) DO NOTHING";
        try {
            return jdbcTemplate.update(sql, new MapSqlParameterSource("hook_id", hookId)
                    .addValue("customer_id", customerId)
                    .addValue("message_type", getMessageTopic()));
        } catch (NestedRuntimeException e) {
            log.error("Fail to create tasks for messages.", e);
            throw new DaoException(e);
        }
    }

    @Override
    public Map<Long, List<Task>> getScheduled(int limit) throws DaoException {
        final String sql = " WITH scheduled AS (" +
                "SELECT st.message_id, st.queue_id, cq.customer_id " +
                "FROM hook.scheduled_task st " +
                "JOIN hook.customer_queue cq ON st.queue_id=cq.id " +
                "JOIN hook.simple_retry_policy srp ON st.queue_id=srp.queue_id " +
                "AND st.message_type=srp.message_type " +
                "WHERE st.message_type = CAST(:message_type as hook.message_topic) " +
                "AND COALESCE(srp.next_fire_time_ms, 0) < :curr_time " +
                "ORDER BY st.message_id " +
                "ASC LIMIT :limit " +
                "FOR UPDATE SKIP LOCKED " +
                "), locked_customer_queue AS (" +
                "  SELECT cq.id FROM hook.customer_queue cq " +
                "  WHERE cq.customer_id IN (SELECT DISTINCT schd.customer_id FROM scheduled schd) " +
                "  FOR UPDATE SKIP LOCKED " +
                ") SELECT message_id, queue_id FROM scheduled s " +
                "  JOIN locked_customer_queue cq ON s.queue_id = cq.id";
        try {
            List<Task> tasks = jdbcTemplate.query(sql,
                    new MapSqlParameterSource("message_type", getMessageTopic())
                            .addValue("curr_time", System.currentTimeMillis())
                            .addValue("limit", limit),
                    taskRowMapper);
            return splitByQueue(tasks);
        } catch (NestedRuntimeException e) {
            log.warn("Fail to get active tasks from scheduled_task", e);
            throw new DaoException(e);
        }
    }

}
