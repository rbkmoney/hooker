package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookManagerSrv;
import com.rbkmoney.damsel.webhooker.WebhookNotFound;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.utils.HookConverter;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by inalarsanukaev on 06.04.17.
 */
@Service
public class HookerService implements WebhookManagerSrv.Iface {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    HookDao hookDao;

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
            log.error("Fail to delete webhook: {}", id);
            throw new WebhookNotFound();
        }
    }
}
