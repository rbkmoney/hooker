package com.rbkmoney.hooker.retry;

import com.rbkmoney.hooker.retry.impl.simple.SimpleRetryPolicyRecord;

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
        public SimpleRetryPolicyRecord cast(RetryPolicyRecord record){
            return (SimpleRetryPolicyRecord) record;
        }
    }
}
