package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.AbstractTaskDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by jeckep on 17.04.17.
 */
@Slf4j
@Component
public class InvoicingTaskDao extends AbstractTaskDao {

    public InvoicingTaskDao(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getMessageTopic() {
        return Event.TopicEnum.INVOICESTOPIC.getValue();
    }

    //TODO limit invoices from hook
    public int save(List<Long> messageIds) throws DaoException {
        if (messageIds == null || messageIds.isEmpty()) {
            return 0;
        }
        final String sql =
                " insert into hook.scheduled_task(message_id, queue_id, message_type)" +
                        " select m.id, q.id, w.topic" +
                        " from hook.message m" +
                        " join hook.webhook w on m.party_id = w.party_id " +
                        " and w.enabled and w.topic=CAST(:message_type as hook.message_topic)" +
                        " join hook.webhook_to_events wte on wte.hook_id = w.id" +
                        " join hook.invoicing_queue q on q.hook_id=w.id and q.enabled and q.invoice_id=m.invoice_id" +
                        " where m.id in (:message_ids) " +
                        " and m.event_type = wte.event_type " +
                        " and (m.shop_id = wte.invoice_shop_id or wte.invoice_shop_id is null) " +
                        " and (m.invoice_status = wte.invoice_status or wte.invoice_status is null) " +
                        " and (m.payment_status = wte.invoice_payment_status or wte.invoice_payment_status is null)" +
                        " and (m.refund_status = wte.invoice_payment_refund_status " +
                        " or wte.invoice_payment_refund_status is null)" +
                        " ON CONFLICT (message_id, queue_id, message_type) DO NOTHING";

        final MapSqlParameterSource sqlParameterSources = new MapSqlParameterSource("message_ids", messageIds)
                .addValue("message_type", Event.TopicEnum.INVOICESTOPIC.getValue());

        try {
            return jdbcTemplate.update(sql, sqlParameterSources);
        } catch (NestedRuntimeException e) {
            throw new DaoException("Failed to create tasks for messageIds=" + messageIds, e);
        }
    }

    public int save(Long hookId, String invoiceId) throws DaoException {
        final String sql =
                " insert into hook.scheduled_task(message_id, queue_id, message_type)" +
                        " select m.id, q.id, w.topic" +
                        " from hook.message m" +
                        " join hook.webhook w on m.party_id = w.party_id " +
                        "                    and w.id = :hook_id " +
                        "                    and w.enabled " +
                        "                    and w.topic=CAST(:message_type as hook.message_topic)" +
                        " join hook.webhook_to_events wte on wte.hook_id = w.id" +
                        " join hook.invoicing_queue q on q.hook_id=w.id " +
                        "                            and q.enabled " +
                        "                            and q.invoice_id=m.invoice_id" +
                        " where m.invoice_id = :invoice_id " +
                        " and m.event_type = wte.event_type " +
                        " and (m.shop_id = wte.invoice_shop_id or wte.invoice_shop_id is null) " +
                        " and (m.invoice_status = wte.invoice_status or wte.invoice_status is null) " +
                        " and (m.payment_status = wte.invoice_payment_status or wte.invoice_payment_status is null)" +
                        " and (m.refund_status = wte.invoice_payment_refund_status " +
                        " or wte.invoice_payment_refund_status is null)" +
                        " ON CONFLICT (message_id, queue_id, message_type) DO NOTHING";

        final MapSqlParameterSource sqlParameterSources = new MapSqlParameterSource("hook_id", hookId)
                .addValue("invoice_id", invoiceId)
                .addValue("message_type", Event.TopicEnum.INVOICESTOPIC.getValue());

        try {
            return jdbcTemplate.update(sql, sqlParameterSources);
        } catch (NestedRuntimeException e) {
            throw new DaoException("Failed to create tasks for hook_id=" + hookId, e);
        }
    }

    @Override
    public Map<Long, List<Task>> getScheduled() throws DaoException {
        final String sql = " WITH scheduled AS (" +
                "SELECT st.message_id, st.queue_id, iq.invoice_id " +
                "FROM hook.scheduled_task st " +
                "JOIN hook.invoicing_queue iq ON st.queue_id=iq.id AND iq.enabled " +
                "JOIN hook.simple_retry_policy srp ON st.queue_id=srp.queue_id " +
                "AND st.message_type=srp.message_type " +
                "JOIN hook.webhook w ON iq.hook_id = w.id AND w.enabled " +
                "WHERE st.message_type = CAST(:message_type as hook.message_topic) " +
                "AND COALESCE(srp.next_fire_time_ms, 0) < :curr_time " +
                "ORDER BY w.availability ASC, st.message_id ASC " +
                "LIMIT 1 " +
                "FOR UPDATE OF iq SKIP LOCKED " +
                "), locked_invoicing_queue AS (" +
                "  SELECT liq.id FROM hook.invoicing_queue liq " +
                "  WHERE liq.invoice_id IN (SELECT DISTINCT schd.invoice_id FROM scheduled schd) " +
                "  FOR UPDATE OF liq SKIP LOCKED " +
                ") SELECT message_id, queue_id FROM hook.scheduled_task s " +
                "  JOIN locked_invoicing_queue lq ON s.queue_id = lq.id " +
                " ORDER BY s.message_id " +
                " FOR UPDATE OF s SKIP LOCKED";
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
}
