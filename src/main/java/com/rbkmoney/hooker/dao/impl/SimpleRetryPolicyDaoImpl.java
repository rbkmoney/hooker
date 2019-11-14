package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimpleRetryPolicyDaoImpl implements SimpleRetryPolicyDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void update(SimpleRetryPolicyRecord record) throws DaoException {
        final String sql = "update hook.simple_retry_policy " +
                " set last_fail_time = :last_fail_time, fail_count = :fail_count, next_fire_time_ms = :next_fire_time_ms" +
                " where queue_id = :queue_id and message_type=CAST(:message_type as hook.message_topic)";
        try {
            jdbcTemplate.update(sql, new MapSqlParameterSource("queue_id", record.getQueueId())
                    .addValue("message_type", record.getMessageType())
                    .addValue("last_fail_time", record.getLastFailTime())
                    .addValue("fail_count", record.getFailCount())
                    .addValue("next_fire_time_ms", record.getNextFireTime()));
        } catch (NestedRuntimeException e) {
            throw new DaoException("Fail to update simple_retry_policy for record=" + record.getQueueId(), e);
        }
    }
}
