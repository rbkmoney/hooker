package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jeckep on 17.04.17.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTaskDao implements TaskDao {

    protected final NamedParameterJdbcTemplate jdbcTemplate;

    public static RowMapper<Task> taskRowMapper = (rs, i) ->
            new Task(rs.getLong("message_id"), rs.getLong("queue_id"));

    protected abstract String getMessageTopic();

    @Override
    public void remove(long queueId, long messageId) throws DaoException {
        final String sql = "DELETE FROM hook.scheduled_task where queue_id=:queue_id and message_id=:message_id and message_type=CAST(:message_type as hook.message_topic)";
        try {
            jdbcTemplate.update(sql, new MapSqlParameterSource("queue_id", queueId)
                    .addValue("message_id", messageId)
                    .addValue("message_type", getMessageTopic()));
            log.debug("Task with queueId {} messageId  {} removed from hook.scheduled_task", queueId, messageId);
        } catch (NestedRuntimeException e) {
            log.warn("Fail to delete task by queue_id {} and message_id {}", queueId, messageId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public void removeAll(long queueId) throws DaoException {
        final String sql = "DELETE FROM hook.scheduled_task where queue_id=:queue_id and message_type=CAST(:message_type as hook.message_topic)";
        try {
            jdbcTemplate.update(sql, new MapSqlParameterSource("queue_id", queueId).addValue("message_type", getMessageTopic()));
        } catch (NestedRuntimeException e) {
            log.warn("Fail to delete tasks for hook:" + queueId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public Map<Long, List<Task>> getScheduled() throws DaoException {
        final String sql = "SELECT st.message_id, st.queue_id " +
                "FROM hook.scheduled_task st " +
                "JOIN hook.simple_retry_policy srp ON st.queue_id=srp.queue_id " +
                "AND st.message_type=srp.message_type " +
                "WHERE st.message_type = CAST(:message_type as hook.message_topic) " +
                "AND COALESCE(srp.next_fire_time_ms, 0) < :curr_time " +
                "ORDER BY st.message_id ASC LIMIT 10000 FOR UPDATE SKIP LOCKED";
        try {
            List<Task> tasks = jdbcTemplate.query(sql,
                    new MapSqlParameterSource("message_type", getMessageTopic())
                            .addValue("curr_time", System.currentTimeMillis()),
                    taskRowMapper);
            return splitByQueue(tasks);
        } catch (NestedRuntimeException e) {
            log.warn("Fail to get active tasks from scheduled_task", e);
            throw new DaoException(e);
        }
    }

    //should preserve order by message id
    private Map<Long, List<Task>> splitByQueue(List<Task> orderedByMessageIdTasks) {
        return orderedByMessageIdTasks.stream().collect(Collectors.groupingBy(Task::getQueueId));
    }
}
