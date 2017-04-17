package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;

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
    public void create(Collection<Long> messageIds) {
        final String sql = " insert into hook.scheduled_task(message_id, hook_id)\n" +
                " select m.id, w.id \n" +
                " from hook.message m\n" +
                " join hook.webhook w on m.party_id = w.party_id and w.enabled = TRUE \n" +
                " where m.id in (:ids)" +
                " ON CONFLICT (message_id, hook_id) DO NOTHING";
        try {
            getNamedParameterJdbcTemplate().update(sql,new MapSqlParameterSource("ids", messageIds));
        }  catch (DataAccessException e) {
            log.error("Fail to create tasks for messages messages.", e);
            throw new DaoException(e);
        }
    }

    @Override
    public void remove(long hookId, long messageId) {
        final String sql = "DELETE FROM hook.scheduled_task where hook_id=:hook_id and message_id=:message_id";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("hook_id", hookId).addValue("message_id", messageId));
        } catch (DataAccessException e) {
            log.error("Fail to delete task by hook_id and message_id", e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<Task> getAll() {
        final String sql = "SELECT * FROM hook.scheduled_task";
        try {
            List<Task> tasks = getNamedParameterJdbcTemplate().query(sql, taskRowMapper);
            return tasks;
        }  catch (DataAccessException e) {
            log.error("MessageDaoImpl.getByStatus error", e);
            throw new DaoException(e);
        }
    }
}
