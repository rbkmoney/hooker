package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.impl.CustomerDaoImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final CustomerDaoImpl customerDao;

    public Long getLastEventId() {
        Long custLastEventId = customerDao.getMaxEventId();
        log.info("Get last event id = {}", custLastEventId);
        return custLastEventId;
    }
}
