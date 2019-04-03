package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookManagerSrv;
import com.rbkmoney.damsel.webhooker.WebhookNotFound;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.utils.HookConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by inalarsanukaev on 06.04.17.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HookerService implements WebhookManagerSrv.Iface {

    private final HookDao hookDao;

    @Override
    public List<Webhook> getList(String s) throws TException {
        return HookConverter.convert(hookDao.getPartyHooks(s));
    }

    @Override
    public Webhook get(long id) throws WebhookNotFound {
        Hook hook = hookDao.getHookById(id);
        if (hook == null) {
            log.warn("Webhook not found: {}", id);
            throw new WebhookNotFound();
        }
        return HookConverter.convert(hook);
    }

    @Override
    public Webhook create(WebhookParams webhookParams) throws TException {
        Hook hook = hookDao.create(HookConverter.convert(webhookParams));
        log.info("Webhook created: {}", hook);
        return HookConverter.convert(hook);
    }

    @Override
    public void delete(long id) throws WebhookNotFound{
        try {
            hookDao.delete(id);
            log.info("Webhook deleted: {}", id);
        } catch (Exception e){
            log.error("Fail to delete webhook: {}", id, e);
            throw new WebhookNotFound();
        }
    }
}
