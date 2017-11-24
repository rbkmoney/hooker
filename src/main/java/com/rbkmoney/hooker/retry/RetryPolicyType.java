package com.rbkmoney.hooker.retry;

import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jeckep on 17.04.17.
 */
public enum RetryPolicyType {
    /*
    * Первая и самая простая политика переотправки.
    * Если хук не отвечает или отвечает с ошибкой,
    * пробуем 4 раза с интервалами 30сек, 5мин, 15мин, 1час опять послать
    * неотправленное сообщение в этот хук. При этом очередь сообщений для хука копится.
    * После первой удачной отправки, после неудачной, счетчик неудачных попыток сбрасывается.
    * */

    SIMPLE {
        @Override
        public RetryPolicyRecord build(ResultSet rs) throws SQLException{
            SimpleRetryPolicyRecord record = new SimpleRetryPolicyRecord();
            record.setQueueId(rs.getLong("id"));
            record.setMessageType(rs.getString("message_type"));
            record.setFailCount(rs.getInt("fail_count"));
            record.setLastFailTime(rs.getLong("last_fail_time"));
            return record;
        }

        public SimpleRetryPolicyRecord cast(RetryPolicyRecord record){
            return (SimpleRetryPolicyRecord) record;
        }
    };

    public abstract RetryPolicyRecord build(ResultSet rs) throws SQLException;
}
