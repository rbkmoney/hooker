package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.InvoiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventService {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    InvoiceDao invoiceDao;

    public Long getLastEventId() {
        Long lastEventId = invoiceDao.getMaxEventId();
        log.info("Get last event id = {}", lastEventId);
        return lastEventId;
    }
}
