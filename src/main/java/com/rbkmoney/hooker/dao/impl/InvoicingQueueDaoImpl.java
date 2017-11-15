package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.InvoicingQueueDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.InvoicingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class InvoicingQueueDaoImpl extends NamedParameterJdbcDaoSupport implements InvoicingQueueDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public InvoicingQueueDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public void createWithPolicy(long messageId) throws DaoException {
        final String sql = "with queue as ( " +
                "insert into hook.invoicing_queue(hook_id, invoice_id) " +
                "select t.hook_id, m.invoice_id from hook.message m " +
                "join hook.task t on t.message_id = m.id " +
                "where m.id=:id returning *) " +
                "insert into hook.simple_retry_policy(queue_id) select id from queue";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", messageId));
        } catch (NestedRuntimeException e) {
            log.error("Fail to createWithPolicy queue {}", messageId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<InvoicingQueue> getWithPolicies(Collection<Long> ids) {
        List<Hook> hooks;

        final String sql =
                " select w.*, k.*, srp.*" +
                        " from hook.webhook w " +
                        " join hook.party_key k on k.party_id = w.party_id " +
                        " left join hook.simple_retry_policy srp on srp.hook_id = w.id" +
                        " where w.id in (:ids)";
        final MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);

        try {
            List<Hook> hooksFromDb = jdbcTemplate.query(sql, params, hookWithPolicyRowMapper);
            hooks.addAll(hooksFromDb);
            return hooks;
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }
}
