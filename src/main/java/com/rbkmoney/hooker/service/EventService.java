package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventService {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    InvoicingMessageDao messageDao;

    @Autowired
    CustomerDao customerDao;

    public Long getLastEventId(int div, int mod) {
        Long invLastEventId = messageDao.getMaxEventId(div, mod);
        Long custLastEventId = customerDao.getMaxEventId(div, mod);
        Long max = invLastEventId;
        if (invLastEventId == null) {
            max = custLastEventId;
        } else if (custLastEventId != null) {
            max = Math.max(invLastEventId, custLastEventId);
        }
        log.info("Get last event id = {}", max);
        return max;
    }
}
