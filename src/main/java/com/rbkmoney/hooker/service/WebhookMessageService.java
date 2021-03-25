package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.webhooker.SourceNotFound;
import com.rbkmoney.damsel.webhooker.WebhookMessageServiceSrv;
import com.rbkmoney.damsel.webhooker.WebhookNotFound;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookMessageService implements WebhookMessageServiceSrv.Iface {

    private final HookDao hookDao;
    private final InvoicingTaskDao invoicingTaskDao;
    private final CustomerTaskDao customerTaskDao;

    @Override
    public void send(long hookId, String sourceId) throws WebhookNotFound, SourceNotFound, TException {
        log.info("Start creating tasks for sending hooks for hookId={}, invoiceId={}", hookId, sourceId);
        Hook hook = hookDao.getHookById(hookId);
        if (hook == null) {
            log.warn("Webhook with id={} not found", hookId);
            throw new WebhookNotFound();
        }
        int count;
        if (hook.getTopic().equals(Event.TopicEnum.INVOICESTOPIC.getValue())) {
            count = invoicingTaskDao.save(hookId, sourceId);
        } else if (hook.getTopic().equals(Event.TopicEnum.CUSTOMERSTOPIC.getValue())) {
            count = customerTaskDao.create(hookId, sourceId);
        } else {
            throw new RuntimeException("Unknown webhook type " + hook.getTopic());
        }
        if (count < 1) {
            log.warn("No tasks created for hookId={} and invoiceId={}", hookId, sourceId);
            throw new SourceNotFound();
        }
        log.info("Tasks has been created. Count={} for hookId={}, invoiceId={}", count, hookId, sourceId);
    }
}
