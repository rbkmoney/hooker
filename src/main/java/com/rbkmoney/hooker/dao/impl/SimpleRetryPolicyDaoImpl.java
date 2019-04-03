package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;

/**
 * Created by jeckep on 17.04.17.
 */
@Slf4j
public class SimpleRetryPolicyDaoImpl extends NamedParameterJdbcDaoSupport implements SimpleRetryPolicyDao {

    public SimpleRetryPolicyDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public void update(SimpleRetryPolicyRecord record) throws DaoException {
        final String sql = "update hook.simple_retry_policy " +
                " set last_fail_time = :last_fail_time, fail_count = :fail_count" +
                " where queue_id = :queue_id and message_type=CAST(:message_type as hook.message_topic)";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("queue_id", record.getQueueId())
                    .addValue("message_type", record.getMessageType())
                    .addValue("last_fail_time", record.getLastFailTime())
                    .addValue("fail_count", record.getFailCount()));
            log.info("Record in table 'simple_retry_policy' with id {} updated.", record.getQueueId());
        } catch (NestedRuntimeException e) {
            log.warn("Fail to update simple_retry_policy for record {} ", record.getQueueId(), e);
            throw new DaoException(e);
        }
    }
}
