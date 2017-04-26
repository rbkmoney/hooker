package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;

/**
 * Created by jeckep on 17.04.17.
 */

public class SimpleRetryPolicyDaoImpl extends NamedParameterJdbcDaoSupport implements SimpleRetryPolicyDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public SimpleRetryPolicyDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public void update(SimpleRetryPolicyRecord record) {
        final String sql = "update hook.simple_retry_policy " +
                " set last_fail_time = :last_fail_time, fail_count = :fail_count" +
                " where hook_id = :hook_id";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("hook_id", record.getHookId())
                    .addValue("last_fail_time", record.getLastFailTime())
                    .addValue("fail_count", record.getFailCount()));
            log.info("Record in table hook_id = "+record.getHookId()+" 'simple_retry_policy' updated.");
        } catch (DataAccessException e) {
            log.error("Fail to update simple_retry_policy for hook: " + record.getHookId(), e);
            throw new DaoException(e);
        }
    }
}
