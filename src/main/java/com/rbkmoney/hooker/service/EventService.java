package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final InvoicingMessageDao messageDao;
    private final CustomerDao customerDao;

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
