package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.AbstractTaskDao;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

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
            int updateCount = jdbcTemplate.update(sql, new MapSqlParameterSource("message_id", messageId)
                    .addValue("message_type", getMessageTopic()));
            log.info("Created tasks count={} for messageId={}", updateCount, messageId);
        } catch (NestedRuntimeException e) {
            log.error("Fail to create tasks for messages.", e);
            throw new DaoException(e);
        }
    }
}
