package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.TaskDao;
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
public class TaskDaoImpl extends NamedParameterJdbcDaoSupport implements TaskDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public TaskDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public static RowMapper<Task> taskRowMapper = (rs, i) ->
            new Task(rs.getLong("hook_id"), rs.getLong("message_id"));

    @Override
    public void create(long messageId) {
        final String sql =
                " insert into hook.scheduled_task(message_id, hook_id)" +
                " select m.id, w.id " +
                " from hook.message m" +
                " join hook.webhook w on m.party_id = w.party_id and w.enabled" +
                " join hook.webhook_to_events wte on wte.hook_id = w.id" +
                " where m.id = :id " +
                " and m.event_type = wte.event_type " +
                " and (wte.invoice_shop_id is null or m.shop_id = wte.invoice_shop_id) " +
                " and ((m.invoice_status = wte.invoice_status or wte.invoice_status is null) or (m.payment_status = wte.invoice_payment_status or wte.invoice_payment_status is null))" +
                " ON CONFLICT (message_id, hook_id) DO NOTHING";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", messageId));
            log.debug("Created tasks count : " + updateCount);
        } catch (NestedRuntimeException e) {
            log.error("Fail to create tasks for messages messages.", e);
            throw new DaoException(e);
        }
    }

    @Override
    public void remove(long hookId, long messageId) {
        final String sql = "DELETE FROM hook.scheduled_task where hook_id=:hook_id and message_id=:message_id";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("hook_id", hookId).addValue("message_id", messageId));
            log.debug("Task with hook_id = " + hookId + " messageId = " + messageId + " removed from hook.scheduled_task");
        } catch (NestedRuntimeException e) {
            log.error("Fail to delete task by hook_id and message_id", e);
            throw new DaoException(e);
        }
    }

    @Override
    public void removeAll(long hookId) {
        final String sql = "DELETE FROM hook.scheduled_task where hook_id=:hook_id";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("hook_id", hookId));
        } catch (NestedRuntimeException e) {
            log.error("Fail to delete tasks for hook:" + hookId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<Task> getAll() {
        final String sql = "SELECT * FROM hook.scheduled_task";
        try {
            List<Task> tasks = getNamedParameterJdbcTemplate().query(sql, taskRowMapper);
            log.debug("Tasks count: " + tasks.size());
            return tasks;
        } catch (NestedRuntimeException e) {
            log.error("Fail to get all tasks from scheduled_task", e);
            throw new DaoException(e);
        }
    }

    @Override
    // should return ordered BY hook_id, message_id
    public Map<Long, List<Task>> getScheduled(Collection<Long> excludeHooksIds) {
        final String sql =
                " SELECT DISTINCT * " +
                " FROM hook.scheduled_task st" +
                (excludeHooksIds.size() > 0 ? " WHERE st.hook_id not in (:hook_ids)" : "") +
                " ORDER BY hook_id ASC , message_id ASC";
        try {
            List<Task> tasks = getNamedParameterJdbcTemplate().query(
                    sql,
                    new MapSqlParameterSource("enabled", true).addValue("hook_ids", excludeHooksIds)
                    , taskRowMapper);
            Map<Long, List<Task>> longListMap = splitByHooks(tasks);
            return longListMap;
        } catch (NestedRuntimeException e) {
            log.error("Fail to get active tasks from scheduled_task", e);
            throw new DaoException(e);
        }
    }

    //should preserve order
    private Map<Long, List<Task>> splitByHooks(List<Task> orderedByHookIdMessageIdTasks) {
        final Map<Long, List<Task>> map = new HashMap<>();
        if (orderedByHookIdMessageIdTasks.size() == 0) {
            return map;
        }
        int start = 0;
        long previousHookId = orderedByHookIdMessageIdTasks.get(0).getHookId();
        for (int i = 0; i < orderedByHookIdMessageIdTasks.size(); i++) {
            long currentHookId = orderedByHookIdMessageIdTasks.get(i).getHookId();
            if (previousHookId != currentHookId) {
                map.put(previousHookId, orderedByHookIdMessageIdTasks.subList(start, i));
                start = i;
                previousHookId = currentHookId;
            }
        }
        map.put(previousHookId, orderedByHookIdMessageIdTasks.subList(start, orderedByHookIdMessageIdTasks.size()));

        return map;
    }
}
