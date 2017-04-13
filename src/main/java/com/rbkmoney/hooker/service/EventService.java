package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.MessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventService {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MessageDao messageDao;

    public Long getLastEventId() {
        Long lastEventId = messageDao.getMaxEventId();
        log.info("Get last event id = {}", lastEventId);
        return lastEventId;
    }
}
