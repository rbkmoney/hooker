package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleanTablesDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public int cleanInvocing(int daysAgo) {
        final String sql =
            " with excluded_ids as (" +
            "    select queue_id from hook.scheduled_task st " +
            "    where message_type = CAST(:message_type as hook.message_topic)), " +
            "  ids as (" +
            "    select iq.id as queue_id from hook.invoicing_queue iq \n" +
            "    where iq.wtime < (now() at time zone 'utc' - (interval '1 days') * :days_ago) " +
            "      and iq.id not in (select excluded_ids.queue_id from excluded_ids) " +
            "      and iq.enabled), " +
            "  delete_simple_retry_policy as (" +
            "    delete from hook.simple_retry_policy srp " +
            "    using ids where srp.queue_id = ids.queue_id " +
            "                and srp.message_type = CAST(:message_type as hook.message_topic) " +
            "    returning *) " +
            "  delete from hook.invoicing_queue iq " +
            "  using ids where iq.id = ids.queue_id;";
        try {
            return jdbcTemplate.update(sql, new MapSqlParameterSource("days_ago", daysAgo)
                    .addValue("message_type", Event.TopicEnum.INVOICESTOPIC.getValue()));
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }

}
