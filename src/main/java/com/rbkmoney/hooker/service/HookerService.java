package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookManagerSrv;
import com.rbkmoney.damsel.webhooker.WebhookNotFound;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.utils.HookConverter;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by inalarsanukaev on 06.04.17.
 */
@Service
public class HookerService implements WebhookManagerSrv.Iface {
    @Autowired
    HookDao hookDao;

    @Override
    public List<Webhook> getList(String s) throws TException {
        return HookConverter.convert(hookDao.getPartyHooks(s));
    }

    @Override
    public Webhook get(long id) throws WebhookNotFound, TException {
        Hook hook = hookDao.getHookById(id);
        if (hook == null) {
            throw new WebhookNotFound();
        }
        return HookConverter.convert(hook);
    }

    @Override
    public Webhook create(WebhookParams webhookParams) throws TException {
        return HookConverter.convert(hookDao.create(HookConverter.convert(webhookParams)));
    }

    @Override
    public void delete(long id) throws WebhookNotFound, TException {
        if (!hookDao.delete(id)) {
            throw new WebhookNotFound();
        }
    }
}
