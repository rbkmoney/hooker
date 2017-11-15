package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jeckep on 17.04.17.
 */
public abstract class AbstractTaskDao extends NamedParameterJdbcDaoSupport implements TaskDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public AbstractTaskDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public static RowMapper<Task> taskRowMapper = (rs, i) ->
            new Task(rs.getLong("hook_id"), rs.getLong("message_id"), rs.getString("invoice_id"));

    protected abstract String getMessageTopic();

    @Override
    public void remove(long hookId, long messageId) {
        final String sql = "DELETE FROM hook.scheduled_task where hook_id=:hook_id and message_id=:message_id and message_type=CAST(:message_type as hook.message_topic)";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("hook_id", hookId)
                    .addValue("message_id", messageId)
                    .addValue("message_type", getMessageTopic()));
            log.debug("Task with hook_id = " + hookId + " messageId = " + messageId + " removed from hook.scheduled_task");
        } catch (NestedRuntimeException e) {
            log.error("Fail to delete task by hook_id and message_id", e);
            throw new DaoException(e);
        }
    }

    @Override
    public void removeAll(long hookId) {
        final String sql = "DELETE FROM hook.scheduled_task where hook_id=:hook_id and message_type=CAST(:message_type as hook.message_topic)";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("hook_id", hookId).addValue("message_type", getMessageTopic()));
        } catch (NestedRuntimeException e) {
            log.error("Fail to delete tasks for hook:" + hookId, e);
            throw new DaoException(e);
        }
    }

    @Override
    // should return ordered BY invoice_id, message_id
    public Map<String, List<Task>> getScheduled(Collection<Long> excludeHooksIds) {
        final String sql =
                " SELECT DISTINCT q.hook_id, q.invoice_id, st.message_id FROM hook.scheduled_task st " +
                        "JOIN hook.invoicing_queue q on q.hook_id=st.hook_id " +
                        "WHERE st.message_type=CAST(:message_type as hook.message_topic)" +
                        (excludeHooksIds.size() > 0 ? " AND st.hook_id not in (:hook_ids)" : "") +
                        " ORDER BY q.invoice_id, st.message_id ASC";
        try {
            List<Task> tasks = getNamedParameterJdbcTemplate().query(
                    sql,
                    new MapSqlParameterSource()
                            .addValue("hook_ids", excludeHooksIds)
                            .addValue("message_type", getMessageTopic())
                    , taskRowMapper);
            Map<String, List<Task>> longListMap = splitByInvoice(tasks);
            return longListMap;
        } catch (NestedRuntimeException e) {
            log.error("Fail to get active tasks from scheduled_task", e);
            throw new DaoException(e);
        }
    }

    //should preserve order
    //TODO Invoice ordering
    private Map<String, List<Task>> splitByInvoice(List<Task> orderedByInvoiceIdMessageIdTasks) {
        final Map<String, List<Task>> map = new HashMap<>();
        if (orderedByInvoiceIdMessageIdTasks.size() == 0) {
            return map;
        }
        int start = 0;
        String previousInvId = orderedByInvoiceIdMessageIdTasks.get(0).getInvoiceId();
        for (int i = 0; i < orderedByInvoiceIdMessageIdTasks.size(); i++) {
            String currentInvId = orderedByInvoiceIdMessageIdTasks.get(i).getInvoiceId();
            if (!previousInvId.equals(currentInvId)) {
                map.put(previousInvId, orderedByInvoiceIdMessageIdTasks.subList(start, i));
                start = i;
                previousInvId = currentInvId;
            }
        }
        map.put(previousInvId, orderedByInvoiceIdMessageIdTasks.subList(start, orderedByInvoiceIdMessageIdTasks.size()));

        return map;
    }
}
