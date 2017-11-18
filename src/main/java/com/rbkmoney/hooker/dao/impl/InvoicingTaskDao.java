package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.AbstractTaskDao;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.swag_webhook_events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;

/**
 * Created by jeckep on 17.04.17.
 */
public class InvoicingTaskDao extends AbstractTaskDao {

    Logger log = LoggerFactory.getLogger(this.getClass());

    public InvoicingTaskDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getMessageTopic() {
        return Event.TopicEnum.INVOICESTOPIC.getValue();
    }

    @Override
    public void create(long messageId) {
        final String sql =
                " insert into hook.scheduled_task(message_id, queue_id, message_type) " +
                        "select m.id, q.id, CAST(:message_type as hook.message_topic) " +
                        "from hook.invoicing_queue q " +
                        "join hook.message m on q.invoice_id=m.invoice_id " +
                        "where m.id=:message_id";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("message_id", messageId)
                    .addValue("message_type", getMessageTopic()));
            log.info("Created tasks count={} for messageId={}", updateCount, messageId);
        } catch (NestedRuntimeException e) {
            log.error("Fail to createWithPolicy tasks for messages.", e);
            throw new DaoException(e);
        }
    }
}
