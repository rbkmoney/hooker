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
        List<Hook> hooks;
        try {
            hooks = hookDao.getPartyHooks(s);
        } catch (Exception e) {
            throw new TException(e);
        }
        return HookConverter.convert(hooks);
    }

    @Override
    public Webhook get(long id) throws WebhookNotFound, TException {
        Hook hook;
        try {
            hook = hookDao.getHookById(id);
        } catch (Exception e) {
            throw new TException(e);
        }
        if (hook == null) {
            throw new WebhookNotFound();
        }
        return HookConverter.convert(hook);
    }

    @Override
    public Webhook create(WebhookParams webhookParams) throws TException {
        try {
            Hook hook = hookDao.create(HookConverter.convert(webhookParams));
            return HookConverter.convert(hook);
        } catch (Exception e) {
            throw new TException(e);
        }
    }

    @Override
    public void delete(long id) throws WebhookNotFound, TException {
        boolean isDeleted;
        try {
            isDeleted = hookDao.delete(id);
        } catch (Exception e) {
            throw new TException(e);
        }
        if (!isDeleted) {
            throw new WebhookNotFound();
        }
    }
}
